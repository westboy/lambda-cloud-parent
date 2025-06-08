# Lambda Cloud Security Starter

基于Spring Boot的安全认证模块，提供多种认证方式和安全防护功能。

## 功能特性

- 多种认证方式：表单登录、短信验证码、HMAC、第三方登录
- 集成Sa-Token进行权限控制
- XSS防护
- 验证码支持
- 登录限制策略
- 支持微信小程序登录

## 配置项

```yaml
lambda:
  security:
    # Sa-Token配置
    sa-token:
      enableMethodAuthentication: true # 是否启用注解鉴权
      check-same-token: true # 是否检查Same-Token
      ignored: # 忽略拦截的路径
        - /public/**

    # 表单登录配置
    form:
      enabled: true # 是否启用表单登录
      loginPage: "/login.html" # 登录页面
      loginProcessingUrl: "/login" # 登录处理URL
      enableVerify: false # 是否启用验证码
      parameters: # 表单参数名
        username: "username"
        password: "password"
      lockStrategy: # 登录锁定策略
        failureMaxTimes: 3 # 最大失败次数
        duration: 1 # 锁定时长
        timeUnit: HOURS # 时间单位

    # HMAC认证配置
    hmac:
      enabled: false # 是否启用HMAC认证
      clients: # 客户端配置
        - appid: "client1"
          secret: "secret1"

    # 短信验证码登录配置
    sms:
      enabled: false # 是否启用短信登录
      loginPath: "/sms-login" # 短信登录URL
      verifyPath: "/sms-code" # 验证码获取URL
      code: "code" # 验证码参数名
      mobile: "mobile" # 手机号参数名
      validMinutes: 3 # 验证码有效期(分钟)
      resendSeconds: 60 # 重发间隔(秒)

    # 第三方登录配置
    thirdPartLogin:
      enabled: false # 是否启用第三方登录
      loginPath: "/thirdPart-login" # 第三方登录URL
      wxMa: # 微信小程序配置
        enabled: false
        appId: ""
        secret: ""

    # XSS防护配置
    xss-protected:
      enabled: false # 是否启用XSS防护
      trusted: # 信任的URL
        - "/api/trusted"

    # 验证码配置
    verify:
      devMode: false # 开发模式
      url: "/jcaptcha" # 验证码获取URL
      duration: 180 # 有效期(秒)
      timeUnit: SECONDS # 时间单位
      captchaWidth: 180 # 验证码宽度
      captchaHeight: 70 # 验证码高度
      captchaCodeCount: 4 # 验证码字符数
```

## 使用示例

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-sercurity</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 登录实现

```java
@Service
public class UserDetailServiceImpl implements UserDetailService {
    @Override
    public SimpleLoginUser loadUserByUsername(String username) {
        // 实现用户信息加载逻辑
        return new SimpleLoginUser(username, "加密后的密码", "角色列表");
    }
}
```

### 3. 权限控制

```java
@RestController
public class DemoController {
    @SaCheckLogin
    @GetMapping("/secure")
    public String secure() {
        return "需要登录才能访问";
    }
}
```

## 注意事项

1. 启用短信验证码登录需要配置短信发送服务
2. 启用微信小程序登录需要配置正确的appId和secret
3. XSS防护会过滤请求参数中的潜在危险内容
4. 表单登录的锁定策略基于Redis实现
5. 默认忽略路径包括：/public/**, /v3/**, /anon/**, *.html, *.css, *.ico, *.js
