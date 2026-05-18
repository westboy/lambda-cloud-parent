---
name: "lambda-cloud-security"
description: "Lambda Cloud 安全认证配置指南。当用户需要配置登录认证(表单/短信/HMAC/第三方)、权限校验、验证码、XSS防护或多loginType管理时调用。"
---

# Lambda Cloud 安全认证

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-security</artifactId>
</dependency>
```

## 配置总览

配置前缀: `lambda.security`

```yaml
lambda:
  security:
    sa-token:
      token-name: Authorization
      # 其他配置
      add-cors-header: true  # 默认 true
      is-share: true  # 默认 true
      is-read-cookie: false  # 默认 false
      cookie:
        domain: example.com
        path: /
        http-only: true  # 默认 true
        same-site: Lax  # 默认 Lax
      ignored:
        - /public/**
        - /v3/api-docs/**
        - /anon/**
      loginTypes:
        - login
    form:
      enabled: true
      loginProcessingUrl: /login
      parameters:
        username: username
        password: password
      lockStrategy:
        failureMaxTimes: 3
        duration: 1
        timeUnit: HOURS
    sms:
      enabled: true
      loginPath: /sms-login
      verifyPath: /sms-code
      code: code
      mobile: mobile
      validMinutes: 3
      resendSeconds: 60
    hmac:
      enabled: true
      clients:
        - appid: app001
          secret: your-secret-key
    thirdPartLogin:
      enabled: true
      loginPath: /thirdPart-login
      wxMa:
        enabled: true
        appId: your-wx-appid
        secret: your-wx-secret
    verify:
      url: /jcaptcha
      duration: 180
      timeUnit: SECONDS
      captchaCodeCount: 4
    xss-protected:
      enabled: true
      trusted:
        - your-domain.com
    form:
      captchaTrigger:
        enabled: true
        failureTriggerTimes: 2
```

## 表单登录

### 启用条件

`lambda.security.form.enabled=true`

### 登录链路

1. `FormAuthenticationProcessingFilter` 命中 `loginProcessingUrl`
2. 执行 `FormLoginValidator` 列表（含动态验证码验证器）
3. 校验用户、密码与锁定策略
4. 成功走 `CommonAuthenticationSuccessHandler`，失败走 `CommonAuthenticationFailureHandler`

### 实现 UserDetailService

`UserDetailService` 继承自 Sa-Token 的 `StpInterface`，提供用户认证与权限管理能力:

```java
public interface UserDetailService extends StpInterface {
    default LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        throw new AuthenticationException("用户名登录暂未实现！");
    }
    default LoginUser loginByMobile(String mobile, String loginType) throws AuthenticationException {
        throw new AuthenticationException("手机号登录暂未实现！");
    }
}
```

实现示例:

```java
@Component
public class UserDetailServiceImpl implements UserDetailService {
    @Override
    public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        UserEntity user = userService.getByUsername(username);
        if (user == null) {
            throw new AuthenticationException("用户不存在");
        }
        if (!user.isEnabled()) {
            throw new AuthenticationException("用户已被禁用");
        }
        return new DefaultLoginUser(user.getId(), user.getUsername(),
            user.getTenantId(), user.getAuthorities());
    }

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        return userService.getUserPermissions(loginId);
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        return userService.getUserRoles(loginId);
    }
}
```

### 自定义认证处理器

```java
@Component
public class CustomAuthSuccessHandler implements AuthenticationSuccessHandler {
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) {
        // 自定义成功响应
    }
}
```

### 动态验证码触发

当 `captchaTrigger.enabled=true` 且 `form.enableVerify=false` 时:
- 达到 `failureTriggerTimes` 次失败后要求验证码
- 未提供验证码抛 `CaptchaRequiredException`

## 短信登录

### 启用条件

`lambda.security.sms.enabled=true`

### 登录链路

1. 调用 `/sms-code` 发送验证码
2. `SmsVerifyCodeGenerateImpl` 负责发码（可叠加图形验证码防刷）
3. 调用 `/sms-login` 携带手机号和验证码登录
4. `SmsAuthenticationProcessingFilter` 完成认证

### 配置 SMS 发送

需同时引入 `lambda-cloud-starter-sms`:
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

## HMAC 签名认证

### 启用条件

`lambda.security.hmac.enabled=true`

### 认证链路

1. `HmacAuthenticationProcessingFilter` 解析请求签名
2. `HmacClientService` 按 appid 查客户端
3. 验证 HMAC 签名
4. 成功后创建 HMAC loginType 会话

### HMAC 签名生成

```java
// 使用 core 模块的 HmacGenerator
String base = HmacGenerator.baseString(appid, timestamp, queryMap, body);
String authorization = HmacGenerator.authorization(appid, secret, timestamp, base);

// 在请求头中添加
// Authorization: hmac appid:timestamp:signature
```

### Feign 调用签名

```java
@Bean
public HmacClientRequestInterceptor hmacInterceptor(
        @Value("${hmac.appid}") String appid,
        @Value("${hmac.secret}") String secret) {
    return new HmacClientRequestInterceptor(appid, secret);
}
```

## 第三方登录

### 启用条件

`lambda.security.thirdPartLogin.enabled=true`

### 微信小程序登录

```yaml
lambda:
  security:
    thirdPartLogin:
      wxMa:
        enabled: true
        appId: your-appid
        secret: your-secret
```

### 实现 ThirdPartyLoginService

```java
@Component
public class ThirdPartyLoginServiceImpl implements ThirdPartyLoginService {
    @Override
    public LoginUser loadThirdPartyUser(String thirdType, String authParam) {
        // 根据第三方类型和参数查询/创建用户
        // 返回 LoginUser 实现
    }
}
```

## XSS 防护

`lambda.security.xss-protected.enabled=true`

- `XSSDefendFilter` 过滤请求参数
- `XSSRequestWrapper` 包装请求，清理 XSS 脚本
- `trusted` 配置信任域集合

## 验证码

### 图形验证码

```yaml
lambda:
  security:
    verify:
      devMode: false       # 开发模式下使用固定验证码
      url: /jcaptcha
      duration: 180
      captchaCodeCount: 4
```

## Sa-Token 拦截链路

### 鉴权流程

1. `SaInterceptor` 通过 `SaTokenInterceptor` 接入
2. 过滤静态资源和错误处理器
3. 委派 `SecureInterceptor#handle()`
4. 默认执行 `stpLogic.checkLogin()`

### 同源令牌检查

- `SaServletFilter` 对 `/**` 生效，排除 ignored 路径
- 非 HMAC 请求且启用 `checkSameToken` 时，执行 `SaSameUtil.checkCurrentRequestToken()`

### 自定义鉴权

```java
@Component
public class CustomSecureInterceptor extends SecureInterceptor {
    @Override
    public boolean handle(HttpServletRequest request, HttpServletResponse response,
                         Object handler) throws Exception {
        // 自定义鉴权逻辑
        StpLogicUtils.getActiveStpLogic().checkPermission("user:list");
        return true;
    }
}
```

## 多 loginType 管理

通过 `StpLogicUtils` 管理多种登录类型:

```java
// 获取当前活跃的 StpLogic
StpLogic logic = StpLogicUtils.getActiveStpLogic();

// 获取指定类型的 StpLogic
StpLogic formLogic = StpLogicUtils.getStpLogic("form");

// 获取登录会话
SaSession session = StpLogicUtils.getSaSession(token);
```

## 安全最佳实践

1. **密码存储**: 使用 `StandardPasswordEncoder` 或 `Sha256PasswordEncoder`
2. **敏感接口**: 使用 Sa-Token 的 `@SaCheckPermission` 或 `@SaCheckRole` 注解
3. **HMAC 密钥**: 不要硬编码，使用环境变量或配置中心
4. **验证码**: 生产环境关闭 `devMode`
5. **锁定策略**: 合理设置 `failureMaxTimes` 和 `duration`，防止暴力破解
6. **XSS**: 对用户输入的 HTML 内容始终进行转义
