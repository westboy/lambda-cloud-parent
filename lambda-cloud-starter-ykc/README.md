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
└─ META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports

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

## 协议消息定义

### V1.6 协议消息（帧类型码）

#### 上行消息（充电桩 -> 运营平台）

| 帧类型码 | 类名 | 消息名称 |
|----------|------|----------|
| 0x01 | YkcV16LoginRequest | 登录请求 |
| 0x03 | YkcV16HeartbeatRequest | 心跳请求 |
| 0x05 | YkcV16BillingModelVerificationRequest | 计费模型验证请求 |
| 0x09 | YkcV16BillingModelRequest | 计费模型请求 |
| 0x12 | YkcV16MonitoringDataRequest | 读取实时监测数据请求 |
| 0x31 | YkcV16StartChargingRequest | 充电桩主动申请启动充电 |
| 0x34 | YkcV16RemoteStartChargingRequest | 运营平台远程控制启机 |
| 0x36 | YkcV16RemoteStopChargingRequest | 运营平台远程停机 |
| 0x3B | YkcV16TransactionRecordRequest | 交易记录 |
| 0x42 | YkcV16AccountBalanceUpdateRequest | 远程账户余额更新 |
| 0x44 | YkcV16OfflineCardSyncRequest | 离线卡数据同步 |
| 0x46 | YkcV16OfflineCardClearRequest | 离线卡数据清除 |
| 0x48 | YkcV16OfflineCardQueryRequest | 离线卡数据查询 |
| 0x52 | YkcV16WorkingParamsSetRequest | 充电桩工作参数设置 |
| 0x56 | YkcV16TimeSyncRequest | 对时设置 |
| 0x58 | YkcV16BillingModelSetRequest | 计费模型设置 |
| 0x61 | YkcV16ParkLockStatusUploadRequest | 地锁数据上送 |
| 0x62 | YkcV16ParkLockControlRequest | 遥控地锁升锁与降锁命令 |
| 0x92 | YkcV16RemoteRebootRequest | 远程重启 |
| 0x94 | YkcV16RemoteUpdateRequest | 远程更新 |
| 0xA1 | YkcV16ParallelStartChargingRequest | 充电桩主动申请并充充电 |
| 0xA4 | YkcV16ParallelRemoteStartChargingRequest | 远程控制并充启机 |

#### 下行消息（运营平台 -> 充电桩）

| 帧类型码 | 类名 | 消息名称 |
|----------|------|----------|
| 0x02 | YkcV16LoginResponse | 登录响应 |
| 0x04 | YkcV16HeartbeatResponse | 心跳响应 |
| 0x06 | YkcV16BillingModelVerificationResponse | 计费模型验证应答 |
| 0x0A | YkcV16BillingModelResponse | 计费模型响应 |
| 0x13 | YkcV16MonitoringDataResponse | 上传实时监测数据 |
| 0x15 | YkcV16ChargingHandshakeResponse | 充电握手 |
| 0x17 | YkcV16ParameterConfigResponse | 参数配置 |
| 0x19 | YkcV16ChargingEndResponse | 充电结束 |
| 0x1B | YkcV16ErrorReportResponse | 错误报文 |
| 0x1D | YkcV16BmsAbortResponse | BMS中止 |
| 0x21 | YkcV16ChargerAbortResponse | 充电机中止 |
| 0x23 | YkcV16BmsDemandAndChargerOutputResponse | BMS需求与充电机输出 |
| 0x25 | YkcV16BmsInfoResponse | BMS信息 |
| 0x32 | YkcV16StartChargingResponse | 运营平台确认启动充电 |
| 0x33 | YkcV16RemoteStartChargingResponse | 远程启动充电命令回复 |
| 0x35 | YkcV16RemoteStopChargingResponse | 远程停机命令回复 |
| 0x40 | YkcV16TransactionRecordConfirmResponse | 交易记录确认 |
| 0x41 | YkcV16AccountBalanceUpdateResponse | 余额更新应答 |
| 0x43 | YkcV16OfflineCardSyncResponse | 离线卡数据同步应答 |
| 0x45 | YkcV16OfflineCardClearResponse | 离线卡数据清除应答 |
| 0x47 | YkcV16OfflineCardQueryResponse | 离线卡数据查询应答 |
| 0x51 | YkcV16WorkingParamsSetResponse | 充电桩工作参数设置应答 |
| 0x55 | YkcV16TimeSyncResponse | 对时设置应答 |
| 0x57 | YkcV16BillingModelSetResponse | 计费模型应答 |
| 0x63 | YkcV16ParkLockControlResponse | 地锁控制返回（上行） |
| 0x91 | YkcV16RemoteRebootResponse | 远程重启应答 |
| 0x93 | YkcV16RemoteUpdateResponse | 远程更新应答 |
| 0xA2 | YkcV16ParallelStartChargingResponse | 确认并充启动充电 |
| 0xA3 | YkcV16ParallelRemoteStartChargingResponse | 远程并充启机命令回复 |

---

### V2.0/V2.1 协议消息（帧类型码）

#### 上行消息（充电桩 -> 运营平台）

| 帧类型码 | 类名 | 消息名称 | 备注 |
|----------|------|----------|------|
| 0x01 | YkcV20LoginRequest | 充电桩登录认证请求 | V2.1新增字段 |
| 0x03 | YkcV20HeartbeatRequest | 充电桩心跳包 | |
| 0x05 | YkcV20BillingModelVerificationRequest | 计费模型验证请求 | |
| 0x09 | YkcV20BillingModelRequest | 充电桩计费模型请求 | |
| 0x12 | YkcV20ReadRealtimeDataRequest | 读取实时监测数据 | |
| 0x13 | YkcV20UploadRealtimeDataRequest | 上传实时监测数据 | V2.1新增字段 |
| 0x15 | YkcV20ChargingHandshakeRequest | 充电握手 | |
| 0x17 | YkcV20BmsConfigRequest | 参数配置 | |
| 0x19 | YkcV20ChargingEndRequest | 充电结束 | |
| 0x1B | YkcV20BmsErrorRequest | 错误报文 | |
| 0x1D | YkcV20BmsAbortRequest | 充电阶段BMS中止 | |
| 0x21 | YkcV20ChargerAbortRequest | 充电阶段充电机中止 | |
| 0x23 | YkcV20BmsDemandAndChargerOutputRequest | 充电过程BMS需求、充电机输出 | |
| 0x25 | YkcV20BmsInfoRequest | 充电过程BMS信息 | |
| 0x36 | YkcV20RemoteStopRequest | 运营平台远程停机 | |
| 0x3D | YkcV20TransactionRecordRequest | 交易记录 | V2.1结构变更 |
| 0x40 | YkcV20TransactionConfirmRequest | 交易记录确认 | |
| 0x42 | YkcV20AccountBalanceUpdateRequest | 远程账户余额更新 | |
| 0x44 | YkcV20OfflineCardSyncRequest | 离线卡数据同步 | |
| 0x46 | YkcV20OfflineCardClearRequest | 离线卡数据清除 | |
| 0x48 | YkcV20OfflineCardQueryRequest | 离线卡数据查询 | |
| 0x50 | YkcV20ErrorReportRequest | 设备故障上送 | V2.1新增 |
| 0x4B | YkcV20FaultResetReportRequest | 设备故障复位上送 | V2.1新增 |
| 0x4D | YkcV20TransactionRecordCallRequest | 交易记录召唤 | V2.1新增 |
| 0x4F | YkcV20ChargerStartFinishedRequest | 充电机启动完成 | V2.1新增 |
| 0x52 | YkcV20PowerModifyRequest | 功率修改 | |
| 0x56 | YkcV20TimeSyncRequest | 对时设置 | |
| 0x58 | YkcV20BillingModelSetRequest | 计费模型设置 | |
| 0x60 | YkcV20DefaultMaxPowerRequest | 默认最大功率下发 | |
| 0x5B | YkcV20QrCodeSetRequest | 二维码设置 | V2.1新增 |
| 0x5D | YkcV20PlatformConnectConfigRequest | 平台连接设置 | V2.1新增 |
| 0x5F | YkcV20WorkingParamsSetRequest | 参数设置 | V2.1新增 |
| 0x61 | YkcV20ParkLockStatusUploadRequest | 地锁数据上送 | |
| 0x62 | YkcV20ParkLockControlRequest | 遥控地锁升锁与降锁命令 | |
| 0x92 | YkcV20RemoteRebootRequest | 远程重启 | |
| 0x94 | YkcV20RemoteUpdateRequest | 远程更新 | V2.1增加MD5 |
| 0x96 | YkcV20KeyUpdateRequest | 密钥更新 | V2.1新增 |
| 0x98 | YkcV20LogCallRequest | 日志召唤 | V2.1新增 |
| 0xA1 | YkcV20ParallelStartChargingRequest | 充电桩主动申请并充充电 | |
| 0xA2 | YkcV20ParallelStartChargingConfirmRequest | 运营平台确认并充启动充电 | |
| 0xA4 | YkcV20ParallelRemoteStartRequest | 运营平台远程控制并充启机 | |
| 0xA5 | YkcV20StartChargingRequest | 充电桩主动申请启动充电 | |
| 0xA9 | YkcV20VinCodeReportRequest | 充电桩上报vin码 | V2.1新增 |

#### 下行消息（运营平台 -> 充电桩）

| 帧类型码 | 类名 | 消息名称 | 备注 |
|----------|------|----------|------|
| 0x02 | YkcV20LoginResponse | 登录认证应答 | V2.1新增密钥字段 |
| 0x04 | YkcV20HeartbeatResponse | 心跳包应答 | |
| 0x06 | YkcV20BillingModelVerificationResponse | 计费模型验证请求应答 | |
| 0x0A | YkcV20BillingModelResponse | 计费模型请求应答 | V2.1结构变更 |
| 0x35 | YkcV20RemoteStopReplyResponse | 远程停机命令回复 | |
| 0x41 | YkcV20AccountBalanceUpdateResponse | 余额更新应答 | |
| 0x43 | YkcV20OfflineCardSyncResponse | 离线卡数据同步应答 | |
| 0x45 | YkcV20OfflineCardClearResponse | 离线卡数据清除应答 | |
| 0x47 | YkcV20OfflineCardQueryResponse | 离线卡数据查询应答 | |
| 0x49 | YkcV20ErrorReportResponse | 设备故障上送回复确认 | V2.1新增 |
| 0x4A | YkcV20FaultResetReportResponse | 设备故障复位上送回复确认 | V2.1新增 |
| 0x4C | YkcV20TransactionRecordCallResponse | 交易记录召唤确认 | V2.1新增 |
| 0x4E | YkcV20ChargerStartFinishedResponse | 充电机启动完成应答 | V2.1新增 |
| 0x51 | YkcV20PowerModifyResponse | 功率修改应答 | |
| 0x55 | YkcV20TimeSyncResponse | 对时设置应答 | |
| 0x57 | YkcV20BillingModelSetResponse | 计费模型应答 | |
| 0x59 | YkcV20DefaultMaxPowerResponse | 默认最大功率下发应答 | |
| 0x5A | YkcV20QrCodeSetResponse | 二维码设置应答 | V2.1新增 |
| 0x5C | YkcV20PlatformConnectConfigResponse | 平台连接设置应答 | V2.1新增 |
| 0x5E | YkcV20WorkingParamsSetResponse | 参数设置应答 | V2.1新增 |
| 0x63 | YkcV20ParkLockControlResponse | 充电桩返回数据（上行） | |
| 0x91 | YkcV20RemoteRebootResponse | 远程重启应答 | |
| 0x93 | YkcV20RemoteUpdateResponse | 远程更新应答 | |
| 0x95 | YkcV20KeyUpdateResponse | 密钥更新应答 | V2.1新增 |
| 0x97 | YkcV20LogCallResponse | 日志召唤应答 | V2.1新增 |
| 0xA3 | YkcV20ParallelRemoteStartReplyResponse | 远程并充启机命令回复 | |
| 0xA6 | YkcV20StartChargingConfirmResponse | 运营平台确认启动充电 | V2.1新增字段 |
| 0xA7 | YkcV20RemoteStartReplyResponse | 远程启动充电命令回复 | |
| 0xAA | YkcV20VinCodeReportResponse | 充电桩上报vin码回复 | V2.1新增 |

---

### 版本差异对比

| 特性 | V1.6 | V2.0/V2.1 |
|------|------|-----------|
| 帧类型码格式 | 0x前缀 (如0x01) | 无前缀 (如01) |
| 数据长度 | 1字节 | 2字节 |
| 发送时间 | 无 | CP56Time2a格式 |
| 登录帧字段 | 基础字段 | 增加Token、手机号、网络制式、经纬度等 |
| 启动充电帧 | 0x31/0x32 | 0xA5/0xA6 (新增) + 0x34/0x33 |
| 停止充电帧 | 0x36/0x35 | 0x36/0x35 (保持) |
| 交易记录帧 | 0x3B | 0x3D (结构变更) |
| 故障管理 | 无 | 0x49/0x50, 0x4A/0x4B |
| 交易记录召唤 | 无 | 0x4C/0x4D |
| 充电机启动完成 | 无 | 0x4E/0x4F |
| 二维码设置 | 无 | 0x5A/0x5B |
| 平台连接设置 | 无 | 0x5C/0x5D |
| 参数设置 | 0x52 | 0x5F |
| VIN码上报 | 无 | 0xA9/0xAA |
| 密钥更新 | 无 | 0x95/0x96 |
| 日志召唤 | 无 | 0x97/0x98 |

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
