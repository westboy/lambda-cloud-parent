# Lambda Cloud Starter YKC

YKC（云快充）协议模块，基于 Netty 实现的高性能充电桩通讯框架，支持 YKC 协议的多个版本。

## 概述

`lambda-cloud-starter-ykc` 是 Lambda Cloud 微服务框架的充电桩通讯模块，专注于电动汽车充电桩的协议处理。该模块基于 Netty 实现，提供高性能的网络通信能力，并集成了 YKC 协议的完整实现。

## 功能特性

### 多版本协议支持

- **YKC v1.6**: 早期版本协议兼容
- **YKC v1.7**: 优化版本协议支持
- **YKC v2.0**: 最新版本协议实现

### 协议消息类型

#### 请求消息 (Request)

| 消息类型 | 说明 | 支持版本 |
|---------|------|---------|
| 登录 | 设备登录认证 | v1.6, v1.7, v2.0 |
| 心跳 | 保持连接活跃 | v1.6, v1.7, v2.0 |
| 计费模型 | 充电计费参数 | v1.6, v1.7, v2.0 |
| 计费模型验证 | 计费参数校验 | v1.6, v1.7, v2.0 |
| 充电开始 | 启动充电 | v1.6, v1.7, v2.0 |
| 充电结束 | 停止充电 | v1.6, v1.7, v2.0 |
| 远程启动 | 远程控制启动 | v1.6, v2.0 |
| 远程停止 | 远程控制停止 | v1.6, v1.7, v2.0 |
| 远程重启 | 设备远程重启 | v1.6 |
| 远程升级 | 固件远程升级 | v1.6 |
| 交易记录 | 充电交易数据 | v1.6, v1.7, v2.0 |
| 监控数据 | 实时监控信息 | v1.6, v1.7 |
| 时间同步 | 设备时间同步 | v1.6 |
| 场站锁控制 | 停车场锁控制 | v1.6 |
| 工作参数 | 设备工作参数 | v1.6 |
| 参数配置 | 参数配置查询 | v2.0 |
| BMS配置 | 电池管理系统配置 | v2.0 |
| 实时数据 | 实时数据读取 | v2.0 |
| 实时数据上传 | 实时数据上报 | v2.0 |
| 交易确认 | 交易确认 | v2.0 |
| 账户余额更新 | 账户余额同步 | v1.6 |

#### 响应消息 (Response)

每个请求消息都有对应的响应消息，包含：
- 登录响应
- 心跳响应
- 计费模型响应
- 计费模型验证响应
- 充电握手响应
- 充电开始响应
- 充电结束响应
- 远程启动响应
- 远程停止响应
- 交易记录响应
- 监控数据响应
- 参数配置响应
- BMS 相关信息响应
- 错误上报响应
- 场站锁状态响应
- 交易记录确认响应

## 核心组件

### 自动配置

- **YkcAutoConfiguration**: 自动配置类，负责注册 Netty 服务器和相关组件
- **YkcProperties**: 配置属性类，支持自定义配置

### 协议基类

- **YkcV16BasePayload**: v1.6 协议基类
- **YkcV17BasePayload**: v1.7 协议基类
- **YkcV20BasePayload**: v2.0 协议基类

## 依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-ykc</artifactId>
    <version>2025.1.1-SNAPSHOT</version>
</dependency>
```

### 传递依赖

该模块依赖以下模块：

- `lambda-cloud-core`: 核心工具类
- `lambda-cloud-starter-netty`: Netty 网络通信
- `lambda-cloud-starter-test`: 测试支持

## 配置说明

### 基础配置

```yaml
lambda:
  ykc:
    enabled: true
    server:
      port: 9000
      boss-thread-count: 1
      worker-thread-count: 4
```

### Netty 配置

详细 Netty 配置请参考 [lambda-cloud-starter-netty](../lambda-cloud-starter-netty/README.md)

## 使用示例

### 启动 YKC 服务

```java
@SpringBootApplication
public class YkcServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(YkcServerApplication.class, args);
    }
}
```

### 处理充电请求

```java
@Service
public class ChargingService {

    @Autowired
    private YkcMessageHandler messageHandler;

    public void handleStartCharging(YkcV16StartChargingRequest request) {
        // 处理充电开始请求
        // 验证计费模型
        // 记录交易
        // 返回响应
    }
}
```

### 协议版本选择

```java
// 使用 v1.6 协议
YkcV16StartChargingRequest request = new YkcV16StartChargingRequest();

// 使用 v1.7 协议
YkcV17StartChargingRequest request = new YkcV17StartChargingRequest();

// 使用 v2.0 协议
YkcV20StartChargingRequest request = new YkcV20StartChargingRequest();
```

## 消息流程

### 设备登录流程

```
1. 设备发送 LoginRequest (登录请求)
2. 服务器验证设备信息
3. 服务器返回 LoginResponse (登录响应)
4. 登录成功后进入正常通信状态
```

### 充电流程

```
1. 设备发送 StartChargingRequest (充电开始请求)
2. 服务器验证计费模型
3. 服务器返回 StartChargingResponse (充电开始响应)
4. 充电过程中设备发送 MonitoringDataRequest (监控数据)
5. 设备发送 ChargingEndRequest (充电结束请求)
6. 服务器返回 ChargingEndResponse (充电结束响应)
7. 设备发送 TransactionRecordRequest (交易记录)
8. 服务器返回 TransactionRecordResponse (交易记录响应)
```

### 心跳保持

```
设备定期发送 HeartbeatRequest (心跳请求)
服务器返回 HeartbeatResponse (心跳响应)
```

## 注意事项

1. **协议版本兼容**: 不同版本的协议消息结构有所不同，请根据设备实际情况选择合适的协议版本
2. **网络配置**: 确保 Netty 服务器端口配置正确，设备能够正常连接
3. **安全考虑**: 生产环境建议配置 SSL/TLS 加密传输
4. **性能优化**: 根据实际并发量调整 Netty 线程池配置
5. **日志记录**: 建议开启详细日志以便问题排查

## 版本信息

- **当前版本**: 2025.1.1-SNAPSHOT
- **Java 版本**: 21+
- **Spring Boot 版本**: 3.5.3
- **Netty 版本**: 4.1.122.Final
