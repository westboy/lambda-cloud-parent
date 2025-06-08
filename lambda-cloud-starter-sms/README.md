# Lambda Cloud SMS Starter

基于Spring Boot的短信服务模块，支持阿里云和腾讯云短信服务。

## 功能特性

- 支持阿里云短信服务
- 支持腾讯云短信服务
- 提供mock短信服务用于测试
- 自动配置短信服务发送器

## 配置项

```yaml
lambda:
  sms:
    prod:
      enabled: false # 是否启用生产环境
    aliyun:
      enabled: false # 是否启用阿里短信服务
      regionId: cn-hangzhou # API支持的RegionID
      accessKeyId: # 用于标识用户
      accessKeySecret: # 用来验证用户的密钥
      domain: dysmsapi.aliyuncs.com # 接口调用地址
      signName: # 短信签名
      version: 2017-05-25 # API版本号
      codeTemplateId: # 短信验证码模板id
    tencent:
      enabled: false # 是否启用腾讯短信服务
      appId: # 短信应用SDK AppID
      appKey: # 短信应用SDK AppKey
      smsSign: # 签名
      nationCode: 86 # 国家码
      codeTemplateId: # 短信验证码模板id
```

## 使用示例

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-sms</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 配置短信服务

```yaml
lambda:
  sms:
    prod:
      enabled: true
    aliyun:
      enabled: true
      accessKeyId: your-access-key-id
      accessKeySecret: your-access-key-secret
      signName: your-sign-name
      codeTemplateId: SMS_123456789
```

### 3. 发送短信

```java
@Autowired
private SmsMessageSender smsMessageSender;

public void sendVerifyCode(String phone, String code) {
    SmsSendResult result = smsMessageSender.sendVerifyCode(phone, code, 5);
    if (!result.isSuccess()) {
        log.error("短信发送失败: {}", result.getMessage());
    }
}
```

## 注意事项

1. 启用生产环境需要配置正确的accessKeyId和accessKeySecret
2. 短信模板需要在对应云平台预先申请
3. 默认使用mock短信服务，不会实际发送短信
4. 阿里云和腾讯云短信服务不能同时启用
