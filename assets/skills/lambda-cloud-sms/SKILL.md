---
name: "lambda-cloud-sms"
description: "Lambda Cloud 短信服务配置指南。当用户需要集成阿里云短信、腾讯云短信或Mock短信发送时调用。"
---

# Lambda Cloud 短信服务

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-sms</artifactId>
</dependency>
```

## 配置

配置前缀: `lambda.sms`

### Mock 模式 (默认, 开发测试用)

```yaml
lambda:
  sms:
    prod:
      enabled: false  # 默认值
```

### 阿里云短信

```yaml
lambda:
  sms:
    prod:
      enabled: true
    aliyun:
      enabled: true
      region-id: cn-hangzhou
      access-key-id: your-access-key-id
      access-key-secret: your-access-key-secret
      domain: dysmsapi.aliyuncs.com
      sign-name: LAMBDA
      version: "2017-05-25"
      action: SENDSMS
      code-template-id: SMS_123456789
```

### 腾讯云短信

```yaml
lambda:
  sms:
    prod:
      enabled: true
    tencent:
      enabled: true
      app-id: your-app-id
      app-key: your-app-key
      sms-sign: LAMBDA
      nation-code: "86"
      code-template-id: 123456
```

## 使用方式

```java
@Service
public class VerifyCodeService {

    @Autowired
    private SmsMessageSender smsMessageSender;

    // 发送验证码
    public boolean sendVerifyCode(String phone, String code) {
        SmsSendResult result = smsMessageSender.sendVerifyCode(phone, code, 5);
        return result.isSuccess();
    }

    // 发送模板消息
    public boolean sendMessage(String phone, String templateId, String params) {
        SmsSendResult result = smsMessageSender.sendMessage(phone, templateId, params);
        return result.isSuccess();
    }

    // 获取短信服务商
    public SmsISP getISP() {
        return smsMessageSender.isp();
    }
}
```

## SmsSendResult 结果模型

| 字段 | 说明 |
|------|------|
| `success` | 是否成功 |
| `id` | 本地请求记录 ID |
| `bizId` | 厂商侧业务 ID |
| `message` | 厂商响应描述 |

## SmsISP 服务商枚举

| 值 | 说明 |
|------|------|
| `MOCK` | Mock 模式 |
| `ALIYUN` | 阿里云 |
| `TENCENT` | 腾讯云 |

## 各实现细节

### MockSmsMessageSender

- 仅打印日志, 不调用外部网关
- 始终返回 `success=true`
- `isp()` 返回 `SmsISP.MOCK`

### AliYunSmsMessageSender

- 初始化创建 `DefaultAcsClient`
- 验证码参数: JSON 格式 `{\"code\":\"1234\",\"expire\":\"5\"}`
- 成功判定: 响应 `Code == "OK"`
- 每次请求生成本地 `id` 并透传 `OutId`

### TencentSmsMessageSender

- 初始化创建 `SmsSingleSender`
- 验证码参数顺序: `[code, expire]`
- 模板发送: `params` 需为 JSON 数组字符串
- 成功判定: 响应 `result == 0`

## 与安全模块集成

短信服务常与 `lambda-cloud-starter-security` 的短信登录功能配合使用:

```yaml
lambda:
  security:
    sms:
      enabled: true
      loginPath: /sms-login
      verifyPath: /sms-code
      code: code
      mobile: mobile
      validMinutes: 3
      resendSeconds: 60
      mock: false
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

## 完整示例

```java
@RestController
@RequestMapping("/api/sms")
public class SmsController {

    @Autowired
    private SmsMessageSender smsMessageSender;

    @PostMapping("/send-code")
    public Result<String> sendVerifyCode(@RequestParam String phone) {
        String code = String.valueOf((int) (Math.random() * 9000 + 1000));
        SmsSendResult result = smsMessageSender.sendVerifyCode(phone, code, 5);

        if (result.isSuccess()) {
            // 保存验证码到 Redis
            redisHelper.set("sms:code:" + phone, code, 5, TimeUnit.MINUTES);
            return Result.success("发送成功");
        }
        return Result.fail("发送失败: " + result.getMessage());
    }

    @PostMapping("/verify-code")
    public Result<Boolean> verifyCode(@RequestParam String phone, @RequestParam String code) {
        String savedCode = redisHelper.get("sms:code:" + phone, String.class);
        return Result.success(code.equals(savedCode));
    }
}
```

## 最佳实践

1. **敏感信息**: 不要硬编码 access-key, 使用环境变量或配置中心
2. **防刷限制**: 结合 Redis 实现发送频率限制
3. **Mock 测试**: 开发测试环境使用 Mock 模式, 避免真实短信下发
4. **验证码有效期**: 合理设置 `validMinutes`, 一般 3-5 分钟
5. **重发间隔**: 设置 `resendSeconds` 防止频繁发送
6. **错误处理**: 处理短信发送失败场景, 提供重试机制
