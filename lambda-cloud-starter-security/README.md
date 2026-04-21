# lambda-cloud-starter-security

`lambda-cloud-starter-security` 是基于 Sa-Token 的安全增强 starter，提供统一认证鉴权、验证码防护、HMAC 接口签名、第三方登录、XSS 防护与多登录类型管理能力。

## 模块定位

- 统一接入 Web/Reactive 两类安全拦截能力。
- 以自动装配方式组织表单登录、短信登录、HMAC、第三方登录等认证链路。
- 通过可插拔接口给业务暴露用户查询、权限模型和第三方账号映射扩展点。

## 目录结构（src/main）

```text
src/main/java/com/lambda/autoconfig/
├─ SecurityAutoConfiguration.java
└─ SecurityProperties.java

src/main/java/com/lambda/security/
├─ LoginMode.java / LoginResult.java / LoginError.java
├─ service/
│  ├─ UserDetailService.java
│  ├─ HmacClientService.java
│  └─ ThirdPartyLoginService.java
├─ handler/
│  ├─ AuthenticationSuccessHandler.java
│  ├─ AuthenticationFailureHandler.java
│  ├─ LogoutHandler.java
│  ├─ LogoutSuccessHandler.java
│  └─ impl/*.java
├─ inteceptor/
│  ├─ SaTokenInterceptor.java
│  └─ SecureInterceptor.java
├─ web/
│  ├─ AbstractAuthenticationProcessingFilter.java
│  ├─ form/*.java
│  ├─ sms/*.java
│  ├─ hmac/*.java
│  ├─ third/*.java
│  ├─ verify/*.java
│  └─ xss/*.java
├─ provider/
│  ├─ ThirdPartLoginProvider.java
│  └─ wx/*.java
└─ encode/*.java

src/main/resources/META-INF/spring/
└─ org.springframework.boot.autoconfigure.AutoConfiguration.imports
```

自动装配注册项：

```text
com.lambda.autoconfig.SecurityAutoConfiguration
```

## 自动装配机制

### 主配置入口

`SecurityAutoConfiguration` 启用 `SecurityProperties` 绑定，按条件装配子配置：

- `SaTokenConfiguration`：Sa-Token 核心拦截、同源令牌检查、多 loginType 初始化。
- `VerifyConfiguration`：验证码生成/校验服务和统一验证码过滤器。
- `SmsConfiguration`：短信验证码登录。
- `HmacConfiguration`：HMAC 签名认证。
- `FormConfiguration`：用户名密码登录、锁定策略、动态验证码、登出。
- `ThirdPartyConfiguration`：第三方登录（含微信小程序分支）。

### 关键条件开关

- `lambda.security.form.enabled`
- `lambda.security.sms.enabled`
- `lambda.security.hmac.enabled`
- `lambda.security.thirdPartLogin.enabled`
- `lambda.security.thirdPartLogin.wxMa.enabled`
- `lambda.security.xss-protected.enabled`
- `lambda.security.form.captcha-trigger.enabled`

### 默认过滤器顺序

- `VerifyCodeFilter`：`order=20`
- 认证过滤器（表单/短信/HMAC/第三方）：`order=30`
- `FormLogoutFilter`：`order=40`

## 配置模型

配置前缀：`lambda.security`

### sa-token

- 继承 Sa-Token 原生配置。
- 扩展项：
  - `ignored` 自定义忽略路径
  - `loginTypes` 动态注册多登录类型
- 内置忽略路径包含：`/public/**`、`/v3/**`、`/anon/**`、静态资源后缀等。
- Sa-Token 原生配置在本 starter 中使用 `lambda.security.sa-token` 作为绑定前缀（对应注入到 `SaTokenConfig` Bean），用于与业务侧 `sa-token.*` 配置隔离。

### form

- `enabled` 默认 `false`
- `loginPage` 默认 `/login.html`
- `loginProcessingUrl` 默认 `/login`
- `enableVerify` 默认 `false`
- `parameters.username/password` 默认 `username/password`
- `logout.logoutUrl` 默认 `/logout`
- `logout.logoutSuccessUrl` 默认 `/login?logout`
- `lockStrategy`：
  - `failureMaxTimes` 默认 `3`
  - `duration` 默认 `1`
  - `timeUnit` 默认 `HOURS`
- `captchaTrigger`：
  - `enabled` 默认 `false`
  - `failureTriggerTimes` 默认 `2`

### verify（图形验证码）

- `devMode` 默认 `false`
- `url` 默认 `/jcaptcha`
- `duration` 默认 `180`
- `timeUnit` 默认 `SECONDS`
- `captchaWidth/captchaHeight` 默认 `180/70`
- `captchaCodeCount` 默认 `4`

### sms

- `enabled` 默认 `false`
- `loginPath` 默认 `/sms-login`
- `verifyPath` 默认 `/sms-code`
- `code` 默认 `code`
- `mobile` 默认 `mobile`
- `validMinutes` 默认 `3`
- `resendSeconds` 默认 `60`
- `enableVerify` 默认 `false`
- `mock` 默认 `false`

### hmac

- `enabled` 默认 `false`
- `clients[]`：`appid/secret`

### thirdPartLogin

- `enabled` 默认 `false`
- `loginPath` 默认 `/thirdPart-login`
- `thirdName` 默认 `thirdType`
- `thirdAuthParam` 默认 `thirdAuthParam`
- `buildAuthorizationUrl` 默认 `buildAuthorizationUrl`
- `wxMa.enabled/appId/secret` 支持微信小程序登录。

### xss-protected

- `enabled` 默认 `false`
- `trusted`：信任域集合。

## 认证与鉴权链路

### Sa-Token 鉴权链路

1. `SaInterceptor` 通过 `SaTokenInterceptor` 接入。
2. `SaTokenInterceptor` 过滤静态资源和错误处理器。
3. 委派 `SecureInterceptor#handle(...)`。
4. 默认 `SecureInterceptor` 仅执行 `stpLogic.checkLogin()`。

### 同源令牌链路

- `SaServletFilter` 对 `/**` 生效，排除 ignored 路径。
- 非 HMAC 请求且启用 `checkSameToken` 时，执行 `SaSameUtil.checkCurrentRequestToken()`。
- 出错统一转为 `ErrorModel` JSON。

### 表单登录链路

1. `FormAuthenticationProcessingFilter` 命中 `loginProcessingUrl`。
2. 前置执行 `FormLoginValidator` 列表（含动态验证码验证器）。
3. 校验用户、密码与锁定策略。
4. 成功走 `CommonAuthenticationSuccessHandler`，失败走 `CommonAuthenticationFailureHandler`。

动态验证码触发逻辑：

- `captcha-trigger.enabled=true` 且 `form.enableVerify=false` 时启用。
- 达到失败阈值后要求验证码；未提供验证码会抛 `CaptchaRequiredException`。

### 短信登录链路

1. `SmsVerifyCodeGenerateImpl` 负责发码（可叠加图形验证码防刷）。
2. `SmsVerifyCodeValidationImpl` 负责验码。
3. `SmsAuthenticationProcessingFilter` 基于手机号完成登录。

### HMAC 认证链路

1. `HmacAuthenticationProcessingFilter` 解析请求签名并校验。
2. `HmacClientService` 按 `appid` 查客户端（默认 `MemoryHmacClientService`）。
3. `HmacAuthenticationSuccessHandler` 复用或创建 HMAC loginType 会话。

### 第三方登录链路

1. `ThirdPartAuthenticationProcessingFilter` 作为统一入口。
2. 按 `ThirdPartLoginProvider#support(...)` 选择具体 provider。
3. provider 调用 `ThirdPartyLoginService` 将第三方结果映射为 `LoginUser`。
4. 微信小程序场景通过 `WxMaService` + `WxMaLoginProvider` 接入。

## 关键扩展点

- `UserDetailService`：实现用户名/手机号登录与 Sa-Token 权限/角色查询。
- `SecureInterceptor`：扩展方法级或业务级访问控制。
- `FormLoginValidator`：扩展表单登录前校验规则。
- `HmacClientService`：替换 HMAC 客户端存储来源。
- `ThirdPartLoginProvider`：新增第三方平台登录适配。
- `ThirdPartyLoginService`：映射第三方身份到本地用户。

## 最小配置示例

```yaml
lambda:
  security:
    sa-token:
      token-name: Authorization
    form:
      enabled: true
      login-processing-url: /login
      lock-strategy:
        failure-max-times: 3
        duration: 1
        time-unit: HOURS
    verify:
      url: /jcaptcha
      duration: 180
      time-unit: SECONDS
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `cn.dev33:sa-token-spring-boot4-starter`
- `cn.dev33:sa-token-reactor-spring-boot4-starter`
- `cn.dev33:sa-token-redis-jackson`
- `com.lambda.cloud:lambda-cloud-starter-web`
- `com.lambda.cloud:lambda-cloud-starter-redis`
- `com.lambda.cloud:lambda-cloud-starter-sms`
- `org.jsoup:jsoup`
- `org.owasp.esapi:esapi`

## 当前实现约束

- 认证过滤器默认都映射到 `/*`，多模式并存时依赖各自 `support` 逻辑做请求分流。
- `FormConfiguration` 中 `UserDetailService` 以 `required=false` 注入，但登录过滤器运行仍依赖业务提供实现。
- `HmacConfiguration` 默认内存客户端实现基于配置初始化，不支持运行时热更新。
- 第三方登录开启后若没有可用 `ThirdPartLoginProvider`，启动会抛 `IllegalStateException`。
- 动态验证码仅在“未全局强制验证码”模式下生效（`form.enableVerify=false`）。
