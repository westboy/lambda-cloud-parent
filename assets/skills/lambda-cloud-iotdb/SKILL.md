---
name: "lambda-cloud-iotdb"
description: "Lambda Cloud IoTDB时序数据库配置指南。当用户需要集成Apache IoTDB时序数据库、配置Tree/Table模式或使用订阅功能时调用。"
---

# Lambda Cloud IoTDB 时序数据库

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-iotdb</artifactId>
</dependency>
```

## 配置

配置前缀: `lambda.iotdb`

### Tree 模式

```yaml
lambda:
  iotdb:
    tree-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    max-size: 10
```

### Table 模式

```yaml
lambda:
  iotdb:
    table-dialect: true
    host: localhost
    port: 6667
    user: root
    password: root
    database: test_db
    thrift-max-frame-size: 67108864
```

### 订阅功能

```yaml
lambda:
  iotdb:
    enable-subscription: true
    base-package: com.lambda.cloud.iotdb  # 默认值
```

### 完整配置项

| 配置项 | 说明 | 默认值 |
|--------|------|--------|
| `tree-dialect` | Tree 模式 | - |
| `table-dialect` | Table 模式 | - |
| `host` | 主机地址 | - |
| `port` | 端口 | - |
| `user` | 用户名 | - |
| `password` | 密码 | - |
| `max-size` | 连接池大小 | `10` |
| `database` | 数据库名 (Table 模式) | - |
| `node-urls` | 集群节点列表 | - |
| `ttl` | 数据 TTL | - |
| `timeout` | 超时时间(ms) | `10000` |
| `thrift-max-frame-size` | Thrift 最大帧大小 | - |
| `enable-subscription` | 启用订阅 | - |
| `base-package` | 订阅扫描包 | `com.lambda.cloud.iotdb` |

## Tree 模式使用

```java
@Service
public class IotDbTreeService {

    @Autowired
    private SessionPool sessionPool;

    // 创建时间序列
    public void createTimeseries(String deviceId, String measurement) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            session.createTimeseries(
                deviceId + "." + measurement,
                TSDataType.DOUBLE,
                TSEncoding.RLE,
                CompressionType.SNAPPY
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }

    // 插入数据
    public void insert(String deviceId, long timestamp, String measurement, double value) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            session.insertRecord(
                deviceId,
                timestamp,
                Collections.singletonList(measurement),
                Collections.singletonList(TSDataType.DOUBLE),
                Collections.singletonList(value)
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }

    // 批量插入
    public void insertBatch(String deviceId, List<Long> timestamps,
                            List<String> measurements, List<List<Object>> values) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            for (int i = 0; i < timestamps.size(); i++) {
                session.insertRecord(
                    deviceId,
                    timestamps.get(i),
                    measurements,
                    Arrays.asList(TSDataType.DOUBLE, TSDataType.FLOAT),
                    values.get(i)
                );
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }

    // 查询数据
    public List<RowRecord> query(String deviceId, long startTime, long endTime) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            SessionDataSet dataSet = session.executeQueryStatement(
                "SELECT * FROM " + deviceId + " WHERE time > " + startTime + " AND time < " + endTime
            );

            List<RowRecord> records = new ArrayList<>();
            while (dataSet.hasNext()) {
                RowRecord record = dataSet.next();
                records.add(record);
            }
            return records;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }
}
```

## Table 模式使用

```java
@Service
public class IotDbTableService {

    @Autowired
    private ITableSessionPool tableSessionPool;

    // 创建表
    public void createTable(String tableName) {
        ITableSession session = null;
        try {
            session = tableSessionPool.getSession();
            session.executeNonQueryStatement(
                "CREATE TABLE IF NOT EXISTS " + tableName + " (" +
                "time TIMESTAMP, " +
                "device_id STRING, " +
                "temperature FLOAT, " +
                "humidity FLOAT" +
                ")"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                tableSessionPool.putBack(session);
            }
        }
    }

    // 插入数据
    public void insert(String tableName, long timestamp, String deviceId,
                       float temperature, float humidity) {
        ITableSession session = null;
        try {
            session = tableSessionPool.getSession();
            session.executeNonQueryStatement(
                "INSERT INTO " + tableName + "(time, device_id, temperature, humidity) " +
                "VALUES (" + timestamp + ", '" + deviceId + "', " + temperature + ", " + humidity + ")"
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                tableSessionPool.putBack(session);
            }
        }
    }

    // 查询数据
    public List<RowRecord> query(String tableName, String deviceId,
                                 long startTime, long endTime) {
        ITableSession session = null;
        try {
            session = tableSessionPool.getSession();
            SessionDataSet dataSet = session.executeQueryStatement(
                "SELECT * FROM " + tableName +
                " WHERE device_id = '" + deviceId + "'" +
                " AND time > " + startTime +
                " AND time < " + endTime
            );

            List<RowRecord> records = new ArrayList<>();
            while (dataSet.hasNext()) {
                records.add(dataSet.next());
            }
            return records;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (session != null) {
                tableSessionPool.putBack(session);
            }
        }
    }
}
```

## 声明式订阅

```java
@IotDbSubscription(
    consumerId = "sensor-consumer",
    topic = "sensor_data",
    consumerGroupId = "sensor-group"
)
public class SensorDataHandler implements MessageHandler {

    @Override
    public void handle(RowRecord record) {
        // 处理订阅消息
        long timestamp = record.getTimestamp();
        List<Field> fields = record.getFields();

        System.out.println("Received at " + timestamp);
        for (Field field : fields) {
            System.out.println("  " + field.toString());
        }
    }
}
```

### @IotDbSubscription 注解

| 属性 | 说明 | 默认值 |
|------|------|--------|
| `consumerId` | 消费者 ID | 必填 |
| `topic` | 订阅主题 | 必填 |
| `consumerGroupId` | 消费者组 ID | `default_group` |

## 集群配置

```yaml
lambda:
  iotdb:
    tree-dialect: true
    node-urls:
      - iotdb-node1:6667
      - iotdb-node2:6667
      - iotdb-node3:6667
    user: root
    password: root
    max-size: 20
```

## 完整示例

```java
@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    @Autowired
    private SessionPool sessionPool;

    @PostMapping("/data")
    public Result<Void> submitSensorData(@RequestBody SensorDataDTO dto) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            session.insertRecord(
                "root.sg1.d" + dto.getDeviceId(),
                dto.getTimestamp(),
                Arrays.asList("temperature", "humidity"),
                Arrays.asList(TSDataType.DOUBLE, TSDataType.DOUBLE),
                Arrays.asList(dto.getTemperature(), dto.getHumidity())
            );
            return Result.success();
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }

    @GetMapping("/data")
    public Result<List<RowRecord>> querySensorData(
            @RequestParam String deviceId,
            @RequestParam long startTime,
            @RequestParam long endTime) {
        Session session = null;
        try {
            session = sessionPool.getSession();
            SessionDataSet dataSet = session.executeQueryStatement(
                "SELECT temperature, humidity FROM root.sg1.d" + deviceId +
                " WHERE time > " + startTime + " AND time < " + endTime
            );

            List<RowRecord> records = new ArrayList<>();
            while (dataSet.hasNext()) {
                records.add(dataSet.next());
            }
            return Result.success(records);
        } catch (Exception e) {
            return Result.fail(e.getMessage());
        } finally {
            if (session != null) {
                sessionPool.putBack(session);
            }
        }
    }
}
```

## 最佳实践

1. **模式选择**: 设备数据用 Tree 模式, 分析查询用 Table 模式
2. **连接池**: 合理设置 `max-size`, 避免连接不足
3. **Session 管理**: 使用完及时 `putBack`, 避免连接泄漏
4. **数据模型**: Tree 模式使用 `root.sg.device` 格式
5. **订阅**: 确保 `enable-subscription=true` 并配置正确的 `base-package`
6. **集群**: 生产环境配置多个 `node-urls` 实现高可用
