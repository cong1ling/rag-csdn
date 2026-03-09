# Bilibili 字幕获取实现方案文档

## 1. 背景与问题描述

### 1.1 项目目标
在 spring-ai-extensions 的 document-readers 模块中实现 Bilibili 视频内容读取器（BilibiliDocumentReader），用于提取视频的标题、简介和字幕内容，以支持 AI 应用对 B站视频内容的理解和处理。

### 1.2 当前实现存在的问题
当前 `BilibiliDocumentReader` 实现尝试从视频基本信息接口（`/x/web-interface/view`）直接获取字幕数据：

```java
JsonNode subtitleList = videoData.path("subtitle").path("list");
```

**问题分析**：
- 视频基本信息接口（`/x/web-interface/view`）在当前版本的实现中**默认将字幕地址置空**
- 该接口仅提供视频元数据（标题、作者、播放量、简介等），不再直接返回字幕信息
- B站字幕分为两种类型：
  - **UP主上传字幕**：由创作者手动上传的字幕文件
  - **AI生成字幕**：包括中文（自动生成）和英文（自动生成）等多语言版本
- 推测由于 AI 生成字幕涉及算力成本，B站强制要求通过播放器接口（`/x/player/wbi/v2`）获取字幕
- 播放器接口需要 WBI 签名验证，当前实现未包含签名逻辑

## 2. Bilibili 字幕获取完整流程

根据对 github上公开的B站api文档 分析，正确的字幕获取流程包含以下四个步骤：

### 2.1 步骤一：获取视频基本信息

**接口地址**：
```
GET https://api.bilibili.com/x/web-interface/view
```

**请求参数**：
- `bvid`: 视频 BV 号（必填）

**认证要求**：无需认证

**返回数据**：
```json
{
  "code": 0,
  "data": {
    "title": "视频标题",
    "desc": "视频简介",
    "owner": {...},
    "stat": {...},
     ...
  }
}
```

**提取字段**：
- `data.title`: 视频标题
- `data.desc`: 视频简介

### 2.2 步骤二：获取分P列表（获取 cid）

**接口地址**：
```
GET https://api.bilibili.com/x/player/pagelist
```

**请求参数**：
- `bvid`: 视频 BV 号（必填）

**认证要求**：无需认证

**返回数据**：
```json
{
  "code": 0,
  "data": [
    {
      "cid": 1234567890,
      "page": 1,
      "part": "分P标题",
      "duration": 300
    }
  ]
}
```

**提取字段**：
- `data[0].cid`: 第一个分P的 cid（视频分段ID，后续获取字幕必需）

### 2.3 步骤三：获取字幕列表（需要 WBI 签名）

**接口地址**：
```
GET https://api.bilibili.com/x/player/wbi/v2
```

**请求参数**：
- `bvid`: 视频 BV 号（必填）
- `cid`: 视频分段 ID（必填，从步骤二获取）
- `wts`: 时间戳（WBI 签名参数）
- `w_rid`: WBI 签名哈希值（WBI 签名参数）
- `web_location`: 固定值 1315873 或 1550101

**认证要求**：需要 WBI 签名验证

**返回数据**：
```json
{
  "code": 0,
  "data": {
    "subtitle": {
      "subtitles": [
        {
          "lan": "ai-zh",
          "lan_doc": "中文",
          "subtitle_url": "//i0.hdslb.com/bfs/subtitle/xxx.json"
        }
      ]
    }
  }
}
```

**提取字段**：
- `data.subtitle.subtitles[0].subtitle_url`: 字幕文件下载地址

**注意事项**：
- 字幕 URL 可能缺少协议前缀，需要补充 `https:`
- 如果 `subtitles` 数组为空，表示该视频无字幕

### 2.4 步骤四：下载字幕内容

**接口地址**：
```
GET https://i0.hdslb.com/bfs/subtitle/xxx.json
```
（从步骤三获取的完整 URL）

**认证要求**：无需认证（字幕文件为公开资源）

**返回数据**：
```json
{
  "font_size": 0.4,
  "font_color": "#FFFFFF",
  "body": [
    {
      "from": 0.0,
      "to": 2.5,
      "content": "字幕文本内容"
    },
    {
      "from": 2.5,
      "to": 5.0,
      "content": "下一句字幕"
    }
  ]
}
```

**处理逻辑**：
- 遍历 `body` 数组
- 提取每个元素的 `content` 字段
- 拼接成完整文本

## 3. WBI 签名算法详解

### 3.1 算法概述
WBI（Web Bilibili Interface）签名是 B站的反爬虫机制，用于验证请求的合法性。签名算法包含以下步骤：

### 3.2 获取 Mixin Key

**步骤 1**：请求导航接口获取图片 URL
```
GET https://api.bilibili.com/x/web-interface/nav
```

**返回数据**：
```json
{
  "data": {
    "wbi_img": {
      "img_url": "https://i0.hdslb.com/bfs/wbi/7cd084941338484aae1ad9425b84077c.png",
      "sub_url": "https://i0.hdslb.com/bfs/wbi/4932caff0ff746eab6f01bf08b70ac45.png"
    }
  }
}
```

**步骤 2**：提取文件名
- `img_key` = `7cd084941338484aae1ad9425b84077c`（从 `img_url` 提取）
- `sub_key` = `4932caff0ff746eab6f01bf08b70ac45`（从 `sub_url` 提取）
- `raw_key` = `img_key` + `sub_key`

**步骤 3**：按照固定索引表重排字符
```java
int[] MIXIN_KEY_ENC_TAB = {
    46, 47, 18, 2, 53, 8, 23, 32, 15, 50, 10, 31, 58, 3, 45, 35, 27, 43, 5, 49,
    33, 9, 42, 19, 29, 28, 14, 39, 12, 38, 41, 13, 37, 48, 7, 16, 24, 55, 40,
    61, 26, 17, 0, 1, 60, 51, 30, 4, 22, 25, 54, 21, 56, 59, 6, 63, 57, 62, 11,
    36, 20, 34, 44, 52
};

String mixinKey = "";
for (int index : MIXIN_KEY_ENC_TAB) {
    if (index < rawKey.length()) {
        mixinKey += rawKey.charAt(index);
    }
}
mixinKey = mixinKey.substring(0, 32); // 取前32位
```

### 3.3 生成签名

**步骤 1**：构建参数字典
```java
Map<String, Object> params = new TreeMap<>(); // 使用 TreeMap 自动排序
params.put("bvid", bvid);
params.put("cid", cid);
params.put("wts", System.currentTimeMillis() / 1000); // 当前时间戳（秒）
params.put("web_location", 1315873);
```

**步骤 2**：URL 编码并排序
```java
String queryString = params.entrySet().stream()
    .sorted(Map.Entry.comparingByKey())
    .map(e -> e.getKey() + "=" + URLEncoder.encode(String.valueOf(e.getValue()), "UTF-8"))
    .collect(Collectors.joining("&"));
```

**步骤 3**：计算 MD5 签名
```java
String signString = queryString + mixinKey;
String w_rid = md5(signString); // 使用自定义 md5 方法
params.put("w_rid", w_rid);
```

**步骤 4**：发送请求
```
GET https://api.bilibili.com/x/player/wbi/v2?bvid=xxx&cid=xxx&wts=xxx&w_rid=xxx&web_location=1315873
```

### 3.4 签名算法说明
- **Mixin Key 缓存**：建议缓存 mixin key，避免每次请求都重新获取（有效期约为数小时）
- **时间戳精度**：`wts` 使用秒级时间戳（Unix timestamp）
- **参数排序**：必须按参数名的 ASCII 码顺序排序
- **编码规范**：使用 UTF-8 编码

## 4. 实现建议

### 4.1 Cookie 认证与 Builder 模式

#### 4.1.1 认证需求说明

播放器接口（`/x/player/wbi/v2`）虽然主要依赖 WBI 签名验证，但在某些情况下（如访问受限视频或提高请求成功率）可能需要携带用户 Cookie。建议使用 Builder 模式创建 `BilibiliDocumentReader` 实例，支持可选的 Cookie 认证。

**需要支持的 Cookie 字段**：
- `SESSDATA`：用户会话标识（必需，用于身份验证）
- `bili_jct`：CSRF Token（必需，用于 POST 请求防护）
- `buvid3`：浏览器指纹标识（可选，用于设备识别）

**不需要支持的字段**：
- `ac_time_value`：用于 Cookie 刷新机制，DocumentReader 生命周期较短，无需实现自动刷新功能

#### 4.1.2 Builder 模式设计

```java
public class BilibiliDocumentReader implements DocumentReader {

    private final String resourcePath;
    private final String sessdata;
    private final String biliJct;
    private final String buvid3;
    private final ObjectMapper objectMapper;
    private final WebClient webClient;

    // 私有构造函数，仅通过 Builder 创建
    private BilibiliDocumentReader(Builder builder) {
        this.resourcePath = builder.resourcePath;
        this.sessdata = builder.sessdata;
        this.biliJct = builder.biliJct;
        this.buvid3 = builder.buvid3;
        this.objectMapper = new ObjectMapper();
        this.webClient = createWebClient();
    }

    // Builder 静态内部类
    public static class Builder {
        private final String resourcePath; // 必需参数
        private String sessdata;
        private String biliJct;
        private String buvid3;

        public Builder(String resourcePath) {
            Assert.hasText(resourcePath, "Resource path must not be empty");
            this.resourcePath = resourcePath;
        }

        public Builder sessdata(String sessdata) {
            this.sessdata = sessdata;
            return this;
        }

        public Builder biliJct(String biliJct) {
            this.biliJct = biliJct;
            return this;
        }

        public Builder buvid3(String buvid3) {
            this.buvid3 = buvid3;
            return this;
        }

        public BilibiliDocumentReader build() {
            return new BilibiliDocumentReader(this);
        }
    }

    // 创建 WebClient，自动添加 Cookie
    private WebClient createWebClient() {
        return WebClient.builder()
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultHeader(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
            .defaultHeader(HttpHeaders.COOKIE, buildCookieHeader())
            .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(MAX_MEMORY_SIZE))
            .build();
    }

    // 构建 Cookie 请求头
    private String buildCookieHeader() {
        List<String> cookies = new ArrayList<>();
        if (sessdata != null && !sessdata.isEmpty()) {
            cookies.add("SESSDATA=" + sessdata);
        }
        if (biliJct != null && !biliJct.isEmpty()) {
            cookies.add("bili_jct=" + biliJct);
        }
        if (buvid3 != null && !buvid3.isEmpty()) {
            cookies.add("buvid3=" + buvid3);
        }
        return String.join("; ", cookies);
    }

    @Override
    public List<Document> get() {
        // 实现逻辑...
    }
}
```

#### 4.1.3 使用示例

**无认证模式**（仅使用 WBI 签名）：
```java
BilibiliDocumentReader reader = new BilibiliDocumentReader.Builder("BV1xx411c7mD")
    .build();
List<Document> documents = reader.get();
```

**完整认证模式**（推荐）：
```java
BilibiliDocumentReader reader = new BilibiliDocumentReader.Builder("BV1xx411c7mD")
    .sessdata("your_sessdata_value")
    .biliJct("your_bili_jct_value")
    .buvid3("your_buvid3_value")
    .build();
List<Document> documents = reader.get();
```

**部分认证模式**：
```java
BilibiliDocumentReader reader = new BilibiliDocumentReader.Builder("BV1xx411c7mD")
    .sessdata("your_sessdata_value")
    .biliJct("your_bili_jct_value")
    .build();
List<Document> documents = reader.get();
```

#### 4.1.4 Cookie 获取方法

用户可通过以下步骤获取 Cookie 值：

1. 在浏览器中登录 B站（https://www.bilibili.com）
2. 打开浏览器开发者工具（F12）
3. 切换到 Application/应用程序 标签页
4. 在左侧菜单选择 Cookies → https://www.bilibili.com
5. 找到以下字段并复制其值：
   - `SESSDATA`
   - `bili_jct`
   - `buvid3`

**注意事项**：
- Cookie 具有时效性，过期后需要重新获取
- 不要在公共代码仓库中硬编码 Cookie 值
- 建议通过环境变量或配置文件传入 Cookie

### 4.2 代码结构调整

### 4.2 代码结构调整

建议将 `BilibiliDocumentReader` 拆分为以下方法：

```java
public class BilibiliDocumentReader implements DocumentReader {

    // 1. 获取视频基本信息
    private VideoInfo fetchVideoInfo(String bvid);

    // 2. 获取分P列表
    private List<Page> fetchPages(String bvid);

    // 3. WBI 签名相关
    private String getMixinKey(); // 获取并缓存 mixin key
    private Map<String, Object> generateWbiParams(String bvid, long cid);

    // 4. 获取字幕列表
    private SubtitleList fetchPlayerInfo(String bvid, long cid);

    // 5. 下载字幕内容
    private String downloadSubtitle(String subtitleUrl);

    // 6. 主流程
    @Override
    public List<Document> get();
}
```

### 4.3 依赖库建议

- **HTTP 客户端**：继续使用 Spring WebClient
- **JSON 解析**：继续使用 Jackson ObjectMapper
- **MD5 计算**：使用 Java 标准库 `MessageDigest`，无需引入额外依赖

**MD5 工具方法实现**（参考项目中的 AuthTools.java 风格）：
```java
private String md5(String input) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        StringBuilder hex = new StringBuilder();
        for (byte b : digest) {
            String tmp = Integer.toHexString(b & 0xFF);
            if (tmp.length() == 1) {
                hex.append("0");
            }
            hex.append(tmp);
        }
        return hex.toString();
    }
    catch (NoSuchAlgorithmException e) {
        throw new RuntimeException("MD5 algorithm not found", e);
    }
}
```

### 4.4 错误处理

建议增加以下异常处理：
- 网络请求失败（超时、连接失败）
- API 返回错误码（code != 0）
- 字幕不存在的情况
- WBI 签名失败

### 4.5 性能优化

- **Mixin Key 缓存**：使用 Caffeine 或 Guava Cache 缓存 mixin key（TTL: 1小时）
- **并发请求**：如果需要处理多个视频，可使用 WebClient 的异步特性
- **连接池复用**：WebClient 实例应该复用，避免频繁创建

## 5. 参考资源

- **bilibili-api Python 库**：https://github.com/Nemo2011/bilibili-api
- **B站 API 文档**：https://github.com/SocialSisterYi/bilibili-API-collect
- **WBI 签名算法说明**：https://github.com/SocialSisterYi/bilibili-API-collect/issues/903

---

**文档版本**：v1.1
**最后更新**：2026-02-15
**更新内容**：
- 修正问题描述：说明字幕地址被置空而非不返回
- 增加字幕类型说明（UP主上传字幕 vs AI生成字幕）
- 新增 Builder 模式设计方案
- 新增 Cookie 认证机制说明（SESSDATA、bili_jct、buvid3）
- 说明不支持 ac_time_value 字段的原因
