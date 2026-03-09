本文介绍如何通过Java SDK创建一个新的Collection。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

// 通过名称和向量维度进行创建Collection
public Response<Void> create(String name, int dimension);

// 通过CreateCollectionRequest创建Collection
public Response<Void> create(CreateCollectionRequest request);
```

## 使用示例

**说明**

需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。

### 创建单向量集合

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.requests.CreateCollectionRequest;
import com.aliyun.dashvector.models.responses.Response;
import com.aliyun.dashvector.proto.CollectionInfo;
import com.aliyun.dashvector.proto.FieldType;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");

        // 通过CreateCollectionRequest创建Collection
      
        // 创建一个名称为quickstart、向量维度为4、
        // 向量数据类型为float（默认）、
        // 距离度量方式为dotproduct（内积）的Collection
        // 并预先定义多个Field，名称为name、weight、age、id、tags、numbers、grades、bankCards，数据类型分别为str、float、int、long、ARRAY_STRING、ARRAY_INT、ARRAY_FLOAT、ARRAY_LONG
        CreateCollectionRequest request = CreateCollectionRequest.builder()
            .name("quickstart")
            .dimension(4)
            .metric(CollectionInfo.Metric.dotproduct)
            .dataType(CollectionInfo.DataType.FLOAT)
            .filedSchema("name", FieldType.STRING)
            .filedSchema("weight", FieldType.FLOAT)
            .filedSchema("age", FieldType.INT)
            .filedSchema("id", FieldType.LONG)
            .filedSchema("tags", FieldType.ARRAY_STRING)
            .filedSchema("numbers", FieldType.ARRAY_INT)
            .filedSchema("grades", FieldType.ARRAY_FLOAT)
            .filedSchema("bankCards", FieldType.ARRAY_LONG)
            .build();
      
        Response<Void> response = client.create(request);

        System.out.println(response);
        // example output:
        // {"code":0,"message":"","requestId":"883716a3-32f8-4220-ae54-245fa9b87bf0"}
    }
}
```

### **创建多向量集合**

```
public void createCollection() {
    CreateCollectionRequest createCollectionRequest = CreateCollectionRequest.builder()
      .name("multi_vector_demo")
      .vectors("title", VectorParam.builder().dimension(4).quantizeType("DT_VECTOR_INT8").build())
      .vectors("content", VectorParam.builder().dimension(6).metric(CollectionInfo.Metric.euclidean).build())
      .sparseVectors("abstruct", VectorParam.builder().metric(CollectionInfo.Metric.dotproduct).build())
      .sparseVectors("keywords", VectorParam.builder().metric(CollectionInfo.Metric.dotproduct).build())
      // 稀疏向量索引目前仅支持内积度量，dimension/dtype使用默认值无需设置
      .filedSchema("author", FieldType.STRING)
      .filedSchema("tags", FieldType.ARRAY_STRING)
      .build();

    Response<Void> createResponse = client.create(request);
    System.out.println(createResponse);
    assert createResponse.isSuccess();
}
```

## **入参描述**

可通过`CreateCollectionRequestBuilder`构造 `CreateCollectionRequest`对象，其可用方法如下：

| **方法** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- |
| name(String name) | 是   | \\- | 待创建的集合名称 |
| dimension(int dimension) | 是   | \\- | 向量维度 |
| dataType(CollectionInfo.DataType dataType) | 否   | DataType.FLOAT | 向量数据类型，支持 - `DataType.INT` - `DataType.FLOAT` |
| fieldsSchema(Map<String, FieldType> fieldsSchem) | 否   | \\- | Fields定义，Field类型支持 - `FieldType.BOOL` - `FieldType.STRING` - `FieldType.INT` - `FieldType.FLOAT` - `FieldType.LONG` - `FieldType.ARRAY_STRING` - `FieldType.ARRAY_INT` - `FieldType.ARRAY_FLOAT` - `FieldType.ARRAY_LONG` |
| fieldSchema(String key, FieldType value) | 否   | \\- |
| metric(CollectionInfo.Metric metric) | 否   | Metric.cosine | 距离度量支持 - `Metric.cosine` - `Metric.euclidean` - `Metric.dotproduct` metric为`cosine`时，`datatype`必须为`FLOAT` |
| extraParams(Map<String, String> params) | 否   | \\- | 可选参数： - quantize\\_type：量化策略，详情参考[向量动态量化](https://help.aliyun.com/zh/document_detail/2663745.html) |
| timeout(Integer timeout) | 否   | \\- | - `timeout == null`：接口开启同步，待Collection 创建成功后返回 - `timeout == -1`：接口开启异步 - `timeout >= 0`：接口开启同步并等待，若规定时间Collection未创建成功，则返回超时 |
| vectorParam(VectorParam) | 否   | \\- | 设置向量字段的高级参数，如开启量化，详情参考[VectorParam](https://help.aliyun.com/zh/document_detail/2510262.html#9eb8f67036rec) |
| vectors(Map<String, VectorParam>) | 否   | \\- | 定义多向量集合，详情参考[多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html) |
| sparseVectors(Map<String, VectorParam>) | 否   | \\- | 定义稀疏多向量集合，详情参考[多向量检索](https://help.aliyun.com/zh/document_detail/2837745.html) |
| build() | \\- | \\- | 构造 `CreateCollectionRequest`对象 |

**说明**

-   创建Collection时预先定义Fields的收益见[Schema Free](https://help.aliyun.com/zh/document_detail/2510228.html)
    
-   量化策略详情可参考[向量动态量化](https://help.aliyun.com/zh/document_detail/2663745.html)
    

## **出参描述**

**说明**

返回结果为`Response<Void>`对象，`Response<Void>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取已创建的Collection的状态和Schema信息。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

public Response<CollectionMeta> describe(String name);
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.CollectionMeta;
import com.aliyun.dashvector.models.responses.Response;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");

        Response<CollectionMeta> response = client.describe("quickstart");

        System.out.println(response);
        // example output:
        // {
        //     "code":0,
        //     "message":"",
        //     "requestId":"cb468965-d86b-405a-87a4-a596e61c1240",
        //     "output":{
        //         "name":"quickstart",
        //         "dimension":4,
        //         "dataType":"FLOAT",
        //         "metric":"dotproduct",
        //         "status":"SERVING",
        //         "fieldsSchema":{
        //             "name":"STRING",
        //             "weight":"FLOAT",
        //             "age":"INT", 
        //             "id":"LONG"
        //         },
        //         "partitionStatus":{
        //             "default":"SERVING"
        //         }
        //     }
        // }
    }
}
```

## 入参描述

| **参数** | **类型** | **必填** | **默认值** | **说明** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | 已创建集合的名称 |

## **出参描述**

**说明**

返回结果为`Response<CollectionMeta>`对象，`Response<CollectionMeta>`对象中可获取本次操作结果信息，如下表所示。

| **字段** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | CollectionMeta | 参考[CollectionMeta](https://help.aliyun.com/zh/document_detail/2510262.html#c56eb3c05bx05) | ``` { "name":"quickstart", "dimension":4, "dataType":"FLOAT", "metric":"dotproduct", "status":"SERVING", "fieldsSchema":{ "name":"STRING", "weight":"FLOAT", "age":"INT", "id":"LONG" }, "partitionStatus":{ "default":"SERVING" } } ``` |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取已创建的Collection对象。

**说明**

通过Collection对象，后续可进行Doc相关操作，如插入Doc、检索Doc、管理Partition等

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

public DashVectorCollection get(String name);
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;

public class Main {
    public static void main(String[] args) throws DashVectorException {
      	DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        
      	DashVectorCollection collection = client.get("quickstart");
      
      	// 判断请求是否成功
        assert collection.isSuccess();
    }
}
```

## 入参描述

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | 已创建的集合名称 |

## 出参描述

**说明**

返回结果为`DashVectorCollection`对象，`DashVectorCollection`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getName() | String | 集合名称 | complex |
| getCollectionMeta() | CollectionMeta | 集合信息，参考[CollectionMeta](https://help.aliyun.com/zh/document_detail/2510262.html#c56eb3c05bx05) | ``` { "name":"quickstart", "dimension":4, "dataType":"INT", "metric":"dotproduct", "status":"SERVING", "fieldsSchema":{ "name":"STRING", "weight":"FLOAT", "age":"INT" }, "partitionStatus":{ "default":"SERVING" } } ``` |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取所有已创建的Collection名称列表。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

public Response<List<String>> list();
```

## 接口使用

**说明**

需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
      
        Response<List<String>> response = client.list();
      
        System.out.println(response);
        // example output:
        // {
        //     "code":0,
        //     "message":"",
        //     "requestId":"5de1a75e-2996-4496-a284-9b958dfdad53",
        //     "output":[
        //         "simple",
        //         "quickstart"
        //     ]
        // }
    }
}
```

## 入参描述

无

## **出参描述**

**说明**

返回结果为`Response<List<String>>`对象，`Response<List<String>>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<String> | 所有Collection名称列表 | \\['my\\_collection1', 'my\\_collection2'\\] |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取已创建的Collection的统计信息，如Doc数等。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

public Response<CollectionStats> stats();
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.models.CollectionStats;
import com.aliyun.dashvector.common.DashVectorException;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");

        DashVectorCollection collection = client.get("quickstart");
        Response<CollectionStats> response = collection.stats();
        
      	System.out.println(response);
      	// example output:
        // {
        //     "code":0,
        //     "message":"Success",
        //     "requestId":"84b801f9-7545-4f9e-b480-713d6c4d9393",
        //     "output":{
        //         "totalDocCount":1,
        //         "indexCompleteness":1,
        //         "partitions":{
        //             "default":{
        //                 "totalDocCount":1
        //             }
        //         }
        //     }
        // }
    }
}
```

## **入参描述**

无

## **出参描述**

**说明**

返回结果为`Response<CollectionStats>`对象，`Response<CollectionStats>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | CollectionStats | 参考[CollectionStats](https://help.aliyun.com/zh/document_detail/2510262.html#vjcum) | ``` { "totalDocCount":1, "indexCompleteness":1, "partitions":{ "default":{ "totalDocCount":1 } } } ``` |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK删除一个已创建的Collection。

**重要**

删除Collection后，该Collection所有数据将删除且不可恢复，请谨慎操作。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorClient

public Response<Void> delete(String name);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例会删除[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;

public class Main {
    public static void main(String[] args) throws DashVectorException {
      	DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
      	
      	Response<Void> response = client.delete("quickstart");
    }
}
```

## **入参描述**

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | 已创建的集合名称 |

## **出参描述**

**说明**

返回结果为`Response<Void>`对象，`Response<Void>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| isSuccess() | Boolean | 判断请求是否成功 | true |