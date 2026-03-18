# lambda-cloud-starter-netty

`lambda-cloud-starter-netty` 提供 Netty 服务端启动能力与协议引擎能力，核心覆盖：

- Netty Server 自动装配与生命周期托管
- 可扩展的 `ServerBootstrap` 与 `ChannelPipeline` 定制点
- 基于注解的协议解析/序列化/校验引擎
- CRC/长度字段自动计算与校验
- 字段级可选加解密、List/Composite 复合结构解析

## 模块定位

- 面向“二进制协议接入”场景，提供统一协议层基础设施。
- 业务可只关注“协议模型定义 + pipeline handler 组装”，无需重复实现底层字节编解码。
- 与 Spring Boot 集成，支持配置化启动和 Bean 覆盖。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ NettyAutoConfiguration.java
└─ NettyExtendProperties.java

src/main/java/com/lambda/cloud/netty/
├─ NettyServer.java
├─ NettyChannelInitializer.java
├─ customizer/
│  ├─ ServerBootstrapConfigurationCustomizer.java
│  └─ ChannelPipelineConfigurationCustomizer.java
├─ repository/
│  ├─ ChannelRepository.java
│  └─ SerialNumberManager.java
├─ pool/ByteBufPool.java
├─ exception/ProtocolException.java
└─ protocol/
   ├─ annotation/*.java
   ├─ engine/*.java
   ├─ converter/*.java
   ├─ processor/*.java
   ├─ validation/*.java
   ├─ checksum/*.java
   ├─ encrypt/*.java
   ├─ scanner/ProtocolPayloadScanner.java
   └─ message/ProtocolPayloadRegistry.java
```

自动配置导入文件：

```text
src/main/resources/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

## 自动装配机制

### NettyAutoConfiguration

核心装配行为：

- 启用配置绑定：`@EnableConfigurationProperties(NettyExtendProperties.class)`
- 导入 `NettyServer`，交由 `SmartLifecycle` 管理启动/停止
- 注册 `EventLoopGroup`：
  - `bossGroup`
  - `workerGroup`
- 注册 `ServerBootstrap`，并注入：
  - `NettyChannelInitializer`
  - `ServerBootstrapConfigurationCustomizer`
- 注册 `InetSocketAddress`（使用 `spring.netty.server.tcp-port`）
- 注册 `ChannelRepository`

平台选择策略：

- `shouldEpoll`：仅在 Linux 且 `Epoll.isAvailable()` 时使用 Epoll
- 否则使用 NIO（`NioEventLoopGroup` + `NioServerSocketChannel`）

### NettyServer 生命周期

`NettyServer` 实现 `SmartLifecycle`：

- `start()`：执行 `serverBootstrap.bind(...)`
- `stop()`：关闭 `serverChannel`
- `isAutoStartup()`：由 `spring.netty.server.auto-start-up` 控制
- `getPhase()=Integer.MAX_VALUE`：在 Spring 生命周期末尾启动

## 配置模型

前缀：`spring.netty`

`NettyExtendProperties` 关键项：

- `server.tcp-port`：监听端口
- `server.worker-thread-count`：worker 线程数（>0 时按配置创建）
- `server.option-map`：透传到 `ServerBootstrap.option(...)`
- `server.auto-start-up`：是否自动启动
- `server.max-frame-length`：帧最大长度
- `server.dispatch-permits`：分发并发许可
- `server.idle-config.*`：
  - `reader-idle-time-seconds`
  - `writer-idle-time-seconds`
  - `all-idle-time-seconds`
  - `unit`

## 扩展点

### ServerBootstrapConfigurationCustomizer

用于补充 server 级配置，例如：

- `childOption(...)`
- `childHandler(...)` 外的 server 侧参数
- backlog、TCP 参数等

### ChannelPipelineConfigurationCustomizer

用于注入协议解码器、业务 handler、心跳 handler 等。  
`NettyChannelInitializer` 仅调用 `customizer.configuration(pipeline)`，不内置默认 handler 链。

## 协议引擎能力

### 核心抽象

- `ProtocolEngine<T>`：统一接口
  - `parse`
  - `serialize`
  - `validate`
  - `calculateLength`
  - `getMetadata`
- `ReflectionProtocolEngine`：当前主要实现
- `ProtocolEngineFactory`：引擎实例缓存与管理

### 注解模型

- `@ProtocolPayload`：定义消息级元数据
  - `frameType`
  - `crcAlgorithm`
  - `isFrame`
- `@ProtocolField`：定义字段级编解码规则
  - `order`、`length`、`dataType`
  - `computed`、`crcFiled`、`lengthFiled`
  - `payload`、`composite`
  - `encryptedKey`、`encryptedField`
- `@ProtocolValidation`：定义字段校验规则

### 解析/序列化链路

`ReflectionProtocolEngine` 处理流程：

1. 读取/构建 `ProtocolPayloadMetadata`
2. 遍历字段元数据并调用 `ProtocolFieldProcessor`
3. 解析时收集 `computed=true` 字段原始切片，完成后执行 CRC 校验
4. 序列化时先将 CRC/Length 字段置零占位，再回填真实值
5. 调用 `ValidationEngine` 执行规则校验

关键组件职责：

- `ProtocolFieldProcessor`：字段读写、List/Composite 动态长度处理、加密字段处理
- `ComputedProcessor`：CRC/长度计算、回填、校验
- `DataTypeConverterResolver`：数据类型转换器分发
- `ValidationEngine`：范围/长度/正则/自定义验证器执行

### 支持的数据类型

`ProtocolDataType` 支持：

- `HEX`
- `ASCII`
- `BCD`
- `BIT`
- `UINT8/16/32/64`
- `CP56TIME2A`
- `COMPOSITE`
- `LIST`

### 加密能力

- 默认加密服务：`DefaultEncryptionService`
- 触发条件：
  - 报文字段中存在 `encryptedKey=true` 且值为 `0x01`（或数值 1）
  - 字段标记 `encryptedField=true`
- 判断逻辑由 `EncryptionUtils.isEncryptionEnabled(...)` 控制

### CRC能力

- 算法入口：`ChecksumFactory`
- 支持：
  - `CRC16-CCITT`
  - `CRC16-IBM`
  - `CRC16-MAXIM`
  - `CRC16-USB`
  - `CRC16-X25`
  - `CRC16-XMODEM`
  - 默认 `CRC16-MODBUS`

## 运行时辅助组件

- `ChannelRepository`：维护连接 key -> `Channel` 映射
- `SerialNumberManager`：线程安全循环序号生成器（默认 1..0xFFFF）
- `ByteBufPool`：统一池化分配与安全释放
- `ProtocolPayloadScanner` + `ProtocolPayloadRegistry`：按 `frameType` 扫描注册协议类型

## 配置示例

```yaml
spring:
  netty:
    server:
      tcp-port: 9000
      worker-thread-count: 8
      auto-start-up: true
      max-frame-length: 2048
      dispatch-permits: 200
      option-map:
        SO_BACKLOG: 1024
        SO_REUSEADDR: true
      idle-config:
        reader-idle-time-seconds: 60
        writer-idle-time-seconds: 0
        all-idle-time-seconds: 180
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `io.netty:netty-all`
- `org.springframework.boot:spring-boot-starter`
- `org.ow2.asm:asm`
- `org.ow2.asm:asm-commons`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `ChannelPipelineConfigurationCustomizer` 没有默认实现；若业务未提供，`channelInitializer` 依赖无法满足。
- `spring.netty.server.boss-thread-count` 当前未在 `NettyAutoConfiguration` 使用。
- `ProtocolPayloadScanner` 仅注册 `@ProtocolPayload(isFrame=false)` 类型。
- `ProtocolPayloadRegistry` 为静态全局存储，多测试场景需要显式 `clear()`。
- `ValidationEngine` 的数值范围使用 `double` 处理，极大整数存在精度风险。
- 自动配置导入文件当前包含 `com.cx.autoconfig.NettyAutoConfiguration` 与 `ProtocolPayloadAutoConfiguration`，与本模块现有类不一致，存在装配风险。
