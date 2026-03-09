本文介绍如何通过Java SDK为Collection创建一个新的Partition。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

public Response<Void> createPartition(String name, Integer timeout);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.responses.Response;

public class Main {
    public static void main(String[] args) throws DashVectorException {
      	DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
      	DashVectorCollection collection = client.get("quickstart");
    
        // 创建一个名称为shoes的Partition
        Response<Void> response = collection.createPartition("shoes");
      
        // 判断createPartition方法是否成功
        if (response.isSuccess()) {
            System.out.println("createPartition success");
        }
    }
}
```

## 入参描述

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | 待创建的分区名称 |
| timeout | Integer | 否   | null | - `timeout == null`：接口开启同步，待Partition创建成功后返回； - `timeout == -1`：接口开启异步 - `timeout >= 0`：接口开启同步并等待，若规定时间Partition未创建成功，则返回超时。 |

## **出参描述**

**说明**

返回结果为`Response<Void>`对象，`Response<Void>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取Collection中一个已存在的Partition的状态

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

public Response<Status> describePartition(String name);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    
3.  本示例需要参考[新建Partition](https://help.aliyun.com/zh/document_detail/2573598.html)提前创建好名称为`shoes`的Partition。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.responses.Response;

public class Main {
    public static void main(String[] args) throws DashVectorException {
      	DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
      	DashVectorCollection collection = client.get("quickstart");
      	
      	// 描述Partition
        Response<Status> response = collection.describePartition("shoes");

        System.out.println(response);
        // example output:
        // {"code":0,"message":"","requestId":"f554cde8-6147-42b5-b794-8cb9c0ae57da","output":"SERVING"}
    }
}
```

## 入参描述

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | Partition名称 |

## **出参描述**

**说明**

返回结果为`Response<Status>`对象，`Response<Status>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | String | 参考[Status](https://help.aliyun.com/zh/document_detail/2510262.html#df1d5390342q4) | - `INITIALIZED`：创建中 - `SERVING`：服务中 - `DROPPING`：删除中 - `ERROR`：状态异常 |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取Collection中所有Partition名称的列表。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## **接口定义**

Java

```
// class DashVectorCollection

public Response<List<String>> listPartitions();
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection-使用示例](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.responses.Response;

import java.util.List;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
      
        Response<List<String>> response = collection.listPartitions();
    
        System.out.println(response);
        // example output:
        // {"code":0,"message":"","requestId":"d72a4f14-ff9a-4e39-89d4-ce023d34d37f","output":["default","shoes"]}
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
| getReqeuestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | List<String> | Collection中所有Partition的列表 | \\['shoes', 'default'\\] |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK获取Collection中一个已存在的Partition的统计信息，如Doc数等。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## 接口定义

Java

```
// class DashVectorCollection

public Response<PartitionStats> statsPartition(String name);
```

## 使用示例

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    
3.  本示例需要参考[新建Partition](https://help.aliyun.com/zh/document_detail/2573598.html)提前创建好名称为`shoes`的Partition。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.PartitionStats;
import com.aliyun.dashvector.models.responses.Response;

import java.util.List;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
      
        Response<PartitionStats> response = collection.statsPartition("shoes");
    
        System.out.println(response);
        // example output:
        // {"code":0,"message":"Success","requestId":"ebb83c4a-35f7-4128-b1ad-d8e3d9be49a2","output":{"totalDocCount":0}}
    }
}
```

## **入参描述**

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | Partition名称 |

## **出参描述**

**说明**

返回结果为`Response<PartitionStats>`对象，`Response<PartitionStats>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessage() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| getOutput() | PartitionStats | 参考[PartitionStats](https://help.aliyun.com/zh/document_detail/2510262.html#KVUzU) | ``` {"totalDocCount":0} ``` |
| isSuccess() | Boolean | 判断请求是否成功 | true |


本文介绍如何通过Java SDK删除Collection中一个已存在的Partition。

**重要**

删除Partition后，该Partition所有数据将删除且不可恢复，请谨慎操作。

## 前提条件

-   已创建Cluster：[创建Cluster](https://help.aliyun.com/zh/document_detail/2631966.html)。
    
-   已获得API-KEY：[API-KEY管理](https://help.aliyun.com/zh/document_detail/2510230.html)。
    
-   已安装最新版SDK：[安装DashVector SDK](https://help.aliyun.com/zh/document_detail/2510231.html)。
    

## 接口定义

Java

```
// class DashVectorCollection

public Response<Void> deletePartition(String name);
```

## **使用示例**

**说明**

1.  需要使用您的api-key替换示例中的YOUR\_API\_KEY、您的Cluster Endpoint替换示例中的YOUR\_CLUSTER\_ENDPOINT，代码才能正常运行。
    
2.  本示例需要参考[新建Collection](https://help.aliyun.com/zh/document_detail/2573558.html)提前创建好名称为`quickstart`的Collection。
    
3.  本示例需要参考[新建Partition](https://help.aliyun.com/zh/document_detail/2573598.html)提前创建好名称为`shoes`的Partition。
    

Java

```
import com.aliyun.dashvector.DashVectorClient;
import com.aliyun.dashvector.DashVectorCollection;
import com.aliyun.dashvector.common.DashVectorException;
import com.aliyun.dashvector.models.responses.Response;

public class Main {
    public static void main(String[] args) throws DashVectorException {
        DashVectorClient client = new DashVectorClient("YOUR_API_KEY", "YOUR_CLUSTER_ENDPOINT");
        DashVectorCollection collection = client.get("quickstart");
        
        // 删除名为shoes的Partition
        Response<Void> response = collection.deletePartition("shoes");

        // 判断请求是否成功
        // assert response.isSuccess();
    }
}
```

## **入参描述**

| **参数** | **类型** | **必填** | **默认值** | **描述** |
| --- | --- | --- | --- | --- |
| name | String | 是   | \\- | 待删除的Partition名称 |

## **出参描述**

**说明**

返回结果为`Response<Void>`对象，`Response<Void>`对象中可获取本次操作结果信息，如下表所示。

| **方法** | **类型** | **描述** | **示例** |
| --- | --- | --- | --- |
| getCode() | int | 返回值，参考[返回状态码说明](https://help.aliyun.com/zh/document_detail/2510266.html) | 0   |
| getMessasge() | String | 返回消息 | success |
| getRequestId() | String | 请求唯一id | 19215409-ea66-4db9-8764-26ce2eb5bb99 |
| isSuccess() | Boolean | 判断请求是否成功 | true |