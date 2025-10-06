# Lambda Cloud Starter IoTDB

Apache IoTDB 的 Spring Boot Starter，提供时序数据库连接和订阅功能。

## 功能特性

- 支持 IoTDB Tree 和 Table 两种数据模型
- 自动配置 SessionPool 和 TableSessionPool
- 提供订阅功能支持
- 支持消息处理器自动注册

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-iotdb</artifactId>
</dependency>
```

## 配置

```yaml
lambda:
  iotdb:
    tree-dialect: true                    # 启用 Tree 数据模型
    table-dialect: false                  # 启用 Table 数据模型
    enable-subscription: true             # 启用订阅功能
    host: localhost                       # IoTDB 主机地址
    port: 6667                           # IoTDB 端口
    node-urls:                           # 集群节点地址（可选）
      - localhost:6667
    user: root                           # 用户名
    password: root                       # 密码
    database: test                       # 数据库名（Table 模式）
    max-size: 10                         # 连接池最大连接数
    thrift-max-frame-size: 67108864      # Thrift 最大帧大小
    base-package: com.lambda.cloud.iotdb # 扫描包路径
```

## 核心组件

### SessionPool (Tree 数据模型)
当 `tree-dialect=true` 时自动配置，用于 Tree 数据模型的数据操作。

### ITableSessionPool (Table 数据模型)
当 `table-dialect=true` 时自动配置，用于 Table 数据模型的数据操作。

### 订阅功能
当 `enable-subscription=true` 时启用：
- `ISubscriptionTreeSession` - Tree 模型订阅会话
- `ISubscriptionTableSession` - Table 模型订阅会话

## 使用示例

### 基本数据操作

```java
@Service
public class IoTDBService {
    
    @Autowired
    private SessionPool sessionPool; // Tree 模型
    
    @Autowired
    private ITableSessionPool tableSessionPool; // Table 模型
    
    public void insertData() throws Exception {
        // Tree 模型插入
        sessionPool.insertRecord("root.test.device", 
            System.currentTimeMillis(), 
            Arrays.asList("temperature"), 
            Arrays.asList(TSDataType.DOUBLE), 
            Arrays.asList(25.5));
    }
}
```

### 消息订阅

创建消息处理器：

```java
@IotDbSubscription(
    consumerId = "consumer1",
    topic = "test_topic",
    consumerGroupId = "group1"
)
public class MyMessageHandler implements MessageHandler {
    
    @Override
    public void handle(RowRecord record) {
        // 处理接收到的数据
        System.out.println("Received: " + record);
    }
}
```

### 配置示例

#### Tree 数据模型配置
```yaml
lambda:
  iotdb:
    tree-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    max-size: 20
```

#### Table 数据模型配置
```yaml
lambda:
  iotdb:
    table-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    database: test_db
    max-size: 20
```

#### 启用订阅功能
```yaml
lambda:
  iotdb:
    tree-dialect: true
    enable-subscription: true
    host: localhost
    port: 6667
    user: root
    password: root
```

## 注意事项

- Tree 和 Table 数据模型可以同时启用
- 订阅功能需要单独启用
- 确保 IoTDB 服务正常运行
- 根据实际需求调整连接池大小
- 消息处理器需要实现 MessageHandler 接口并使用 @IotDbSubscription 注解