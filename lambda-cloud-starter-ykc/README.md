# lambda-cloud-starter-ykc

`lambda-cloud-starter-ykc` 提供云快充协议（YKC）消息模型与协议载荷自动扫描能力，依托 `lambda-cloud-starter-netty` 的协议引擎完成帧解析与序列化。

## 模块定位

- 提供云快充 1.6 / 1.7 / 2.0 协议消息定义（`@ProtocolPayload` + `@ProtocolField`）。
- 提供应用启动后的协议载荷自动扫描与注册入口。
- 为业务侧 Netty 协议处理器提供可直接复用的数据模型。
- 内置协议文档对照材料，便于字段对齐与验收。

## 目录结构（核心）

```text
src/main/java/com/lambda/autoconfig/
├─ YkcAutoConfiguration.java
└─ YkcProperties.java

src/main/java/com/lambda/cloud/ykc/message/
├─ v16/
│  ├─ YkcV16BasePayload.java
│  ├─ req/*.java
│  └─ resp/*.java
├─ v17/
│  ├─ YkcV17BasePayload.java
│  ├─ req/*.java
│  └─ resp/*.java
└─ v20/
   ├─ YkcV20BasePayload.java
   ├─ req/*.java
   └─ resp/*.java

src/main/resources/
└─ spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

docs/
├─ 云快充平台协议V1.6_完整报文参数对照表.md
├─ 云快充平台协议V1.7_完整报文参数对照表.md
└─ 云快充平台协议V2.1.0_完整报文参数对照表.md
```

自动装配注册项：

```text
com.lambda.autoconfig.YkcAutoConfiguration
```

## 自动装配机制

`YkcAutoConfiguration` 主要行为：

- 启用配置绑定：`YkcProperties`（前缀 `lambda.protocol.scanner`）
- 注册 `ProtocolPayloadScanner` Bean
- 在 `ApplicationReadyEvent` 触发时执行 `scanAndRegister(basePackages)`

扫描逻辑：

1. 若 `lazy-init=true`，跳过启动时自动扫描。
2. 若未配置 `base-packages`，默认扫描 `com.lambda`。
3. 扫描异常时：
   - `fail-on-error=false`：仅记录日志
   - `fail-on-error=true`：抛出异常中断启动

## 配置模型

配置前缀：`lambda.protocol.scanner`

- `enabled` 默认 `true`
- `base-packages` 扫描包数组
- `lazy-init` 默认 `false`
- `fail-on-error` 默认 `false`
- `scan-sub-packages` 默认 `true`
- `exclude-packages`
- `verbose` 默认 `false`

示例：

```yaml
lambda:
  protocol:
    scanner:
      base-packages:
        - com.lambda.cloud.ykc.message
      lazy-init: false
      fail-on-error: true
```

## 协议模型设计

### 通用注解语义

消息类基于以下注解描述编解码元数据：

- `@ProtocolPayload`
  - 描述帧类型、协议名、版本、是否为帧
- `@ProtocolField`
  - 描述字段顺序、长度、字节序、数据类型、是否校验字段等

常见字段标志：

- `lengthFiled=true`：长度字段
- `crcFiled=true`：校验字段
- `computed=true`：引擎计算字段
- `composite=true`：组合对象字段
- `encryptedField=true` / `encryptedKey=true`：加密相关字段

### 基础帧模型

- `YkcV16BasePayload`
  - 帧头 + 长度 + 序列号 + 加密标记 + 帧类型 + 数据域 + CRC
- `YkcV17BasePayload<T>`
  - 泛型 `detail` 结构，起始符默认 `68`
- `YkcV20BasePayload<T>`
  - 新增 `postTime(CP56TIME2A)` 字段，CRC 长度与前版本差异化

### 业务消息组织

- 各版本按 `req` / `resp` 目录划分。
- 具体消息通过 `@ProtocolPayload(frameType = "...")` 标识帧类型。
- 例如：
  - v1.6 登录请求 `frameType = 0x01`
  - v1.7 登录请求载荷类会在构造函数中设置 `frameType = "01"`
  - v2.0 登录请求 `frameType = "01"`

## 与协议引擎协作

本模块不直接提供 Netty Server 启停逻辑，主要提供“消息模型 + 自动扫描注册”。

常见协作方式：

1. 使用 `ReflectionProtocolEngine` 作为编解码引擎。
2. 将具体 `frameType -> payload class` 注册到 `ProtocolPayloadRegistry`。
3. 基于 `base payload` 做解析/序列化。

测试目录中的 `YkcV16BillingMessageTest`、`YkcV17LoginMessageTest` 给出了典型使用方式。

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.lambda.cloud:lambda-cloud-core`
- `com.lambda.cloud:lambda-cloud-starter-netty`
- `com.lambda.cloud:lambda-cloud-starter-test`（test scope）

## 当前实现约束

- `YkcProperties.enabled` 当前未在自动配置流程中实际参与条件判断。
- `scan-sub-packages`、`exclude-packages`、`verbose` 字段当前未被 `YkcAutoConfiguration` 使用。
- 自动装配导入文件位于 `resources/spring/`，与常见 `META-INF/spring/` 目录习惯不同。
- 本模块只提供协议消息定义与扫描，不包含独立的连接管理、会话管理与业务处理编排。
- 默认扫描包为 `com.lambda`，若业务项目包路径不在该范围会出现未注册消息类问题。
