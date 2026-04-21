# lambda-cloud-starter-sms

`lambda-cloud-starter-sms` 提供统一短信发送抽象，支持阿里云、腾讯云两种生产通道，以及默认 Mock 通道，便于在开发/测试环境安全演练短信流程。

## 模块定位

- 对外暴露统一接口 `SmsMessageSender`，屏蔽厂商差异。
- 通过配置项切换生产/非生产发送行为。
- 在生产模式下按厂商配置装配实际短信发送器。
- 在非生产模式下默认使用 Mock 发送器，避免真实短信下发。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ SmsAutoConfiguration.java
└─ SmsProperties.java

src/main/java/com/lambda/cloud/sms/
├─ SmsMessageSender.java
├─ SmsISP.java
├─ model/SmsSendResult.java
├─ mock/MockSmsMessageSender.java
└─ sender/
   ├─ AliYunSmsMessageSender.java
   └─ TencentSmsMessageSender.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.SmsAutoConfiguration
```

## 自动装配机制

`SmsAutoConfiguration` 按 `lambda.sms.prod.enabled` 分支装配：

### 非生产模式（默认）

- 条件：`lambda.sms.prod.enabled=false` 或缺省
- 装配：`MockSmsMessageSender`
- 约束：仅当容器中不存在 `SmsMessageSender` 时注入

### 生产模式

- 条件：`lambda.sms.prod.enabled=true`
- 子分支：
  - `lambda.sms.aliyun.enabled=true` -> 注入 `AliYunSmsMessageSender`
  - `lambda.sms.tencent.enabled=true` -> 注入 `TencentSmsMessageSender`

## 统一发送接口约定

`SmsMessageSender` 定义三类能力：

- `sendVerifyCode(phone, code, expire)`
- `sendMessage(phone, templateId, params)`
- `isp()`

返回模型 `SmsSendResult` 字段：

- `success`：是否成功
- `id`：本地请求记录 ID
- `bizId`：厂商侧业务 ID
- `message`：厂商响应描述

## 配置模型

配置前缀：`lambda.sms`

### prod

- `enabled`：是否启用生产发送模式，默认 `false`

### aliyun

- `enabled`
- `region-id` 默认 `cn-hangzhou`
- `access-key-id`
- `access-key-secret`
- `domain` 默认 `dysmsapi.aliyuncs.com`
- `sign-name`
- `version` 默认 `2017-05-25`
- `action` 默认 `SENDSMS`
- `code-template-id`

### tencent

- `enabled`
- `app-id`
- `app-key`
- `sms-sign`
- `nation-code` 默认 `86`
- `code-template-id`

## 发送实现细节

### MockSmsMessageSender

- 仅打印日志，不调用外部网关。
- 始终返回 `success=true`。
- `isp()` 返回 `SmsISP.MOCK`。

### AliYunSmsMessageSender

- 初始化阶段创建 `DefaultAcsClient`。
- 验证码发送时将 `code/expire` 组装为 JSON 传入模板参数。
- 请求成功判定：响应 `Code == "OK"`。
- 每次请求生成本地 `id` 并透传 `OutId`。

### TencentSmsMessageSender

- 初始化阶段创建 `SmsSingleSender`。
- 验证码发送参数顺序：`[code, expire]`。
- 普通模板发送时 `params` 需为 JSON 数组字符串（会反序列化为 `List<String>`）。
- 请求成功判定：响应 `result == 0`。

## 使用示例

```yaml
lambda:
  sms:
    prod:
      enabled: true
    aliyun:
      enabled: true
      access-key-id: xxx
      access-key-secret: xxx
      sign-name: LAMBDA
      code-template-id: SMS_123456789
```

## 安全建议

- 不要在代码仓库中提交 `access-key-id/access-key-secret/app-key` 等敏感信息，建议通过环境变量、密钥管理系统或配置中心加密能力注入。
- 生产模式建议同时配置网关侧限流/防刷与告警，避免短信通道被滥用。

```java
@Service
public class VerifyCodeService {
    private final SmsMessageSender smsMessageSender;

    public VerifyCodeService(SmsMessageSender smsMessageSender) {
        this.smsMessageSender = smsMessageSender;
    }

    public boolean send(String phone, String code) {
        SmsSendResult result = smsMessageSender.sendVerifyCode(phone, code, 5);
        return result.isSuccess();
    }
}
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `com.lambda.cloud:lambda-cloud-core`
- `com.github.qcloudsms:qcloudsms:1.0.6`
- `com.aliyun:aliyun-java-sdk-core:4.5.25`
- `spring-boot-configuration-processor`（optional）

## 当前实现约束

- 生产模式下若阿里与腾讯同时启用，会同时注册两个 `SmsMessageSender`，按类型注入可能产生冲突。
- 生产模式下若未启用任何厂商，不会自动回退到 Mock，可能导致容器缺少 `SmsMessageSender`。
- `TencentSmsMessageSender#sendMessage` 要求 `templateId` 可转 `int`，否则会抛 `NumberFormatException`。
- `sendMessage(..., params)` 在腾讯通道要求 `params` 为 JSON 数组字符串，不是对象 JSON。
- 厂商异常处理仅记录错误日志并返回失败结果，不包含重试与熔断策略。
