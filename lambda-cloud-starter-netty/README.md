# Lambda Cloud Starter Netty

基于 Netty 的 Spring Boot Starter，提供高性能网络通信能力。

## 功能特性

- 自动配置 Netty 服务器
- 支持 TCP 长连接
- 可配置的线程池和连接参数
- 提供 Channel 管理和序列号生成
- 支持自定义 Pipeline 和 ServerBootstrap 配置

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

### 基本使用

1. 添加依赖
2. 配置端口和线程参数
3. 实现自定义的 Pipeline 配置
4. 启动应用，Netty 服务器将自动启动

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

## 注意事项

- 确保配置的端口未被占用
- 根据实际需求调整线程池大小
- 自定义处理器需要正确处理异常和资源释放