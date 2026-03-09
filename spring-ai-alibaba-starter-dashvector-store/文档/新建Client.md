本文介绍如何通过Java SDK新建一个DashVector Client。

**说明**

通过DashVector Client可连接DashVector服务端，进行Collection相关操作。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## 接口定义

Java

```
package com.aliyun.dashvector;

// 通过apiKey和endpoint构造
DashVectorClient(String apiKey, String endpoint);

// 通过DashVectorClientConfig构造
DashVectorClient(DashVectorClientConfig config);
```

## **使用示例**

**说明**

需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorClientConfig;
import com.aliyun.dashvector.common.DashVectorException;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        // 通过apiKey和endpoint构造
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
      
        // 通过Builder构造DashVectorClientConfig
        DashVectorClientConfig config = DashVectorClientConfig.builder()
            .apiKey("YOUR_API_KEY")
            .endpoint("YOUR_CLUSTER_ENDPOINT")
            .timeout(10f)
            .build();
        client = new DashVectorClient(config);
    }
}
```

## **入参描述**

可通过`DashVectorClientConfigBuilder`构造`DashVectorClientConfig`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **说明** |
| --- | --- | --- | --- |
| apiKey(String apiKey) | 是   | \\- | api-key |
| endpoint(String endpoint) | 是   | \\- | Cluster的Endpoint，可在控制台“Cluster详情”中查看 |
| timeout(Float timeout) | 否   | 10.0f | 超时时间（单位：秒），-1 代表不超时。 |
| build() | \\- | \\- | 构造`DashVectorClientConfig`对象 |

**说明**

endpoint参数，可在控制台[Cluster详情](https://help.aliyun.com/zh/document_detail/2568084.html#eed59512f3ign)中查看。

## 出参描述

**说明**

DashVectorClient初始化期间可能抛出`DashVectorException`异常，可通过具体异常信息分析初始化失败原因。