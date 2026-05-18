---
name: "lambda-cloud-netty"
description: "Lambda Cloud Netty协议引擎配置指南。当用户需要实现二进制协议解析、自定义TCP协议或Netty服务端时调用。"
---

# Lambda Cloud Netty 协议引擎

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-netty</artifactId>
</dependency>
```

## 配置

配置前缀: `spring.netty`

### NettyExtendProperties 完整配置项

| 配置路径 | 类型 | 默认值 | 说明 |
|---------|------|--------|------|
| `server.tcp-port` | `int` | `0` | TCP 监听端口 |
| `server.boss-thread-count` | `int` | `0` | boss 线程数 |
| `server.worker-thread-count` | `int` | `0` | worker 线程数（>0 时按配置创建） |
| `server.so-keepalive` | `boolean` | `true` | TCP 长连接保活 |
| `server.auto-start-up` | `boolean` | `true` | 是否自动启动 |
| `server.max-frame-length` | `int` | `255` | 解码数据的最大帧长度 |
| `server.dispatch-permits` | `int` | `100` | 分发线程池大小 |
| `server.option-map` | `Map<String,Object>` | `{}` | 透传到 `ServerBootstrap.option()` |
| `server.idle-config.reader-idle-time-seconds` | `int` | `0` | 读空闲超时（秒） |
| `server.idle-config.writer-idle-time-seconds` | `int` | `0` | 写空闲超时（秒） |
| `server.idle-config.all-idle-time-seconds` | `int` | `180` | 读写双向空闲超时（秒） |
| `server.idle-config.unit` | `TimeUnit` | `SECONDS` | 空闲时间单位 |

### 配置示例

```yaml
spring:
  netty:
    server:
      tcp-port: 9000
      worker-thread-count: 16
      auto-start-up: true
      max-frame-length: 255
      dispatch-permits: 100
      so-keepalive: true
      option-map:
        SO_BACKLOG: 1024
      idle-config:
        reader-idle-time-seconds: 0
        writer-idle-time-seconds: 0
        all-idle-time-seconds: 180
```

## Netty Server 生命周期

`NettyServer` 实现 `SmartLifecycle`:

- `start()`: 执行 `serverBootstrap.bind()`
- `stop()`: 关闭 `serverChannel`
- `isAutoStartup()`: 由 `server.auto-start-up` 控制
- `getPhase()=Integer.MAX_VALUE`: Spring 生命周期末尾启动

平台选择策略:

- `shouldEpoll`: 仅在 Linux 且 `Epoll.isAvailable()` 时使用 Epoll
- 否则使用 NIO（`NioEventLoopGroup` + `NioServerSocketChannel`）

## 扩展点

### ServerBootstrapConfigurationCustomizer

`@FunctionalInterface`，用于补充 server 级配置:

```java
@Component
public class CustomServerCustomizer implements ServerBootstrapConfigurationCustomizer {

    @Override
    public void configuration(ServerBootstrap bootstrap) {
        bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
        bootstrap.childOption(ChannelOption.TCP_NODELAY, true);
    }
}
```

### ChannelPipelineConfigurationCustomizer

`@FunctionalInterface`，用于注入协议解码器、业务 handler、心跳 handler 等。`NettyChannelInitializer` 仅调用 `customizer.configuration(pipeline)`，不内置默认 handler 链。

```java
@Component
public class CustomPipelineCustomizer implements ChannelPipelineConfigurationCustomizer {

    @Override
    public void configuration(ChannelPipeline pipeline) {
        pipeline.addLast(new IdleStateHandler(0, 0, 180, TimeUnit.SECONDS));
        pipeline.addLast(new HeartbeatHandler());
        pipeline.addLast(new BusinessHandler());
    }
}
```

---

## 协议引擎注解

### @ProtocolPayload 消息级注解

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `frameType` | `String` | `""` | 消息类型标识（如 `"0x3B"`、`"base"`） |
| `name` | `String` | `""` | 消息名称 |
| `crcAlgorithm` | `String` | `"CRC16-MODBUS"` | CRC 算法名称 |
| `description` | `String` | `""` | 消息描述 |
| `version` | `String` | `"1.0"` | 消息版本 |
| `isFrame` | `boolean` | `false` | 是否为帧消息（`true`=帧容器，`false`=消息体） |
| `defaultCharset` | `String` | `"UTF-8"` | 默认字符编码（可被字段级设置覆盖） |

`isFrame` 语义:

- `true`: 帧消息，包含帧头/帧尾/CRC 等结构，引擎会自动执行 CRC 校验与回填
- `false`: 消息体，通过 `ProtocolPayloadRegistry` 按 `frameType` 注册后由帧消息的 composite/payload 字段动态路由

### @ProtocolField 字段级注解

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `order` | `int` | (必填) | 字段在协议中的顺序（从 0 开始） |
| `length` | `int` | `0` | 字段长度（字节数） |
| `dataType` | `ProtocolDataType` | `HEX` | 数据类型 |
| `composite` | `boolean` | `false` | 复合类型标志，表示该字段为一个类，需递归收集其 `@ProtocolField` |
| `encryptedKey` | `boolean` | `false` | 加密控制标识，当该字段值为 `0x01` 时 `encryptedField` 字段生效 |
| `encryptedField` | `boolean` | `false` | 是否为加密数据字段 |
| `computed` | `boolean` | `false` | 计算字段，参与 CRC 校验值与长度计算 |
| `payload` | `boolean` | `false` | 子消息体标识，配合 `ProtocolPayloadRegistry` 动态路由消息体类型 |
| `crcFiled` | `boolean` | `false` | CRC 校验字段，存储校验值 |
| `lengthFiled` | `boolean` | `false` | 长度字段，存储数据长度 |
| `serialFiled` | `boolean` | `false` | 序号字段，存储消息序号 |
| `precision` | `int` | `0` | 小数精度（小数位数） |
| `littleEndian` | `boolean` | `false` | 是否小端字节序 |
| `description` | `String` | `""` | 字段描述 |
| `optional` | `boolean` | `false` | 是否为可选字段 |
| `defaultValue` | `String` | `""` | 可选字段数据不足时的默认值 |
| `charset` | `String` | `"UTF-8"` | 字符编码（仅 ASCII 类型有效） |
| `padding` | `PaddingDirection` | `LEFT` | 填充方向 |
| `paddingChar` | `String` | `"0"` | 填充字符 |
| `listElementType` | `ProtocolDataType` | `HEX` | List 元素数据类型（仅 LIST 有效） |
| `listElementSize` | `int` | `0` | List 固定元素数量（>0 时按此数量解析） |
| `listElementClass` | `Class<?>` | `Void.class` | List 元素类（仅 COMPOSITE 类型有效） |
| `listElementSizeField` | `String` | `""` | 引用同一消息类中字段名，动态获取 List 元素数量 |

约束: 同一字段不可同时标注 `encryptedKey` 与 `encryptedField`。

### @ProtocolValidation 字段验证注解

| 属性 | 类型 | 默认值 | 说明 |
|------|------|--------|------|
| `min` | `long` | `Long.MIN_VALUE` | 最小值（数值类型） |
| `max` | `long` | `Long.MAX_VALUE` | 最大值（数值类型） |
| `minLength` | `int` | `0` | 最小长度（字符串类型） |
| `maxLength` | `int` | `Integer.MAX_VALUE` | 最大长度（字符串类型） |
| `pattern` | `String` | `""` | 正则表达式验证 |
| `nullable` | `boolean` | `true` | 是否允许为空 |
| `validator` | `Class<? extends ProtocolValidator>` | `ProtocolValidator.class` | 自定义验证器类 |
| `message` | `String` | `"字段验证失败"` | 验证失败错误消息 |

### ProtocolDataType 枚举

| 枚举值 | 描述 | 字节数 |
|--------|------|--------|
| `HEX` | 十六进制数据 | 按 length |
| `ASCII` | ASCII 字符串 | 按 length |
| `BCD` | BCD 编码 | 按 length |
| `BIT` | 位数据 | 按 length |
| `BMS_CURRENT` | BMS 电流 | 按 length |
| `UINT8` | 无符号 8 位整数 | 1 |
| `UINT16` | 无符号 16 位整数 | 2 |
| `UINT32` | 无符号 32 位整数 | 4 |
| `UINT64` | 无符号 64 位整数 | 8 |
| `CP56TIME2A` | CP56Time2a 时间格式 | 7 |
| `COMPOSITE` | 复合类型 | 动态 |
| `LIST` | 列表类型 | 动态 |

### PaddingDirection 枚举

| 枚举值 | 描述 |
|--------|------|
| `LEFT` | 左填充（在数据前面填充） |
| `RIGHT` | 右填充（在数据后面填充） |
| `NONE` | 不填充 |

---

## 核心 API

### ProtocolEngine 接口

```java
public interface ProtocolEngine<T> {
    T parse(ByteBuf byteBuf, Class<?> messageClass) throws ProtocolException;
    void serialize(T message, ByteBuf byteBuf) throws ProtocolException;
    ValidationResult validate(T message);
    int calculateLength(Class<?> messageClass);
    ProtocolPayloadMetadata getMetadata(Class<?> messageClass);
}
```

### ProtocolEngineFactory

```java
public class ProtocolEngineFactory {
    enum EngineType {
        REFLECTION,
        BYTECODE,
        ANNOTATION_PROCESSOR
    }

    static <T> ProtocolEngine<T> getEngine(EngineType type);
    static void addEngine(EngineType type, ProtocolEngine<?> engine);
    static void clearCache();
}
```

使用示例:

```java
ProtocolEngineFactory.addEngine(
    ProtocolEngineFactory.EngineType.REFLECTION,
    new ReflectionProtocolEngine(encryptionService));

ProtocolEngine<MyMessage> engine =
    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

MyMessage msg = engine.parse(byteBuf, MyMessage.class);
engine.serialize(msg, outBuf);
ValidationResult result = engine.validate(msg);
```

### ProtocolPayloadRegistry

```java
public class ProtocolPayloadRegistry {
    static void register(String protocol, Class<?> protocolMessage);
    static Class<?> getProtocolMessage(String protocol);
    static Map<String, Class<?>> getAllProtocols();
    static void clear();
    static boolean unregister(String protocol);
}
```

- 仅注册 `isFrame=false` 的消息体类
- `ProtocolPayloadScanner` 可自动扫描指定包路径并注册

### ProtocolPayloadScanner

```java
public class ProtocolPayloadScanner {
    void scanAndRegister(String... basePackages);
}
```

### ChannelRepository

```java
public class ChannelRepository {
    void put(String key, Channel value);
    Optional<Channel> get(String key);
    void remove(String key);
    int size();
    void clear();
    boolean containsKey(String key);
    Map<String, Channel> getChannels();
}
```

使用示例:

```java
@Service
public class DeviceService {

    @Autowired
    private ChannelRepository channelRepository;

    public void registerDevice(String deviceId, Channel channel) {
        channelRepository.put(deviceId, channel);
    }

    public void sendMessage(String deviceId, Object message) {
        channelRepository.get(deviceId).ifPresent(channel -> {
            if (channel.isActive()) {
                channel.writeAndFlush(message);
            }
        });
    }

    public void removeDevice(String deviceId) {
        channelRepository.remove(deviceId);
    }
}
```

### ByteBufPool

```java
public class ByteBufPool {
    static ByteBuf buffer();
    static ByteBuf acquire(int capacity);
    static void safeRelease(ByteBuf buf);
    static String getPoolStats();
}
```

- `buffer()`: 从池中分配默认大小缓冲区
- `safeRelease()`: 安全释放，自动处理空值和重复释放
- `getPoolStats()`: 返回内存使用统计信息

### SerialNumberManager

```java
public class SerialNumberManager {
    SerialNumberManager();
    SerialNumberManager(int minSerial, int maxSerial);
    int nextSerial();
    int getCurrentSerial();
    void reset();
    void setCurrentSerial(int serial);
    boolean isValidSerial(int serial);
    String getRangeInfo();
    int getRemainingCount();
    static SerialNumberManager create(int minSerial, int maxSerial);
    static SerialNumberManager createDefault();
}
```

- 默认范围: `1` 到 `0xFFFF`
- `nextSerial()`: 线程安全，循环使用（到达 maxSerial 后回到 minSerial）
- `setCurrentSerial()`: 设置当前序号，超出范围抛出 `IllegalArgumentException`

### ProtocolException 错误码

| ErrorCode | 描述 |
|-----------|------|
| `UNKNOWN` | 未知错误 |
| `PARSE_ERROR` | 解析错误 |
| `SERIALIZE_ERROR` | 序列化错误 |
| `VALIDATION_ERROR` | 验证错误 |
| `FIELD_NOT_FOUND` | 字段未找到 |
| `TYPE_MISMATCH` | 类型不匹配 |
| `LENGTH_MISMATCH` | 长度不匹配 |
| `BUFFER_UNDERFLOW` | 缓冲区数据不足 |
| `ANNOTATION_MISSING` | 注解缺失 |
| `REFLECTION_ERROR` | 反射错误 |
| `CRC_ERROR` | CRC 处理错误 |
| `CRC_VALIDATION_ERROR` | CRC 校验失败 |

---

## 解析/序列化链路

`ReflectionProtocolEngine` 处理流程:

1. 读取/构建 `ProtocolPayloadMetadata`
2. 遍历字段元数据并调用 `ProtocolFieldProcessor` 解析
3. 解析时收集 `computed=true` 字段原始切片，完成后执行 CRC 校验
4. 序列化时先将 CRC/Length 字段置零占位，再回填真实值
5. 调用 `ValidationEngine` 执行规则校验

关键组件职责:

- `ProtocolFieldProcessor`: 字段读写、List/Composite 动态长度处理、加密字段处理
- `ComputedProcessor`: CRC/长度计算、回填、校验
- `DataTypeConverterResolver`: 数据类型转换器分发
- `ValidationEngine`: 范围/长度/正则/自定义验证器执行

### 加密能力

- 触发条件: 报文字段中存在 `encryptedKey=true` 且值为 `0x01`（或数值 1），同时字段标记 `encryptedField=true`
- 判断逻辑由 `EncryptionUtils.isEncryptionEnabled()` 控制

### CRC 能力

- 算法入口: `ChecksumFactory`
- 支持算法: `CRC16-CCITT`、`CRC16-IBM`、`CRC16-MAXIM`、`CRC16-USB`、`CRC16-X25`、`CRC16-XMODEM`、`CRC16-MODBUS`（默认）

---

## 示例代码

### 帧消息 (isFrame=true)

```java
@Getter
@Setter
@ProtocolPayload(frameType = "base", name = "云快充基础协议", isFrame = true, description = "云快充基础协议字段")
public abstract class YkcV16BaseMessage<T> implements ProtocolMessage {

    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX, lengthFiled = true, littleEndian = true, description = "数据长度")
    private Integer dataLength;

    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.HEX, computed = true, littleEndian = true, description = "序列号域")
    private Integer serialNumber;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.HEX, computed = true, littleEndian = true, description = "加密标志")
    private String encryptFlag;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(order = 5, composite = true, computed = true, description = "内容")
    private T body;

    @ProtocolField(order = 6, length = 2, dataType = ProtocolDataType.HEX, littleEndian = true, crcFiled = true, description = "校验码")
    private String crc;
}
```

### 消息体 (isFrame=false)

```java
@Getter
@Setter
@ProtocolPayload(frameType = "0x3B", name = "交易记录", description = "消息体", version = "1.0")
public class RawInnerRecord {

    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.HEX, description = "订单编号")
    private String orderNumber;

    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.HEX, description = "桩编号")
    private String stationNumber;

    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer gunNumber;

    @ProtocolField(order = 4, length = 7, computed = true, dataType = ProtocolDataType.CP56TIME2A, description = "开始时间")
    private String startTime;

    @ProtocolField(order = 6, length = 4, computed = true, dataType = ProtocolDataType.HEX, littleEndian = true, precision = 5, description = "尖单价")
    private BigDecimal peakPrice;
}
```

### 基本类型 LIST

```java
@Getter
@Setter
@ProtocolPayload(frameType = "F0", name = "CountedListMessage")
public class CountedListMessage {

    @ProtocolField(order = 1, length = 1, computed = true, dataType = ProtocolDataType.UINT8)
    private Integer count;

    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.LIST,
        listElementType = ProtocolDataType.UINT8, listElementSizeField = "count")
    private List<Integer> values;
}
```

### Composite LIST

```java
@Getter
@Setter
@ProtocolPayload(frameType = "F1", name = "CompositeListMessage")
public class CompositeListMessage {

    @ProtocolField(order = 1, length = 1, computed = true, dataType = ProtocolDataType.UINT8)
    private Integer count;

    @ProtocolField(order = 2, length = 2, computed = true, composite = true, dataType = ProtocolDataType.LIST,
        listElementType = ProtocolDataType.COMPOSITE, listElementClass = CompositeListItem.class, listElementSizeField = "count")
    private List<CompositeListItem> items;
}
```

### 固定长度 LIST

```java
@Getter
@Setter
@ProtocolPayload(frameType = "F2", name = "FixedListMessage")
public class FixedListMessage {

    @ProtocolField(order = 1, length = 1, listElementSize = 48,
        listElementType = ProtocolDataType.HEX, dataType = ProtocolDataType.LIST, description = "时段费率号")
    private List<Integer> timeSlotRateNumbers;
}
```

### 加密消息

```java
@Getter
@Setter
@ProtocolPayload(frameType = "0x00", name = "云快充协议基础字段", description = "云快充2.1协议基础字段")
public class EncryptedBaseMessage implements ProtocolMessage {

    @ProtocolField(order = 0, length = 1, dataType = ProtocolDataType.HEX, description = "起始符")
    private String startFlag;

    @ProtocolField(order = 1, length = 1, dataType = ProtocolDataType.HEX, lengthFiled = true, littleEndian = true, description = "数据长度")
    private Integer dataLength;

    @ProtocolField(order = 2, length = 2, dataType = ProtocolDataType.HEX, computed = true, littleEndian = true, description = "序列号域")
    private Integer serialNumber;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, computed = true, littleEndian = true, encryptedKey = true, description = "加密标志")
    private Integer sec;

    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.HEX, computed = true, description = "帧类型")
    private String frameType;

    @ProtocolField(order = 5, composite = true, computed = true, encryptedField = true)
    private EncryptedInnerRecord innerRecord;

    @ProtocolField(order = 6, length = 2, dataType = ProtocolDataType.HEX, littleEndian = true, crcFiled = true, description = "校验码")
    private String crc;

    @Override
    public String getFrameType() {
        return frameType;
    }

    @Override
    public void setFrameType(String frameType) {
        this.frameType = frameType;
    }

    @Override
    public Integer getSerialNumber() {
        return serialNumber;
    }

    @Override
    public void setSerialNumber(Integer serialNumber) {
        this.serialNumber = serialNumber;
    }
}
```

### 字段验证

```java
@Getter
@Setter
@ProtocolPayload(frameType = "0x01", name = "ValidatedMessage")
public class ValidatedMessage {

    @ProtocolField(order = 0, length = 2, dataType = ProtocolDataType.HEX)
    @ProtocolValidation(min = 0, max = 9999, message = "编码范围 0-9999")
    private Integer code;

    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.ASCII)
    @ProtocolValidation(minLength = 1, maxLength = 16, nullable = false, message = "设备ID不能为空")
    private String deviceId;

    @ProtocolField(order = 2, length = 8, dataType = ProtocolDataType.ASCII)
    @ProtocolValidation(pattern = "^[A-Za-z0-9]+$", message = "仅允许字母数字")
    private String serialCode;

    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8)
    @ProtocolValidation(validator = PositiveNumberValidator.class)
    private Integer count;
}
```

### 引擎初始化与解析/序列化

```java
EncryptionService encryptionService = new DefaultEncryptionService(key);
ReflectionProtocolEngine engine = new ReflectionProtocolEngine(encryptionService);
ProtocolEngineFactory.addEngine(ProtocolEngineFactory.EngineType.REFLECTION, engine);

ProtocolPayloadRegistry.register("0x3B", RawInnerRecord.class);

ProtocolEngine<EncryptedBaseMessage> protocolEngine =
    ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

byte[] bytes = HexUtil.decodeHex("685e00070058...");
ByteBuf byteBuf = Unpooled.wrappedBuffer(bytes);

EncryptedBaseMessage msg = protocolEngine.parse(byteBuf, EncryptedBaseMessage.class);

ValidationResult validation = protocolEngine.validate(msg);
if (validation.valid()) {
    log.info("消息验证通过");
}

ByteBuf outBuf = Unpooled.buffer();
protocolEngine.serialize(msg, outBuf);
ByteBufPool.safeRelease(outBuf);
```

## 当前实现约束

- `ChannelPipelineConfigurationCustomizer` 没有默认实现；若业务未提供，`channelInitializer` 依赖无法满足
- `spring.netty.server.boss-thread-count` 当前未在 `NettyAutoConfiguration` 使用
- `ProtocolPayloadScanner` 仅注册 `isFrame=false` 类型
- `ProtocolPayloadRegistry` 为静态全局存储，多测试场景需要显式 `clear()`
- `ValidationEngine` 的数值范围使用 `double` 处理，极大整数存在精度风险
