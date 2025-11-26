# Lambda Cloud Starter Netty

基于 Netty 的 Spring Boot Starter，提供高性能网络通信能力。

## 功能特性

### 网络通信
- 自动配置 Netty 服务器
- 支持 TCP 长连接
- 可配置的线程池和连接参数
- 提供 Channel 管理和序列号生成
- 支持自定义 Pipeline 和 ServerBootstrap 配置

### 协议引擎 🚀
- **高性能协议解析**: 基于注解的协议定义和自动解析
- **智能缓存优化**: 字段反射缓存和转换器缓存，提升处理速度
- **对象池化**: 内置 ByteBuf 对象池，减少 GC 压力
- **性能监控**: 实时统计解析、序列化、验证操作的性能指标
- **数据验证**: 强类型数据验证和转换，支持自定义验证规则
- **多引擎支持**: 基础引擎、增强引擎（带/不带监控）
- **List 字段支持**: 支持 List 集合字段的解析和序列化，包括固定长度和动态长度列表

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-netty</artifactId>
</dependency>
```

## 配置

```yaml
spring:
  netty:
    server:
      tcp-port: 8080                    # TCP 端口
      boss-thread-count: 1              # Boss 线程数
      worker-thread-count: 4            # Worker 线程数
      so-keepalive: true                # 开启 TCP 长连接
      auto-start-up: true               # 自动启动
      max-frame-length: 255             # 最大帧长度
      dispatch-permits: 100             # 线程池大小
      idle-config:
        reader-idle-time-seconds: 0     # 读超时时间
        writer-idle-time-seconds: 0     # 写超时时间
        all-idle-time-seconds: 180      # 读写超时时间
```

## 核心组件

### NettyServer
实现 SmartLifecycle 接口的 Netty 服务器，支持自动启动和停止。

### ChannelRepository
提供 Channel 的存储和管理功能。

### SerialNumberAccessor
提供序列号生成功能，支持并发安全的递增序列号。

### 自定义配置接口

#### ChannelPipelineConfigurationCustomizer
用于自定义 ChannelPipeline 配置：

```java
@Component
public class CustomPipelineCustomizer implements ChannelPipelineConfigurationCustomizer {
    @Override
    public void configuration(ChannelPipeline pipeline) {
        // 添加自定义处理器
        pipeline.addLast(new YourCustomHandler());
    }
}
```

#### ServerBootstrapConfigurationCustomizer
用于自定义 ServerBootstrap 配置：

```java
@Component
public class CustomBootstrapCustomizer implements ServerBootstrapConfigurationCustomizer {
    @Override
    public void configuration(ServerBootstrap serverBootstrap) {
        // 自定义 ServerBootstrap 配置
        serverBootstrap.option(ChannelOption.SO_BACKLOG, 1024);
    }
}
```

## 使用示例


### 自定义处理器

```java
@Component
public class MyChannelPipelineCustomizer implements ChannelPipelineConfigurationCustomizer {
    
    @Override
    public void configuration(ChannelPipeline pipeline) {
        // 添加编解码器
        pipeline.addLast(new StringDecoder());
        pipeline.addLast(new StringEncoder());
        
        // 添加业务处理器
        pipeline.addLast(new MyBusinessHandler());
    }
}
```

### 网络服务基本使用

1. 添加依赖
2. 配置端口和线程参数
3. 实现自定义的 Pipeline 配置
4. 启动应用，Netty 服务器将自动启动

### 协议引擎使用

#### 1. 定义协议消息

```java
@ProtocolMessage(version = "1.0", description = "用户消息")
public class UserMessage {
    
    // 基础字段
    @ProtocolField(order = 1, length = 4, dataType = DataType.UINT32)
    @ProtocolValidation(required = true, min = 1)
    private Long userId;
    
    // 字符串字段（带填充）
    @ProtocolField(order = 2, length = 10, dataType = DataType.ASCII, 
                  padding = PaddingDirection.RIGHT, paddingChar = ' ')
    @ProtocolValidation(required = true, pattern = "^[a-zA-Z0-9]+$")
    private String username;
    
    // 嵌套对象（composite=true）
    @ProtocolField(order = 3, composite = true)
    private Address address;
    
    // 列表字段（List支持）
    @ProtocolField(order = 4, dataType = DataType.LIST, listElementType = DataType.UINT16, listElementSize = 5)
    private List<Integer> scores;
    
    // 自动计算字段
    @ProtocolField(order = 5, length = 4, dataType = DataType.UINT32, lengthFiled = true)
    private Integer totalLength;  // 自动计算包长度
    
    @ProtocolField(order = 6, length = 2, dataType = DataType.UINT16, crcFiled = true)
    private Integer crc;          // 自动计算CRC校验
}
```

#### 2. ProtocolField 属性详解

| 属性名 | 类型 | 说明 |
|-------|------|------|
| `order` | int | 字段顺序（从0开始） |
| `length` | int | 字段长度（字节数） |
| `dataType` | DataType | 数据类型 (UINT8, UINT16, UINT32, ASCII, HEX, LIST, BCD等) |
| `composite` | boolean | 是否为嵌套对象 |
| `computed` | boolean | 是否为计算字段（参与校验/长度计算） |
| `payload` | boolean | 是否为有效载荷（参与校验/长度计算） |
| `crcFiled` | boolean | 是否为CRC校验字段（自动计算并填充） |
| `lengthFiled` | boolean | 是否为长度字段（自动计算并填充） |
| `serialFiled` | boolean | 是否为流水号字段（自动递增） |
| `encryptedKey` | boolean | 是否为加密密钥标识 |
| `encryptedField` | boolean | 是否为加密字段 |
| `listElementType` | DataType | List元素类型 |
| `listElementSize` | int | List固定长度 |
| `precision` | int | 数值精度（小数位数） |
| `littleEndian` | boolean | 是否小端字节序 |
| `optional` | boolean | 是否可选字段 |
| `padding` | PaddingDirection | 填充方向 (LEFT, RIGHT) |
| `paddingChar` | String | 填充字符 |

#### 3. 使用协议引擎

```java
@Service
public class MessageService {
    
    // 获取默认协议引擎（增强版，带性能监控）
    private final ProtocolEngine<Object> engine = ProtocolEngineFactory.getDefaultEngine();
    
    public void processMessage(ByteBuf buffer) throws ProtocolException {
        // 解析消息
        UserMessage message = (UserMessage) engine.parse(buffer, UserMessage.class);
        
        // 验证消息
        ValidationResult validation = engine.validate(message);
        if (!validation.valid()) {
            throw new ProtocolException("验证失败: " + validation.message());
        }
        
        // 处理业务逻辑...
        
        // 序列化响应
        ByteBuf responseBuffer = Unpooled.buffer();
        engine.serialize(message, responseBuffer);
        
        // 发送响应...
    }
}
```