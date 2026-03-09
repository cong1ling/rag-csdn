本文介绍如何通过Java SDK向Collection中插入Doc。

**说明**

1.  插入Doc时若指定id已存在，已存在的Doc不会被覆盖，本次插入Doc操作无效。
    
2.  插入Doc时若不指定id，则在插入过程中会自动生成id，并在[返回结果](https://help.aliyun.com/zh/document_detail/2510262.html#a02ca8627cyna)中携带id信息。
    

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

// 同步接口
public Response<List<DocOpResult>> insert(InsertDocRequest insertDocRequest);

// 异步接口
public ListenableFuture<Response<List<DocOpResult>>> insertAsync(InsertDocRequest insertDocRequest);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

### 插入Doc

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorClientConfig;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.InsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;
import com.google.common.util.concurrent.ListenableFuture;

import java.util.*;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
        // 构建Vector
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
        
        // 构建Doc
      	Doc doc = Doc.builder().id("1").vector(vector).build();
        
        // 插入Doc
        Response<List<DocOpResult>> response = collection.insert(InsertDocRequest.builder().doc(doc).build());
        
        // 判断插入是否成功
      	// assert response.isSuccess() 
    }
}
```

### 插入不带有Id的Doc

Java

```
// 构建Vector
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
        
// 构建Doc
Doc doc = Doc.builder().vector(vector).build();
        
// 插入Doc
Response<List<DocOpResult>> response = collection.insert(InsertDocRequest.builder().doc(doc).build());        
```

### **插入带有Fields的Doc**

Java

```
// 构建Vector
Vector vector = Vector.builder().value(Arrays.asList(0.2f, 0.2f, 0.3f, 0.4f)).build();

// 插入单条数据，并设置Fields Value
Doc doc = Doc.builder()
  .id("2")
  .vector(vector)
  // 设置创建Collection时预定义的Fields Value
  .field("name", "zhangshan")
  .field("age", 20)
  .field("weight", 100f)
  .field("id", 1234567890l)
  .field("tags", Arrays.asList("hello", "world"))
  .field("numbers", Arrays.asList(1, 2, 3))
  .field("grades", Arrays.asList(1.1f, 2.2f, 3.3f))
  .field("bankCards", Arrays.asList(1L, 2L, 3L))
  // 设置Schema-Free的Field & Value
  .field("anykey1", "String")
  .field("anykey2", 1)
  .field("anykey3", true)
  .field("anykey4", 3.1415926f)
  .build();

// 插入Doc
Response<List<DocOpResult>> response = collection.insert(InsertDocRequest.builder().doc(doc).build());

// 判断插入Doc是否成功
assert response.isSuccess()
```

### **批量插入Doc**

Java

```
// 通过InsertDocRequest对象，批量插入10条Doc
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+3))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+3)).build())
    	.build()
  );
}

InsertDocRequest request = InsertDocRequest.builder().docs(docs).build();
Response<List<DocOpResult>> response = collection.insert(request);

// 判断插入是否成功
assert response.isSuccess();
```

### **异步插入Doc**

Java

```
// 异步批量插入10条数据
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+13))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+13)).build())
    	.build()
  );
}

InsertDocRequest request = InsertDocRequest.builder().docs(docs).build();
ListenableFuture<Response<List<DocOpResult>>> response = collection.insertAsync(request);

// 等待并获取异步insert结果
Response<List<DocOpResult>> ret = response.get();
```

### 插入带有Sparse Vector的Doc

Java

```
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

// 构建带有Sparse Vector的Doc
Doc doc = Doc.builder()
  .id("28")
  .sparseVector(
  new Map<Integer, Float>() {
    {
      put(1, 0.4f);
      put(10000, 0.6f);
      put(222222, 0.8f);
    }
  })
  .vector(vector)
  .build();

// 插入带有Sparse Vector的Doc
Response<List<DocOpResult>> response = collection.insert(InsertDocRequest.builder().doc(doc).build());
```

### 插入多向量Doc

Java

```
public void insert() {
    collection = client.get(collectionName);
    assert collection.isSuccess();
    SparseVector abstruct_vector = SparseVector.builder()
            .value(0, 0.3)
            .value(1, 0.232)
            .value(2, 0.4482)
            .value(3, 0.6672)
            .build();

    List<Doc> docs = new ArrayList<>();
    docs.add(Doc.builder().id("0")
            .vectors("title", Vector.builder().value(Collections.nCopies(4, 0.1f)).build())
            .vectors("content", Vector.builder().value(Collections.nCopies(6, 0.1f)).build())
            .sparseVectors("abstruct", abstruct_vector)
            .build());
    // 允许部分向量字段缺失
    docs.add(Doc.builder().id("1")
            .vectors("title", (Vector.builder().value(Collections.nCopies(4, 0.2f))).build())
            .build());
    docs.add(Doc.builder().id("2")
            .vectors("content", (Vector.builder().value(Collections.nCopies(6, 0.3f))).build())
            .build());
    InsertDocRequest insertRequest = InsertDocRequest.builder().docs(docs).build();
    Response<List<DocOpResult>> insertResponse = collection.insert(insertRequest);
    System.out.println(insertResponse);
    assert insertResponse.isSuccess();
}
```

**说明**

多向量Collection中，稠密向量+稀疏向量字段总共不能超过4条

## 入参描述

可通过`InsertDocRequestBuilder`构造`InsertDocRequest`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| docs(List<[Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)\\> docs) | 是   | \\- | 设置Doc列表 |
| doc([Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F) doc) | 追加Doc至Doc列表，可多次调用 |
| partition(String partition) | 否   | default | 分区名称 |
| build() | \\- | \\- | 构造`InsertDocRequest`对象 |

可通过`DocBuilder`构造`Doc`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| id(String id) | 否   | \\- | 主键  |
| vector(Vector vector) | 是   | \\- | 向量数据 |
| sparseVector(Map(Integer, Float)) | 否   | \\- | 稀疏向量 |
| fields(Map<String, Object>) | 否   | \\- | 设置Fields |
| field(String key, Object value) | 追加Field至Fields，可多次调用 |
| build() | \\- | \\- | 构造`Doc`对象 |

**说明**

1.  [Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)对象的fields参数，可自由设置“任意”的KeyValue数据，Key必须为`String`类型，Value必须为`String, Integer, Boolean, Float, Long, List<String>, List<Integer>, List<Float> or List<Long>`。
    
    1.  当Key在创建Collection时预先定义过，则Value的类型必须为预定义时的类型
        
    2.  当Key未在创建Collection时预先定义过，则Value的类型可为`String, Integer, Boolean or Float or Long`
        
2.  是否预先定义Fields，可参考[Schema Free](https://help.aliyun.com/zh/document_detail/2510228.html)。
    

## **出参描述**

**说明**

返回结果为`Response<List<DocOpResult>>`对象，`Response<List<DocOpResult>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **返回类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<[DocOpResult](https://help.aliyun.com/zh/document_detail/2510262.html#a02ca8627cyna)\\> | 返回插入Doc的结果 |     |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc插入请求，成功后返回实际消耗的写请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK在Collection中进行相似性检索。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

// 同步接口
public Response<List<Doc>> query(QueryDocRequest queryDocRequest);

// 异步接口
public ListenableFuture<Response<List<Doc>>> queryAsync(QueryDocRequest queryDocRequest);
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection，并参考[插入Doc](https://help.aliyun.com/zh/document_detail/2573586.html)提前插入部分数据。
    

### 根据向量进行相似性检索

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.QueryDocRequest;
import com.aliyun.dashvector.models.responses.Response;

import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
        
        // 构建Vector
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
      	
      	// 构建QueryDocRequest 
      	QueryDocRequest request = QueryDocRequest.builder()
          .vector(vector)
          .topk(100)
          .includeVector(true)
          .build();
        
        // 进行Doc检索
        Response<List<Doc>> response = collection.query(request);
      
      	// 判断请求是否成功
      	// assert response.isSuccess() 

        System.out.println(response);
        // example output:
        // {
        //   "code":0,
        //   "message":"Success",
        //   "requestId":"b26ce0b8-0caf-4836-8136-df889d79ae91",
        //   "output":[
        //     {
        //       "id":"1",
        //       "vector":{
        //         "value":[
        //           0.10000000149011612,
        //           0.20000000298023224,
        //           0.30000001192092896,
        //           0.4000000059604645
        //         ]
        //       },
        //       "fields":{
        //         "name":"zhangsan",
        //         "age":20,
        //         "weight":100.0,
        //         "anykey1":"String",
        //         "anykey2":1,
        //         "anykey3":true,
        //         "anykey4":3.1415926
        //       },
        //       "score":1.1920929E-7
        //     }
        //   ]
        // }
    }
}
```

### 根据主键（对应的向量）进行相似性检索

Java

```
// 构建QueryDocRequest 
QueryDocRequest request = QueryDocRequest.builder()
  .id("1")
  .topk(100)
  .outputFields(Arrays.asList("name", "age")) // 仅返回name、age这2个Field
  .includeVector(true)
  .build();

// 根据主键（对应的向量）进行相似性检索
Response<List<Doc>> response = collection.query(request);

// 判断检索是否成功
assert response.isSuccess()
```

### 带过滤条件的相似性检索

Java

```
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

// 构建QueryDocRequest 
QueryDocRequest request = QueryDocRequest.builder()
  .vector(vector) // 向量检索，也可设置主键检索
  .topk(100)
  .filter("age > 18") // 条件过滤，仅对age > 18的Doc进行相似性检索
  .outputFields(Arrays.asList("name", "age")) // 仅返回name、age这2个Field
  .includeVector(true)
  .build();

// 根据向量或者主键进行相似性检索 + 条件过滤
Response<List<Doc>> response = collection.query(request);

// 判断检索是否成功
assert response.isSuccess()
```

### 带有Sparse Vector的向量检索

**说明**

Sparse Vector（稀疏向量）可用于关键词权重表示，实现带[关键词感知能力的向量检索](https://help.aliyun.com/zh/document_detail/2586282.html)。

Java

```
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

// 构建带有Sparse Vector的QueryDocRequest
QueryDocRequest request = QueryDocRequest.builder()
  .vector(vector) // 向量检索
  .sparseVector(
  	new Map<Integer, Float>() {
      {
        put(1, 0.4f);
        put(10000, 0.6f);
        put(222222, 0.8f);
      }
  	}) // 稀疏向量
  .build();

// 根据向量进行相似性检索 + 稀疏向量
Response<List<Doc>> response = collection.query(request);

// 判断检索是否成功
assert response.isSuccess()
```

### 通过过滤条件进行匹配查询

Java

```
// 构建只携带过滤条件，不包含主键或向量的QueryDocRequest
QueryDocRequest request = QueryDocRequest.builder()
  .topk(100)
  .filter("age > 18") // 条件过滤，仅对age > 18的Doc进行相似性检索
  .outputFields(Arrays.asList("name", "age")) // 仅返回name、age这2个Field
  .includeVector(true)
  .build();

// 支持向量和主键都不传入，那么只进行条件过滤
Response<List<Doc>> response = collection.query(request);

// 判断检索是否成功
assert response.isSuccess()
```

### 向量检索高级参数

**说明**

详情可参考 [向量检索高级参数](https://help.aliyun.com/zh/document_detail/2838122.html)。

Java

```
public void queryParameter() {
    assert collection.isSuccess();
    // 构建Vector
    Vector queryVector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
    VectorQuery vectorQuery = VectorQuery.builder().vector(queryVector).ef(1000).linear(false).radius(100.0F).build();
    QueryDocRequest request = QueryDocRequest.builder().vectorQuery(vectorQuery).build();
    Response<List<Doc>> response = collection.query(request);
    System.out.println(response);
    assert response.isSuccess();
}
```

### **多向量检索**

**说明**

详情可参考 [多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html)。

```
SparseVector abstruct_vector = SparseVector.builder()
            .value(0L, 0.4f)
            .value(1L, 0.6f)
            .value(2L, 0.8f)
            .build();
QueryDocRequest request = QueryDocRequest.builder()
        .vectors("title", VectorQuery.builder().vector(Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build()).numCandidates(3).build())
        .vectors("content", VectorQuery.builder().vector(Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f, 0.5f, 0.6f)).build()).numCandidates(3).build())
        .sparseVectors("abstruct", SparseVectorQuery.builder().vector(abstruct_vector).build())
        // 加权融合排序
        .ranker(WeightedRanker.builder().weights(new HashMap<String, Float>() {{
            put("title", 10000.0F);
            put("content", 2.0F);
                }})
                .build())
        // 倒数秩融合排序
        // .ranker(RrfRanker.builder().rankConstant(100).build())
        .topk(1)
        .build();
Response<List<Doc>> response = collection.query(request);
```

#### 使用多向量的一个向量执行检索

```
public void querySingleVector() {
    VectorQuery vectorQuery = VectorQuery.builder().vector(Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build())
            .ef(1000)
            .radius(100.0F)
            .linear(false)
            .build();
    QueryDocRequest request = QueryDocRequest.builder()
            .vector("title", vectorQuery)
            .topk(10)
            .build();
    Response<List<Doc>> response = collection.query(request);
    System.out.println(response);
    assert response.isSuccess();
}
```

## 入参描述

可通过`QueryDocRequestBuilder`构造`QueryDocRequest`对象，其可用方法如下表所示：

**说明**

`vector`和`id`两个入参需要二选一使用，如都不传入，则仅完成条件过滤。

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| vector(Vector vector) | 否   | \\- | 向量数据 |
| sparseVector(Map(Integer, Float)) | 否   | \\- | 稀疏向量 |
| id(String id) | 否   | \\- | 主键，表示根据主键对应的向量进行相似性检索 |
| topk(int topk) | 否   | 10  | 返回topk相似性结果 |
| filter(String filter) | 否   | \\- | 过滤条件，需满足SQL where子句规范，详见[条件过滤检索](https://help.aliyun.com/zh/document_detail/2513006.html) |
| includeVector(bool includeVector) | 否   | false | 是否返回向量数据 |
| partition(String partition) | 否   | default | 分区名称 |
| outputFields(List<String> outputFields) | 否   | \\- | 返回文档字段列表，默认返回所有文档字段 |
| outputField(String field) | 否   | \\- |
| vectors(Map<String, VectorQuery>) | 否   | \\- | 多向量检索，详情参考[多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html#t2746140.html) |
| ranker(Ranker) | 否   | \\- | 融合排序参数，详情参考[多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html) |
| vectorQuery(VectorQuery) | 否   | \\- | 使用VectorQuery执行高级检索，详情参考[向量检索高级参数](https://help.aliyun.com/zh/document_detail/2838122.html) |
| sparseVectorQuery(SparseVectorQuery) | 否   | \\- | 使用SparseVectorQuery执行稀疏向量检索，详情参考VectorQuery |
| build() | \\- | \\- | 构造`QueryDocRequest`对象 |

## **出参描述**

**说明**

返回结果为`Response<List<Doc>>`对象，`Response<List<Doc>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **返回类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<Doc> | 返回相似性检索结果, Doc 参见[Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F) | ``` [ { "id":"9", "vector":{"value":[0.9,0.9,0.9,0.9]}, "fields":{"name":"java_9","age":9}, "score":90 } ] ``` |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc检索请求，成功后返回实际消耗的读请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK在Collection中按分组进行相似性检索。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## 接口定义

Java(JSP)

```
// class DashVectorCollection

// 同步接口
public Response<List<Group>> queryGroupBy(QueryDocGroupByRequest queryDocGroupByRequest);

// 异步接口
public ListenableFuture<Response<List<Group>>> queryGroupByAsync(QueryDocGroupByRequest queryDocGroupByRequest)
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    

### **根据向量进行分组相似性检索**

Java

```
package com.aliyun.dashvector;

import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Group;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.CreateCollectionRequest;
import com.aliyun.dashvector.models.requests.InsertDocRequest;
import com.aliyun.dashvector.models.requests.QueryDocGroupByRequest;
import com.aliyun.dashvector.models.responses.Response;
import com.aliyun.dashvector.proto.CollectionInfo;
import com.aliyun.dashvector.proto.FieldType;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        DashVectorClientConfig config =
                DashVectorClientConfig.builder()
                        .apiKey("YOUR_API_KEY")
                        .endpoint("YOUR_CLUSTER_ENDPOINT")
                        .timeout(30f)
                        .build();
        DashVectorClient client = new DashVectorClient(config);

        CreateCollectionRequest request = CreateCollectionRequest.builder()
                .name("group_demo")
                .dimension(4)
                .metric(CollectionInfo.Metric.dotproduct)
                .dataType(CollectionInfo.DataType.FLOAT)
                .filedSchema("document_id", FieldType.STRING)
                .filedSchema("chunk_id", FieldType.INT)
                .build();

        Response<Void> createResponse = client.create(request);
        assert createResponse.isSuccess();

        DashVectorCollection collection = client.get("group_demo");
        assert collection.isSuccess();

        List<Doc> docs = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            int finalI = i;
            docs.add(
                    Doc.builder()
                            .id(Integer.toString(i+3))
                            .vector(Vector.builder().value(Collections.nCopies(4, (float) i+3)).build())
                            .fields(
                                    new HashMap<String, Object>() {
                                        {
                                            put("document_id", "paper" + finalI % 3);
                                            put("chunk_id", finalI);
                                        }
                                    }
                            )
                            .build());
        }

        InsertDocRequest insertRequest = InsertDocRequest.builder().docs(docs).build();
        Response<Void> insertResponse = collection.insert(insertRequest);
        assert insertResponse.isSuccess();



        // 构建Vector
        Vector queryVector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

        // 构建 QueryDocGroupByRequest
        QueryDocGroupByRequest queryDocGroupByRequest =
                QueryDocGroupByRequest.builder().includeVector(true).vector(queryVector)
                        .groupByField("document_id")
                        .groupCount(2)
                        .groupTopk(3)
                        .build();

        // 进行分组Doc检索
        Response<List<Group>> response = collection.queryGroupBy(queryDocGroupByRequest);

        // 判断是否成功
        assert response.isSuccess();

        System.out.println(response);
        // example output:
        // {
        //     "code": 0,
        //     "message": "Success",
        //     "requestId": "47404048-6f40-47ad-9d62-2675704afb26",
        //     "output": [
        //         {
        //             "groupId": "paper2",
        //             "docs": [
        //                 {
        //                     "id": "8",
        //                     "vector": {
        //                         "value": [
        //                             8.0,
        //                             8.0,
        //                             8.0,
        //                             8.0
        //                         ]
        //                     },
        //                     "fields": {
        //                         "document_id": "paper2",
        //                         "chunk_id": 5
        //                     },
        //                     "score": 8.0,
        //                     "sparseVector": {
        //
        //                     }
        //                 },
        //                 {
        //                     "id": "5",
        //                     "vector": {
        //                         "value": [
        //                             5.0,
        //                             5.0,
        //                             5.0,
        //                             5.0
        //                         ]
        //                     },
        //                     "fields": {
        //                         "document_id": "paper2",
        //                         "chunk_id": 2
        //                     },
        //                     "score": 5.0,
        //                     "sparseVector": {
        //
        //                     }
        //                 }
        //             ]
        //         },
        //         {
        //             "groupId": "paper1",
        //             "docs": [
        //                 {
        //                     "id": "7",
        //                     "vector": {
        //                         "value": [
        //                             7.0,
        //                             7.0,
        //                             7.0,
        //                             7.0
        //                         ]
        //                     },
        //                     "fields": {
        //                         "document_id": "paper1",
        //                         "chunk_id": 4
        //                     },
        //                     "score": 7.0,
        //                     "sparseVector": {
        //
        //                     }
        //                 },
        //                 {
        //                     "id": "4",
        //                     "vector": {
        //                         "value": [
        //                             4.0,
        //                             4.0,
        //                             4.0,
        //                             4.0
        //                         ]
        //                     },
        //                     "fields": {
        //                         "document_id": "paper1",
        //                         "chunk_id": 1
        //                     },
        //                     "score": 4.0,
        //                     "sparseVector": {
        //
        //                     }
        //                 }
        //             ]
        //         }
        //     ]
        // }
    }
}
```

### **根据主键（对应的向量）进行分组相似性检索**

Java

```
        // 构建QueryDocGroupByRequest
        QueryDocGroupByRequest request = QueryDocGroupByRequest.builder()
                .groupByField("age")          
                .id("1")
                .groupCount(10)  // 返回10个分组
                .groupTopk(10)   // 每个分组最多返回10个doc
                .outputFields(Arrays.asList("name", "age")) // 仅返回name、age这2个Field
                .includeVector(true)
                .build();

        // 根据主键（对应的向量）进行分组相似性检索
        Response<List<Group>> response2 = collection.queryGroupBy(request);

        // 判断检索是否成功
        assert response2.isSuccess();
```

### **带过滤条件的分组相似性检索**

Java

```
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

        // 构建QueryDocGroupByRequest
        QueryDocGroupByRequest request = QueryDocGroupByRequest.builder()
                .groupByField("age")          
                .vector(vector)
                .filter("age > 18") // 条件过滤，仅对age > 18的Doc进行相似性检索
                .outputFields(Arrays.asList("name", "age")) // 仅返回name、age这2个Field
                .includeVector(true)
                .build();

        // 根据向量或者主键进行分组相似性检索 + 条件过滤
        Response<List<Group>> response = collection.queryGroupBy(request);

        // 判断检索是否成功
        assert response.isSuccess();
```

### 带有Sparse Vector的分组向量检索

**说明**

Sparse Vector（稀疏向量）可用于关键词权重表示，实现带[关键词感知能力的向量检索](https://help.aliyun.com/zh/document_detail/2586282.html)。

Java

```
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

        // 构建QueryDocGroupByRequest
        QueryDocGroupByRequest request = QueryDocGroupByRequest.builder()
                .groupByField("age")          
                .vector(vector)
                .sparseVector(
                        new Map<Integer, Float>() {
                            {
                                put(1, 0.4f);
                                put(10000, 0.6f);
                                put(222222, 0.8f);
                            }
                        }) // 稀疏向量
                .build();

        //根据向量进行分组相似性检索 + 稀疏向量
        Response<List<Group>> response = collection.queryGroupBy(request);

        // 判断检索是否成功
        assert response.isSuccess();
```

## **入参描述**

可通过`QueryDocGroupByRequest.builder()`构造`QueryDocGroupByRequest`对象，其可用方法如下表所示：

**说明**

`vector`和`id`两个入参需要二选一使用，并保证其中一个不为空。

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| groupByField( String groupByField ) | 是   | \\- | 按指定字段的值来分组检索，目前不支持schema-free字段。 |
| groupCount(int groupCount) | 否   | 10  | 最多返回的分组个数，尽力而为参数，一般可以返回groupCount个分组。 |
| groupTopk(int groupTopk) | 否   | 1   | 每个分组返回groupTopk条相似性结果，尽力而为参数，优先级低于groupCount。 |
| vector(Vector vector) | 否   | \\- | 向量数据 |
| sparseVector(Map(Integer, Float)) | 否   | \\- | 稀疏向量 |
| id(String id) | 否   | \\- | 主键，表示根据主键对应的向量进行相似性检索 |
| filter(String filter) | 否   | \\- | 过滤条件，需满足SQL where子句规范，[详见](https://help.aliyun.com/zh/document_detail/2513006.html) |
| includeVector(bool includeVector) | 否   | false | 是否返回向量数据 |
| partition(String partition) | 否   | default | 分区名称 |
| outputFields(List<String> outputFields) | 否   | \\- | 返回文档字段列表，默认返回所有文档字段 |
| outputField(String field) | 否   | \\- |
| vectorField(String) | 否   | \\- | 使用[多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html#t2746140.html)的一个向量执行分组检索。 |
| build() | \\- | \\- | 构造`QueryDocRequest`对象 |

## **出参描述**

**说明**

返回结果为`Response<List<Group>>`对象，其中可获取本次操作结果信息，如下表所示。

| **方法** | **返回类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<[Group](https://help.aliyun.com/zh/document_detail/2510262.html#9dcb140036xr8)\\> | 返回分组相似性检索结果 | ``` { "groupId": "20", "docs": [ { "id": "2", "vector": { "value": [ 0.475044, 0.906511, 0.60797, 0.573515 ] }, "fields": { "name": "lisi", "weight": 45.0, "male": false, "age": 20 }, "score": 0.6406033, "sparseVector": { } } ] } ``` |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK向Collection中插入或更新Doc。

**说明**

1.  若调用本接口时Doc Id已存在，则等同于[更新Doc](https://help.aliyun.com/zh/document_detail/2573594.html)；Doc Id不存在，则等同于[插入Doc](https://help.aliyun.com/zh/document_detail/2573586.html)。
    
2.  若调用本接口时不指定Doc Id，则等同于[插入Doc](https://help.aliyun.com/zh/document_detail/2573586.html)，DashVector会自动生成Doc Id，并在返回结果中携带id信息。
    

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## 接口定义

Java

```
// class DashVectorCollection

// 同步接口
public Response<List<DocOpResult>> upsert(UpsertDocRequest upsertDocRequest);

// 异步接口
public ListenableFuture<Response<List<DocOpResult>>> upsertAsync(UpsertDocRequest upsertDocRequest);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

### **插入或更新Doc**

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.UpsertDocRequest;
import com.aliyun.dashvector.models.responses.Response;

import java.util.*;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
        // 构建Vector
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
        
        // 构建Doc
      	Doc doc = Doc.builder().id("1").vector(vector).build();
        
        // 插入或更新Doc
        Response<List<DocOpResult>> response = collection.upsert(UpsertDocRequest.builder().doc(doc).build());
        
        // 判断插入或更新Doc是否成功
      	// assert response.isSuccess() 
    }
}
```

### 插入或更新不带有Id的Doc

Java

```
// 构建Vector
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
        
// 构建Doc
Doc doc = Doc.builder().vector(vector).build();
        
// 插入或更新Doc
Response<List<DocOpResult>> response = collection.upsert(UpsertDocRequest.builder().doc(doc).build());
```

### 插入或更新带有Fields的Doc

Java

```
// 构建Vector
Vector vector = Vector.builder().value(Arrays.asList(0.2f, 0.2f, 0.3f, 0.4f)).build();

// 插入或更新单条数据，并设置Fields Value
Doc doc = Doc.builder()
  .id("2")
  .vector(vector)
  // 设置创建Collection时预定义的Fields Value
  .field("name", "zhangshan")
  .field("age", 20)
  .field("weight", 100f)
  .field("id", 1234567890l)
  .field("tags", Arrays.asList("hello", "world"))
  .field("numbers", Arrays.asList(1, 2, 3))
  .field("grades", Arrays.asList(1.1f, 2.2f, 3.3f))
  .field("bankCards", Arrays.asList(1L, 2L, 3L ,4L))
  // 设置Schema-Free的Field & Value
  .field("anykey1", "String")
  .field("anykey2", 1)
  .field("anykey3", true)
  .field("anykey4", 3.1415926f)
  .build();

// 插入或更新Doc
Response<List<DocOpResult>> response = collection.upsert(UpsertDocRequest.builder().doc(doc).build());

// 判断插入或更新Doc是否成功
assert response.isSuccess()
```

### 批量插入或更新Doc

Java

```
// 通过UpsertDocRequest对象，批量插入或更新10条Doc
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+3))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+3)).build())
    	.build()
  );
}

UpsertDocRequest request = UpsertDocRequest.builder().docs(docs).build();
Response<List<DocOpResult>> response = collection.upsert(request);

// 判断插入或更新Doc是否成功
assert response.isSuccess();
```

### 异步插入或更新Doc

Java

```
// 异步批量插入或更新10条数据
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+13))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+13)).build())
    	.build()
  );
}

UpsertDocRequest request = UpsertDocRequest.builder().docs(docs).build();
ListenableFuture<Response<List<DocOpResult>>> response = collection.upsertAsync(request);

// 等待并获取异步upsert结果
Response<List<DocOpResult>> ret = response.get();
```

### 插入或更新带有Sparse Vector的Doc

Java

```
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

// 构建带有Sparse Vector的Doc
Doc doc = Doc.builder()
  .id("28")
  .sparseVector(
  new Map<Integer, Float>() {
    {
      put(1, 0.4f);
      put(10000, 0.6f);
      put(222222, 0.8f);
    }
  })
  .vector(vector)
  .build();

// 插入或更新带有Sparse Vector的Doc
Response<List<DocOpResult>> response = collection.upsert(UpsertDocRequest.builder().doc(doc).build());
```

## 入参描述

使用`UpsertDocRequestBuilder`构造`UpsertDocRequest`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| docs(List<[Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)\\> docs) | 是   | \\- | 设置Doc列表 |
| doc([Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F) doc) | 追加Doc至Doc列表，可多次调用 |
| partition(String partition) | 否   | default | 分区名称 |
| build() | \\- | \\- | 构造`UpsertDocRequest`对象 |

使用`DocBuilder`构造`Doc`对象，其可用方法如下：

| **方法** | **选项** | **默认值** | **描述** |
| --- | --- | --- | --- |
| id(String id) | 否   | \\- | 主键  |
| vector(Vector vector) | 是   | \\- | 向量数据 |
| sparseVector(Map(Integer, Float)) | 否   | \\- | 稀疏向量 |
| fields(Map<String, Object>) | 否   | \\- | 设置Fields |
| field(String key, Object value) | 追加Field至Fields，可多次调用 |
| build() | \\- | \\- | 构造`Doc`对象 |

**说明**

1.  [Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)对象的fields参数，可自由设置“任意”的KeyValue数据，Key必须为`String`类型，Value必须为`String, Integer, Boolean, Float, Long, List<String>, List<Integer>, List<Float> or List<Long>`。
    
    1.  当Key在创建Collection时预先定义过，则Value的类型必须为预定义时的类型
        
    2.  当Key未在创建Collection时预先定义过，则Value的类型可为`String, Integer, Boolean or Float`
        
2.  是否预先定义Fields，可参考[Schema Free](https://help.aliyun.com/zh/document_detail/2510228.html)。
    

## **出参描述**

**说明**

返回结果为`Response<List<DocOpResult>>`对象，`Response<List<DocOpResult>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<[DocOpResult](https://help.aliyun.com/zh/document_detail/2510262.html#a02ca8627cyna)\\> | 返回插入或更新Doc的结果 |     |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc插入或删除请求，成功后返回实际消耗的写请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK更新Collection中已存在的Doc。

**说明**

1.  若更新Doc时指定id不存在，则本次更新Doc操作无效
    
2.  如只更新部分属性fields，其他未更新属性fields默认被置为`null`
    
3.  **Java SDK 1.0.10版本后，更新Doc时vector变为非必填项**
    

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

// 同步接口
public Response<List<DocOpResult>> update(UpdateDocRequest updateDocRequest);

// 异步接口
public ListenableFuture<Response<List<DocOpResult>>> updateAsync(UpdateDocRequest updateDocRequest); 
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

### **更新Doc**

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.Vector;
import com.aliyun.dashvector.models.requests.UpdateDocRequest;
import com.aliyun.dashvector.models.responses.Response;

import java.util.*;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
        // 构建Vector
        Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();
        
        // 构建Doc
      	Doc doc = Doc.builder().id("1").vector(vector).build();
        
        // 更新Doc
        Response<List<DocOpResult>> response = collection.update(UpdateDocRequest.builder().doc(doc).build());
        
        // 判断更新Doc是否成功
      	// assert response.isSuccess() 
    }
}
```

### 更新带有Fields的Doc

Java

```
// 构建Vector
Vector vector = Vector.builder().value(Arrays.asList(0.2f, 0.2f, 0.3f, 0.4f)).build();

// 更新单条数据，并设置Fields Value
Doc doc = Doc.builder()
  .id("2")
  .vector(vector)
  // 设置创建Collection时预定义的Fields Value
  .field("name", "zhangshan")
  .field("age", 20)
  .field("weight", 100f)
  .field("id", 1234567890l)
  .field("tags", Arrays.asList("hello", "world"))
  .field("numbers", Arrays.asList(1, 2, 3, 4, 5))
  .field("grades", Arrays.asList(1.1f, 2.2f, 3.3f))
  .field("bankCards", Arrays.asList(1L, 2L, 3L, 5L))
  // 设置Schema-Free的Field & Value
  .field("anykey1", "String")
  .field("anykey2", 1)
  .field("anykey3", true)
  .field("anykey4", 3.1415926f)
  .build();

// 更新Doc
Response<List<DocOpResult>> response = collection.update(UpdateDocRequest.builder().doc(doc).build());

// 判断更新Doc是否成功
assert response.isSuccess()
```

### 批量更新Doc

Java

```
// 通过UpdateDocRequest对象，批量更新10条Doc
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+3))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+3)).build())
    	.build()
  );
}

UpdateDocRequest request = UpdateDocRequest.builder().docs(docs).build();
Response<List<DocOpResult>> response = collection.update(request);

// 判断批量更新Doc是否成功
assert response.isSuccess();
```

### 异步更新Doc

Java

```
// 异步批量更新10条数据
List<Doc> docs = new ArrayList<>();
for (int i = 0; i < 10; i++) {
  docs.add(
    Doc.builder()
    	.id(Integer.toString(i+13))
    	.vector(Vector.builder().value(Collections.nCopies(4, (float) i+13)).build())
    	.build()
  );
}

UpdateDocRequest request = UpdateDocRequest.builder().docs(docs).build();
ListenableFuture<Response<List<DocOpResult>>> response = collection.updateAsync(request);

// 等待并获取异步update结果
Response<List<DocOpResult>> ret = response.get();
```

### 更新带有Sparse Vector的Doc

Java

```
Vector vector = Vector.builder().value(Arrays.asList(0.1f, 0.2f, 0.3f, 0.4f)).build();

// 构建带有Sparse Vector的Doc
Doc doc = Doc.builder()
  .id("28")
  .sparseVector(
  new Map<Integer, Float>() {
    {
      put(1, 0.4f);
      put(10000, 0.6f);
      put(222222, 0.8f);
    }
  })
  .vector(vector)
  .build();

// 更新Doc
Response<List<DocOpResult>> response = collection.update(UpdateDocRequest.builder().doc(doc).build());
```

## 入参描述

使用`UpdateDocRequestBuilder`构造`UpdateDocRequest`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| docs(List<[Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)\\> docs) | 是   | \\- | 设置Doc列表 |
| doc([Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F) doc) | 追加Doc至Doc列表，可多次调用 |
| partition(String partition) | 否   | default | 分区名称 |
| build() | \\- | \\- | 构造`UpdateDocRequest`对象 |

使用`DocBuilder`构造`Doc`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| id(String id) | 是   | \\- | 主键  |
| vector(Vector vector) | 否   | \\- | 向量数据 |
| sparseVector(Map(Integer, Float)) | 否   | \\- | 稀疏向量 |
| fields(Map<String, Object>) | 否   | \\- | 设置Fields |
| field(String key, Object value) | 追加Field至Fields，可多次调用 |
| build() | \\- | \\- | 构造`Doc`对象 |

**说明**

1.  [Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)对象的fields参数，可自由设置“任意”的KeyValue数据，Key必须为`String`类型，Value必须为`String, Integer, Boolean, Float, Long, List<String>, List<Integer>, List<Float> or List<Long>`。
    
    1.  当Key在创建Collection时预先定义过，则Value的类型必须为预定义时的类型
        
    2.  当Key未在创建Collection时预先定义过，则Value的类型可为`String, Integer, Boolean or Float`
        
2.  是否预先定义Fields，可参考[Schema Free](https://help.aliyun.com/zh/document_detail/2510228.html)。
    

## **出参描述**

**说明**

返回结果为`Response<List<DocOpResult>>`对象，`Response<List<DocOpResult>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<[DocOpResult](https://help.aliyun.com/zh/document_detail/2510262.html#a02ca8627cyna)\\> | 返回更新Doc的结果 |     |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc更新请求，成功后返回实际消耗的写请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK，根据id或id列表获取Collection中已存在的Doc。

**说明**

如果指定id不存在，则该id对应的Doc为空。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

// 同步接口
public Response<Map<String, Doc>> fetch(FetchDocRequest fetchDocRequest);

// 异步接口
public ListenableFuture<Response<Map<String, Doc>>> fetchAsync(FetchDocRequest fetchDocRequest);
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection，并参考[插入Doc](https://help.aliyun.com/zh/document_detail/2573586.html)提前插入部分数据。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.Doc;
import com.aliyun.dashvector.models.requests.FetchDocRequest;
import com.aliyun.dashvector.models.responses.Response;

import java.util.Map;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");

        // 构建 FetchDocRequest
        FetchDocRequest request = FetchDocRequest.builder()
            .id("1")
            .build();

        // 发送获取Doc请求
        Response<Map<String, Doc>> response = collection.fetch(request);

        System.out.println(response);
        // example output:
        // {
        //     "code":0,
        //     "message":"Success",
        //     "requestId":"489c5cda-3ffc-4171-b6e0-1837b932962b",
        //     "output":{
        //         "1":{
        //             "id":"1",
        //             "vector":{"value":[0.1,0.2,0.3,0.4]},
        //             "fields":{
        //                 "name":"zhangsan",
        //                 "age":20,
        //                 "weight":100.0,
        //                 "anykey1":"String",
        //                 "anykey2":1,
        //                 "anykey3":true,
        //                 "anykey4":3.1415926
        //             },
        //             "score":0
        //         }
        //     }
        // }
    }
}
```

## 入参描述

通过`FetchDocRequestBuilder`构造`FetchDocRequest`对象，其可用方法如下表所示：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| ids(List<String> ids) | 是   | \\- | 文档主键列表 |
| id(String id) | 否   | \\- |
| partition(String partition) | 否   | default | 分区名称 |
| build() | \\- | \\- | 构造`FetchDocRequest`对象 |

## **出参描述**

**说明**

返回结果为`Response<Map<String, Doc>>`对象，`Response<Map<String, Doc>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | Map<String, Doc> | key为主键，value为对应[Doc](https://help.aliyun.com/zh/document_detail/2510262.html#IUn5F)的Map | ``` { "1":{ "id":"1", "vector":{"value":[0.1,0.2,0.3,0.4]}, "fields":{"name":"zhangsan"}, "score":0 } } ``` |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc获取请求，成功后返回实际消耗的读请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK，根据id或id列表删除Collection中已存在的Doc。

**说明**

如果指定id不存在，则删除对应Doc的操作无效。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

// 同步接口
public Response<List<DocOpResult>> delete(DeleteDocRequest deleteDocRequest);

// 异步接口
public ListenableFuture<Response<List<DocOpResult>>> deleteAsync(DeleteDocRequest deleteDocRequest);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection，并参考[插入Doc](https://help.aliyun.com/zh/document_detail/2573586.html)提前插入部分数据。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.requests.DeleteDocRequest;
import com.aliyun.dashvector.models.responses.Response;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");

        // 构建 DeleteDocRequest
        DeleteDocRequest request = DeleteDocRequest.builder()
            .id("1")
            .build();

        // 发送删除Doc请求
        Response<List<DocOpResult>> response = collection.delete(request);
    }
}
```

## 入参描述

通过`DeleteDocRequestBuilder`构造`DeleteDocRequest`对象，其可用方法如下表所示：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| ids(List<String> ids) | 是   | \\- | 文档主键列表 |
| id(String id) | 否   | \\- |
| partition(String partition) | 否   | default | 分区名称 |
| deleteAll(Boolean deleteAll) | 否   | false | 是否清除分区内的全部数据。当传入`true`时，要求`ids`为空 |
| build() | \\- | \\- | 构造`DeleteDocRequest`对象 |

## **出参描述**

**说明**

返回结果为`Response<List<DocOpResult>>`对象，`Response<List<DocOpResult>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<[DocOpResult](https://help.aliyun.com/zh/document_detail/2510262.html#a02ca8627cyna)\\> | 返回删除的结果 |     |
| getUsage() | [RequestUsage](https://help.aliyun.com/zh/document_detail/2510262.html#55f8426cfalxo) | 对Serverless实例（按量付费）集合的Doc删除请求，成功后返回实际消耗的写请求单元数 |     |
| isSuccess() | Boolean | 判断请求是否成功 | true |


