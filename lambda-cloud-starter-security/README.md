# Lambda Cloud Security Starter

基于Spring Boot的企业级安全认证模块，提供完整的多种认证方式和安全防护功能。

## 功能特性

### 🔐 多种认证方式
- **表单登录认证**：传统用户名密码登录，支持验证码和登录锁定策略
- **短信验证码登录**：基于手机号的无密码登录方式
- **HMAC签名认证**：基于HMAC-SHA256的API安全认证
- **第三方登录**：支持微信小程序等第三方平台登录

### 🛡️ 安全防护
- **Sa-Token集成**：完整的权限控制和会话管理
- **XSS防护**：自动过滤和转义危险脚本内容
- **验证码支持**：图形验证码防止机器人攻击
- **登录限制策略**：基于Redis的登录失败锁定机制
- **CSRF防护**：跨站请求伪造攻击防护

### 📱 移动端支持
- **微信小程序登录**：完整的微信小程序授权登录流程
- **移动端适配**：支持移动端的认证和授权

### 🚀 高性能特性
- **Redis缓存**：验证码和会话信息缓存
- **内存存储**：HMAC客户端信息内存存储
- **异步处理**：非阻塞的认证处理流程

## 架构设计

### 核心组件

```
┌─────────────────────────────────────────────────────────────┐
│                    Filter Chain                             │
├─────────────────────────────────────────────────────────────┤
│  XSSDefendFilter → HmacAuthenticationProcessingFilter →     │
│  FormAuthenticationProcessingFilter →                       │
│  SmsAuthenticationProcessingFilter →                        │
│  ThirdPartAuthenticationProcessingFilter                    │
└─────────────────────────────────────────────────────────────┘
                              ↓
┌─────────────────────────────────────────────────────────────┐
│                    Authentication Handlers                  │
├─────────────────────────────────────────────────────────────┤
│  • HmacAuthenticationSuccessHandler                         │
│  • CommonAuthenticationSuccessHandler                       │
│  • CommonAuthenticationFailureHandler                       │
└─────────────────────────────────────────────────────────────┘
```

## 配置项

### 完整配置示例

```yaml
lambda:
  security:
    # Sa-Token扩展配置
    sa-token:
      # 基础Sa-Token配置（继承SaTokenConfig的所有属性）
      token-name: "Authorization"  # Token名称
      timeout: 2592000            # Token有效期(秒)
      activity-timeout: -1        # Token临时有效期(秒)
      is-concurrent: true         # 是否允许同一账号并发登录
      is-share: true             # 在多人登录同一账号时，是否共用一个token
      max-login-count: 12        # 同一账号最大登录数量
      
      # Lambda Cloud扩展配置
      enableMethodAuthentication: true # 是否启用注解鉴权
      ignored: # 忽略拦截的路径（会与默认路径合并）
        - "/custom/**"
        - "/api/public/**"
      # 默认忽略路径：/public/**, /v3/**, /anon/**, *.html, *.css, *.ico, *.js
      
      loginTypes: # 多登录类型配置
        - key: "admin"
          value: "管理员登录"
        - key: "user" 
          value: "普通用户登录"

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
      
      rateStrategy: # 限流策略
        maxTimes: 10 # 最大请求次数
        duration: 1 # 时间窗口长度
        timeUnit: HOURS # 时间窗口单位
      
      logout: # 登出配置
        logoutUrl: "/logout" # 登出处理URL
        logoutSuccessUrl: "/login?logout" # 登出成功跳转URL

    # HMAC认证配置
    hmac:
      enabled: false # 是否启用HMAC认证
      clients: # 客户端配置
        - appid: "client1"
          secret: "secret1"
        - appid: "client2"
          secret: "secret2"

    # 短信验证码登录配置
    sms:
      enabled: false # 是否启用短信登录
      loginPath: "/sms-login" # 短信登录URL
      verifyPath: "/sms-code" # 验证码获取URL
      code: "code" # 验证码参数名
      mobile: "mobile" # 手机号参数名
      validMinutes: 3 # 验证码有效期(分钟)
      resendSeconds: 60 # 重发间隔(秒)
      enableVerify: false # 是否启用图形验证码
      mock: false # 测试模式开关

    # 第三方登录配置
    thirdPartLogin:
      enabled: false # 是否启用第三方登录
      loginPath: "/thirdPart-login" # 第三方登录URL
      thirdName: "thirdType" # 第三方类型参数名
      thirdAuthParam: "thirdAuthParam" # 第三方认证参数名
      buildAuthorizationUrl: "buildAuthorizationUrl" # 构建授权URL参数名
      
      wxMa: # 微信小程序配置
        enabled: false
        appId: "your_wx_appid" # 微信小程序AppID
        secret: "your_wx_secret" # 微信小程序Secret

    # XSS防护配置
    xssProtected:
      enabled: false # 是否启用XSS防护
      trusted: # 信任的域名集合
        - "trusted-domain.com"
        - "internal.company.com"

    # 验证码配置
    verify:
      devMode: false # 开发模式开关
      url: "/jcaptcha" # 验证码获取URL
      duration: 180 # 有效期
      timeUnit: SECONDS # 有效期时间单位
      captchaType: "math" # 验证码类型
      captchaWidth: 180 # 验证码图片宽度
      captchaHeight: 70 # 验证码图片高度
      captchaCodeCount: 4 # 验证码字符数量
      captchaNumberLength: 1 # 数字长度
```

### 配置说明

#### Sa-Token扩展配置 (`sa-token`)
- **基础配置**：继承Sa-Token的所有标准配置属性
- **`enableMethodAuthentication`**：启用方法级别的权限注解（@SaCheckLogin、@SaCheckRole等）
- **`ignored`**：自定义忽略路径，会与默认忽略路径合并
- **`loginTypes`**：多登录类型配置，支持不同类型的用户登录
- **默认忽略路径**：`/public/**`, `/v3/**`, `/anon/**`, `*.html`, `*.css`, `*.ico`, `*.js`

#### 表单登录配置 (`form`)
- **`enabled`**：表单登录功能总开关
- **`loginPage`**：登录页面路径
- **`loginProcessingUrl`**：登录处理URL
- **`enableVerify`**：是否启用图形验证码
- **`parameters`**：自定义表单参数名（username、password）
- **`lockStrategy`**：登录失败锁定策略
  - `failureMaxTimes`：最大失败次数
  - `duration`：锁定时长
  - `timeUnit`：时间单位
- **`rateStrategy`**：请求限流策略
  - `maxTimes`：时间窗口内最大请求次数
  - `duration`：时间窗口长度
  - `timeUnit`：时间窗口单位
- **`logout`**：登出相关配置
  - `logoutUrl`：登出处理URL
  - `logoutSuccessUrl`：登出成功跳转URL

#### HMAC认证配置 (`hmac`)
- **`enabled`**：HMAC认证功能总开关
- **`clients`**：HMAC客户端列表
  - `appid`：客户端应用标识
  - `secret`：客户端密钥（用于HMAC签名）

#### 短信登录配置 (`sms`)
- **`enabled`**：短信登录功能总开关
- **`loginPath`**：短信登录处理URL
- **`verifyPath`**：短信验证码获取URL
- **`code`**：验证码参数名
- **`mobile`**：手机号参数名
- **`validMinutes`**：验证码有效期（分钟）
- **`resendSeconds`**：验证码重发间隔（秒）
- **`enableVerify`**：是否启用图形验证码
- **`mock`**：测试模式开关（不真正发送短信）

#### 第三方登录配置 (`thirdPartLogin`)
- **`enabled`**：第三方登录功能总开关
- **`loginPath`**：第三方登录处理URL
- **`thirdName`**：第三方类型参数名
- **`thirdAuthParam`**：第三方认证参数名
- **`buildAuthorizationUrl`**：构建授权URL参数名
- **`wxMa`**：微信小程序配置
  - `enabled`：微信小程序登录开关
  - `appId`：微信小程序AppID
  - `secret`：微信小程序Secret

#### XSS防护配置 (`xssProtected`)
- **`enabled`**：XSS防护功能总开关
- **`trusted`**：信任的域名集合（不进行XSS过滤）

#### 验证码配置 (`verify`)
- **`devMode`**：开发模式开关（跳过验证码验证）
- **`url`**：验证码获取URL
- **`duration`**：验证码有效期
- **`timeUnit`**：有效期时间单位
- **`captchaType`**：验证码类型
- **`captchaWidth`**：验证码图片宽度
- **`captchaHeight`**：验证码图片高度
- **`captchaCodeCount`**：验证码字符数量
- **`captchaNumberLength`**：数字长度

## 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-security</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 基础配置

```yaml
lambda:
  security:
    # 启用Sa-Token注解鉴权
    sa-token:
      enableMethodAuthentication: true
      token-name: "Authorization"
      timeout: 2592000
    
    # 启用表单登录
    form:
      enabled: true
      enableVerify: true
      loginPage: "/login.html"
      loginProcessingUrl: "/login"
```

### 3. 实现用户服务

```java
@Service
public class UserDetailServiceImpl implements UserDetailService {
    
    @Autowired
    private UserMapper userMapper;
    
    @Override
    public SimpleLoginUser loadUserByUsername(String username) {
        // 从数据库加载用户信息
        User user = userMapper.findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        
        // 构建登录用户对象
        return SimpleLoginUser.builder()
            .username(user.getUsername())
            .password(user.getPassword()) // 已加密的密码
            .roles(user.getRoles()) // 用户角色列表
            .permissions(user.getPermissions()) // 用户权限列表
            .enabled(user.isEnabled())
            .build();
    }
}
```

### 4. 权限控制示例

```java
@RestController
@RequestMapping("/api")
public class DemoController {
    
    // 需要登录
    @SaCheckLogin
    @GetMapping("/user/info")
    public Result getUserInfo() {
        String userId = StpUtil.getLoginId().toString();
        return Result.success(userService.getUserInfo(userId));
    }
    
    // 需要特定角色
    @SaCheckRole("admin")
    @GetMapping("/admin/users")
    public Result getUsers() {
        return Result.success(userService.getAllUsers());
    }
    
    // 多角色检查（OR模式）
    @SaCheckRole(value = {"admin", "manager"}, mode = SaMode.OR)
    @GetMapping("/admin/reports")
    public Result getReports() {
        return Result.success(reportService.getAllReports());
    }
    
    // 多账号体系角色检查
    @SaCheckRole(value = "admin", type = "user")
    @PostMapping("/user/admin-operation")
    public Result userAdminOperation() {
        // 检查user账号体系下的admin角色
        userService.adminOperation();
        return Result.success();
    }
}

/**
 * @SaCheckRole 注解详细说明
 * 
 * @Retention(RetentionPolicy.RUNTIME)
 * @Target({ElementType.METHOD, ElementType.TYPE})
 * public @interface SaCheckRole {
 * 
 *     // 多账号体系下所属的账号体系标识，非多账号体系无需关注此值
 *     String type() default "";
 * 
 *     // 需要校验的角色标识 [数组]
 *     String[] value() default {};
 * 
 *     // 验证模式：AND | OR，默认AND
 *     SaMode mode() default SaMode.AND;
 * }
 * 
 * 参数说明：
 * - type: 账号体系标识，用于多账号体系场景
 *   - 默认为空字符串，表示使用默认账号体系
 *   - 可设置为 "user"、"admin"、"system" 等自定义标识
 *   - 不同type对应不同的登录会话和角色权限体系
 * 
 * - value: 角色标识数组
 *   - 支持单个角色：@SaCheckRole("admin")
 *   - 支持多个角色：@SaCheckRole({"admin", "manager"})
 * 
 * - mode: 验证模式
 *   - SaMode.AND: 必须拥有所有指定角色（默认）
 *   - SaMode.OR: 拥有任意一个指定角色即可
 * 
 * 使用场景：
 * 1. 单一角色检查：@SaCheckRole("admin")
 * 2. 多角色AND检查：@SaCheckRole(value = {"admin", "manager"}, mode = SaMode.AND)
 * 3. 多角色OR检查：@SaCheckRole(value = {"admin", "editor"}, mode = SaMode.OR)
 * 4. 多账号体系：@SaCheckRole(value = "admin", type = "user")
 */
@RestController
public class MultiAccountController {
    
    // 用户体系的管理员角色检查
    @SaCheckRole(value = "admin", type = "user")
    @GetMapping("/user-admin/dashboard")
    public Result getUserAdminDashboard() {
        return Result.success("用户管理员面板");
    }
    
    // 系统体系的管理员角色检查
    @SaCheckRole(value = "admin", type = "system")
    @GetMapping("/system-admin/dashboard")
    public Result getSystemAdminDashboard() {
        return Result.success("系统管理员面板");
    }
    
    // 默认体系的角色检查（type为空）
    @SaCheckRole("admin")
    @GetMapping("/default-admin/dashboard")
    public Result getDefaultAdminDashboard() {
        return Result.success("默认管理员面板");
    }
    
    // 需要特定权限
    @SaCheckPermission("user:delete")
    @DeleteMapping("/user/{id}")
    public Result deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return Result.success();
    }
    
    // 组合权限检查
    @SaCheckPermission(value = {"user:view", "user:edit"}, mode = SaMode.OR)
    @GetMapping("/user/{id}")
    public Result getUser(@PathVariable Long id) {
        return Result.success(userService.getUser(id));
    }
}
```

### 5. HMAC API认证

#### 5.1 配置HMAC客户端

```yaml
lambda:
  security:
    hmac:
      enabled: true
      clients:
        - appid: "client1"
          secret: "your_secret_key_1"
        - appid: "client2"
          secret: "your_secret_key_2"
```

#### 5.2 Java服务端实现

```java
@RestController
public class HmacController {
    
    /**
     * HMAC认证保护的API接口
     * 框架会自动处理HMAC签名验证
     */
    @PostMapping("/api/protected-resource")
    public Result getProtectedResource(@RequestBody Map<String, Object> requestData) {
        // 认证成功后可以获取当前用户信息
        LoginUser currentUser = StpUtil.getLoginUser();
        
        return Result.success(Map.of(
            "message", "访问成功",
            "user", currentUser.getUsername(),
            "data", requestData
        ));
    }
    
    /**
     * HMAC客户端代理用户登录
     * 支持HMAC客户端以其他用户身份执行操作
     */
    @PostMapping("/api/user-proxy")
    public Result userProxyOperation(
            @RequestParam("hmac-run-user") String targetUserId,
            @RequestParam(value = "hmac-run-type", defaultValue = "default") String loginType,
            @RequestBody Map<String, Object> requestData) {
        
        // 框架会自动处理用户切换逻辑
        // 当前登录用户已经是目标用户
        LoginUser currentUser = StpUtil.getLoginUser();
        
        return Result.success(Map.of(
            "message", "代理操作成功",
            "proxyUser", currentUser.getUsername(),
            "loginType", loginType,
            "data", requestData
        ));
    }
}
```

#### 5.3 自定义HMAC客户端服务

```java
/**
 * 基于数据库的HMAC客户端服务
 */
@Service
public class DatabaseHmacClientService implements HmacClientService {
    
    @Autowired
    private HmacClientMapper clientMapper;
    
    @Autowired
    private UserDetailService userDetailService;
    
    @Override
    public LoginUser loadClientByAppid(String appid) {
        // 从数据库加载客户端信息
        HmacClientEntity clientEntity = clientMapper.findByAppid(appid);
        if (clientEntity == null) {
            return null;
        }
        
        // 转换为HmacClient对象
        HmacClient client = new HmacClient(
            "hmac_" + clientEntity.getAppid(),
            clientEntity.getSecret()
        );
        client.setHosts(clientEntity.getHosts());
        return client;
    }
    
    @Override
    public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        // 委托给用户详情服务进行用户认证
        return userDetailService.loginByUsername(username, loginType);
    }
}
```

#### 5.4 Java客户端HMAC签名生成

```java
/**
 * Java客户端HMAC签名工具
 */
public class HmacClientUtils {
    
    /**
     * 生成HMAC认证请求
     * @param appid 应用ID
     * @param secret 应用密钥
     * @param requestPath 请求路径
     * @param queryParams 查询参数
     * @param requestBody 请求体
     * @return Authorization头值
     */
    public static String generateAuthorization(String appid, String secret, 
                                              String requestPath, 
                                              Map<String, String[]> queryParams,
                                              String requestBody) {
        try {
            long timestamp = System.currentTimeMillis();
            
            // 构造基础签名字符串
            String baseString = HmacGenerator.baseString(appid, timestamp, queryParams, requestBody);
            
            // 生成Authorization头
            return HmacGenerator.authorization(appid, secret, timestamp, baseString);
        } catch (Exception e) {
            throw new RuntimeException("生成HMAC签名失败", e);
        }
    }
    
    /**
     * 发送HMAC认证请求
     * @param appid 应用ID
     * @param secret 应用密钥
     * @param url 请求URL
     * @param requestBody 请求体
     * @param runUserId 代理用户ID（可选）
     * @param runType 登录类型（可选）
     * @return 响应结果
     */
    public static String sendHmacRequest(String appid, String secret, String url, 
                                       String requestBody, String runUserId, String runType) {
        try {
            URL requestUrl = new URL(url);
            String requestPath = requestUrl.getPath();
            
            // 构造查询参数
            Map<String, String[]> queryParams = new HashMap<>();
            if (runUserId != null) {
                queryParams.put("hmac-run-user", new String[]{runUserId});
            }
            if (runType != null) {
                queryParams.put("hmac-run-type", new String[]{runType});
            }
            
            // 解析URL中的查询参数
            String query = requestUrl.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=");
                    if (keyValue.length == 2) {
                        queryParams.put(keyValue[0], new String[]{keyValue[1]});
                    }
                }
            }
            
            // 生成Authorization头
            String authorization = generateAuthorization(appid, secret, requestPath, queryParams, requestBody);
            
            // 构造完整的请求URL
            StringBuilder fullUrl = new StringBuilder(url);
            if (runUserId != null || runType != null) {
                fullUrl.append(url.contains("?") ? "&" : "?");
                if (runUserId != null) {
                    fullUrl.append("hmac-run-user=").append(runUserId);
                }
                if (runType != null) {
                    if (runUserId != null) fullUrl.append("&");
                    fullUrl.append("hmac-run-type=").append(runType);
                }
            }
            
            // 发送HTTP请求
            HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl.toString()).openConnection();
            connection.setRequestMethod(requestBody != null ? "POST" : "GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authorization);
            
            if (requestBody != null) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            
            // 读取响应
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("发送HMAC请求失败", e);
        }
    }
}
```

#### 5.5 Java客户端使用示例

```java
@Component
public class HmacClientExample {
    
    private static final String APPID = "client1";
    private static final String SECRET = "your_secret_key_1";
    private static final String BASE_URL = "http://localhost:8080";
    
    /**
     * 基本HMAC认证请求
     */
    public void basicHmacRequest() {
        String url = BASE_URL + "/api/protected-resource";
        String requestBody = "{\"data\": \"some data\"}";
        
        String response = HmacClientUtils.sendHmacRequest(
            APPID, SECRET, url, requestBody, null, null
        );
        
        System.out.println("响应结果: " + response);
    }
    
    /**
     * HMAC客户端代理用户请求
     */
    public void proxyUserRequest() {
        String url = BASE_URL + "/api/user-proxy";
        String requestBody = "{\"operation\": \"update_profile\"}";
        String targetUserId = "user123";
        String loginType = "admin";
        
        String response = HmacClientUtils.sendHmacRequest(
            APPID, SECRET, url, requestBody, targetUserId, loginType
        );
        
        System.out.println("代理请求响应: " + response);
    }
    
    /**
     * GET请求示例
     */
    public void getRequest() {
        String url = BASE_URL + "/api/protected-resource?param1=value1&param2=value2";
        
        String response = HmacClientUtils.sendHmacRequest(
            APPID, SECRET, url, null, null, null
        );
        
        System.out.println("GET请求响应: " + response);
    }
}
```

### 6. 短信验证码登录

#### 配置短信登录

```yaml
lambda:
  security:
    sms:
      enabled: true
      loginPath: "/sms-login"        # 短信登录处理URL
      verifyPath: "/sms-code"        # 验证码获取URL
      code: "code"                   # 验证码参数名
      mobile: "mobile"               # 手机号参数名
      validMinutes: 3                # 验证码有效期（分钟）
      resendSeconds: 60              # 重发间隔（秒）
      enableVerify: false            # 是否启用图形验证码
      mock: false                    # 测试模式开关
```

#### 前端JavaScript示例

```javascript
// 短信验证码登录类
class SmsLogin {
    
    /**
     * 获取短信验证码
     * @param {string} mobile - 手机号
     * @param {string} verifyCode - 图形验证码（启用图形验证时必需）
     * @param {string} verifyToken - 图形验证码令牌（启用图形验证时必需）
     * @param {string} loginType - 登录类型（可选，默认为""）
     * @returns {Promise} 发送结果
     */
    async getSmsCode(mobile, verifyCode = null, verifyToken = null, loginType = '') {
        // 构建请求参数，使用配置中定义的参数名
        const requestBody = { 
            mobile: mobile,  // 对应配置: sms.mobile = "mobile"
            loginType: loginType  // 登录类型参数
        };
        
        // 如果启用了图形验证码（根据后端代码smsLogin.isEnableVerify()）
        if (verifyCode && verifyToken) {
            requestBody.__TOKEN = verifyToken;  // 图形验证码令牌
            requestBody.verifyCode = verifyCode;  // 图形验证码
        }
        
        try {
            // 使用配置中定义的验证码获取路径
            const response = await fetch('/sms-code', {  // 对应配置: sms.verifyPath = "/sms-code"
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(requestBody)
            });
            
            const result = await response.json();
            
            if (response.ok) {
                // 成功响应 - SmsVerifyCodeResponse格式
                console.log('短信验证码发送成功');
                // 显示重发倒计时
                this.startResendCountdown(result.resendSeconds || 60);
                return {
                    success: true,
                    data: result  // { id, resendSeconds, validMinutes, message? }
                };
            } else {
                // 错误响应 - ErrorModel格式
                console.error('短信验证码发送失败:', result.message);
                return {
                    success: false,
                    error: result.error,
                    message: result.message,
                    status: result.status
                };
            }
        } catch (error) {
            console.error('网络请求失败:', error);
            return {
                success: false,
                message: '网络请求失败: ' + error.message
            };
        }
    }
    
    /**
     * 短信验证码登录
     * @param {string} mobile - 手机号
     * @param {string} code - 短信验证码
     * @param {string} loginType - 登录类型（可选）
     * @param {string} loginDevice - 登录设备（可选，默认"default"）
     * @returns {Promise} 登录结果
     */
    async smsLogin(mobile, code, loginType = '', loginDevice = 'default') {
        try {
            // 使用配置中定义的登录路径和参数名
            const response = await fetch('/sms-login', {  // 对应配置: sms.loginPath = "/sms-login"
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    mobile: mobile,  // 对应配置: sms.mobile = "mobile"
                    code: code,      // 对应配置: sms.code = "code"
                    loginType: loginType,    // 登录类型参数
                    loginDevice: loginDevice // 登录设备参数
                })
            });
            
            if (response.ok) {
                // 登录成功 - 返回SaTokenInfo对象
                const tokenInfo = await response.json();
                console.log('短信登录成功');
                
                // 保存token信息到localStorage
                if (tokenInfo.tokenValue) {
                    localStorage.setItem('satoken', tokenInfo.tokenValue);
                    localStorage.setItem('tokenInfo', JSON.stringify(tokenInfo));
                }
                
                return {
                    success: true,
                    data: tokenInfo  // SaTokenInfo: { tokenName, tokenValue, isLogin, loginId, loginType, tokenTimeout, sessionTimeout, tokenSessionTimeout, tokenActiveTimeout, loginDevice }
                };
            } else {
                // 登录失败 - ErrorModel格式
                const errorResult = await response.json();
                console.error('短信登录失败:', errorResult.message);
                return {
                    success: false,
                    error: errorResult.error,
                    message: errorResult.message,
                    status: errorResult.status
                };
            }
        } catch (error) {
            console.error('网络请求失败:', error);
            return {
                success: false,
                message: '网络请求失败: ' + error.message
            };
        }
    }
    
    /**
     * 重发倒计时
     * @param {number} seconds - 倒计时秒数
     */
    startResendCountdown(seconds) {
        const btn = document.getElementById('getSmsCodeBtn');
        if (!btn) return;
        
        btn.disabled = true;
        let countdown = seconds;
        
        const timer = setInterval(() => {
            btn.textContent = `重新发送(${countdown}s)`;
            countdown--;
            
            if (countdown < 0) {
                clearInterval(timer);
                btn.disabled = false;
                btn.textContent = '获取验证码';
            }
        }, 1000);
    }
    
    /**
     * 完整的短信登录流程示例
     * @param {string} mobile - 手机号
     * @param {string} verifyCode - 图形验证码（可选）
     * @param {string} verifyToken - 图形验证码令牌（可选）
     * @param {string} loginType - 登录类型（可选）
     */
    async handleSmsLogin(mobile, verifyCode = null, verifyToken = null, loginType = '') {
        try {
            // 1. 手机号格式验证
            if (!/^1[3-9]\d{9}$/.test(mobile)) {
                throw new Error('请输入正确的手机号格式');
            }
            
            // 2. 获取短信验证码
            const smsResult = await this.getSmsCode(mobile, verifyCode, verifyToken, loginType);
            if (!smsResult.success) {
                throw new Error(smsResult.message);
            }
            
            console.log('短信验证码已发送，请查收');
            
        } catch (error) {
            console.error('获取验证码失败:', error.message);
            alert('获取验证码失败: ' + error.message);
        }
    }
    
    /**
     * 执行登录
     * @param {string} mobile - 手机号
     * @param {string} code - 验证码
     * @param {string} loginType - 登录类型（可选）
     * @param {string} loginDevice - 登录设备（可选）
     */
    async executeLogin(mobile, code, loginType = '', loginDevice = 'default') {
        try {
            // 验证码格式检查
            if (!/^\d{4,6}$/.test(code)) {
                throw new Error('验证码格式不正确');
            }
            
            const loginResult = await this.smsLogin(mobile, code, loginType, loginDevice);
            if (loginResult.success) {
                alert('登录成功!');
                // 跳转到主页或其他页面
                window.location.href = '/dashboard';
            } else {
                throw new Error(loginResult.message);
            }
            
        } catch (error) {
            console.error('登录失败:', error.message);
            alert('登录失败: ' + error.message);
        }
    }
}

/**
 * 表单登录类
 */
class FormLogin {
    /**
     * 表单登录
     * @param {string} username - 用户名
     * @param {string} password - 密码
     * @param {string} loginType - 登录类型（可选）
     * @param {string} loginDevice - 登录设备（可选，默认"default"）
     * @returns {Promise} 登录结果
     */
    async login(username, password, loginType = '', loginDevice = 'default') {
        try {
            const response = await fetch('/form-login', {  // 对应配置: form.loginPath
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    username: username,  // 对应配置: form.usernameParameter
                    password: password,  // 对应配置: form.passwordParameter
                    loginType: loginType,
                    loginDevice: loginDevice
                })
            });
            
            if (response.ok) {
                // 登录成功 - 返回SaTokenInfo对象
                const tokenInfo = await response.json();
                console.log('表单登录成功');
                
                // 保存token信息到localStorage
                if (tokenInfo.tokenValue) {
                    localStorage.setItem('satoken', tokenInfo.tokenValue);
                    localStorage.setItem('tokenInfo', JSON.stringify(tokenInfo));
                }
                
                return {
                    success: true,
                    data: tokenInfo
                };
            } else {
                // 登录失败 - ErrorModel格式
                const errorResult = await response.json();
                console.error('表单登录失败:', errorResult.message);
                return {
                    success: false,
                    error: errorResult.error,
                    message: errorResult.message,
                    status: errorResult.status
                };
            }
        } catch (error) {
            console.error('网络请求失败:', error);
            return {
                success: false,
                message: '网络请求失败: ' + error.message
            };
        }
    }
}

/**
 * HMAC认证类
 * 基于lambda-cloud框架的HMAC认证实现
 * 严格按照HmacGenerator和HmacUtils的Java实现
 */
class HmacAuth {
    constructor(appid, secret, runUserId = null, runType = null) {
        this.appid = appid;
        this.secret = secret;
        this.runUserId = runUserId;
        this.runType = runType;
    }
    
    /**
     * 构建基础签名字符串
     * 严格按照HmacGenerator.baseString的Java实现
     */
    buildBaseString(queryParams, requestBody) {
        const parts = [];
        
        // 1. 处理查询参数（模拟Java的Map<String, String[]>）
        if (queryParams && queryParams.size > 0) {
            const sortedParams = Array.from(queryParams.entries())
                .sort(([a], [b]) => a.localeCompare(b));
            
            for (const [key, values] of sortedParams) {
                // 模拟Java数组格式：[value1, value2]
                const arrayStr = Array.isArray(values) 
                    ? `[${values.join(', ')}]`
                    : `[${values}]`;
                parts.push(`${key}=${arrayStr}`);
            }
        }
        
        // 2. 添加appid
        parts.push(`appid=${this.appid}`);
        
        // 3. 添加时间戳
        const timestamp = Date.now();
        parts.push(`timestamp=${timestamp}`);
        
        // 4. 处理请求体（清理空白字符和反斜杠，与Java的replaceAll("\\s+|\\\\+", "")一致）
        if (requestBody) {
            const cleanedBody = requestBody.replace(/\s+|\\+/g, '');
            if (cleanedBody) {
                parts.push(cleanedBody);
            }
        }
        
        return {
            baseString: parts.join(''),
            timestamp: timestamp
        };
    }
    
    /**
     * 生成HMAC-SHA1签名
     * 使用UTF-8编码，与Java的实现保持一致
     */
    async generateHmacSha1Signature(baseString) {
        const encoder = new TextEncoder();
        const keyData = encoder.encode(this.secret);
        const messageData = encoder.encode(baseString);
        
        const cryptoKey = await crypto.subtle.importKey(
            'raw',
            keyData,
            { name: 'HMAC', hash: 'SHA-1' },
            false,
            ['sign']
        );
        
        const signature = await crypto.subtle.sign('HMAC', cryptoKey, messageData);
        
        // 转换为Base64，与Java的Base64.encodeBase64String一致
        return btoa(String.fromCharCode(...new Uint8Array(signature)));
    }
    
    /**
     * 执行HMAC认证
     * 支持GET和POST请求，支持代理用户参数
     */
    async authenticate(url, requestData = null) {
        try {
            const urlObj = new URL(url);
            const queryParams = new Map();
            
            // 解析现有查询参数
            for (const [key, value] of urlObj.searchParams) {
                queryParams.set(key, value);
            }
            
            // 添加代理用户参数
            if (this.runUserId) {
                queryParams.set('hmac-run-user', this.runUserId);
            }
            if (this.runType) {
                queryParams.set('hmac-run-type', this.runType);
            }
            
            // 构建请求体
            const requestBody = requestData ? JSON.stringify(requestData) : null;
            const { baseString, timestamp } = this.buildBaseString(queryParams, requestBody);
            
            // 生成签名
            const signature = await this.generateHmacSha1Signature(baseString);
            
            // 构建Authorization头（格式：HmacSHA appid:timestamp:signature）
            const authorization = `HmacSHA ${this.appid}:${timestamp}:${signature}`;
            
            // 构建完整URL（添加查询参数）
            const fullPath = new URLSearchParams();
            for (const [key, value] of queryParams) {
                fullPath.append(key, value);
            }
            const fullUrl = `${urlObj.origin}${urlObj.pathname}?${fullPath.toString()}`;
            
            // 发送请求
            const response = await fetch(fullUrl, {
                method: requestData ? 'POST' : 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': authorization
                },
                body: requestBody
            });
            
            const result = await response.json();
            
            if (response.ok && result.code === 200) {
                // 认证成功，保存token信息
                if (result.data && result.data.tokenValue) {
                    localStorage.setItem('satoken', result.data.tokenValue);
                    localStorage.setItem('tokenInfo', JSON.stringify(result.data));
                }
                return { success: true, data: result.data };
            } else {
                return { success: false, message: result.message || '认证失败' };
            }
        } catch (error) {
            console.error('HMAC认证错误:', error);
            return { success: false, message: error.message };
        }
    }
    
    /**
     * 发送GET请求
     * 简化的GET请求方法
     */
    async get(url) {
        return await this.authenticate(url, null);
    }
    
    /**
     * 发送POST请求
     * 简化的POST请求方法
     */
    async post(url, data) {
        return await this.authenticate(url, data);
    }
}

/**
 * HMAC客户端工具类
 * 提供静态方法进行HMAC认证请求
 */
class HmacClient {
    /**
     * 创建HMAC认证实例
     */
    static create(appid, secret, runUserId = null, runType = null) {
        return new HmacAuth(appid, secret, runUserId, runType);
    }
    
    /**
     * 发送基本HMAC请求
     */
    static async request(appid, secret, url, data = null) {
        const auth = new HmacAuth(appid, secret);
        return await auth.authenticate(url, data);
    }
    
    /**
     * 发送代理用户HMAC请求
     */
    static async proxyRequest(appid, secret, url, data, runUserId, runType = 'default') {
        const auth = new HmacAuth(appid, secret, runUserId, runType);
        return await auth.authenticate(url, data);
    }
}

// 使用示例
const smsLogin = new SmsLogin();
const formLogin = new FormLogin();
const hmacAuth = new HmacAuth();

// 短信登录示例
// 绑定获取验证码按钮事件
document.getElementById('getSmsCodeBtn')?.addEventListener('click', async () => {
    const mobile = document.getElementById('mobile')?.value;
    const verifyCode = document.getElementById('verifyCode')?.value; // 图形验证码
    const verifyToken = document.getElementById('verifyToken')?.value; // 图形验证码令牌
    const loginType = document.getElementById('loginType')?.value || ''; // 登录类型
    
    if (!mobile) {
        alert('请输入手机号');
        return;
    }
    
    await smsLogin.handleSmsLogin(mobile, verifyCode, verifyToken, loginType);
});

// 绑定短信登录按钮事件
document.getElementById('smsLoginBtn')?.addEventListener('click', async () => {
    const mobile = document.getElementById('mobile')?.value;
    const code = document.getElementById('smsCode')?.value;
    const loginType = document.getElementById('loginType')?.value || '';
    const loginDevice = document.getElementById('loginDevice')?.value || 'default';
    
    if (!mobile || !code) {
        alert('请输入手机号和验证码');
        return;
    }
    
    await smsLogin.executeLogin(mobile, code, loginType, loginDevice);
});

// 表单登录示例
document.getElementById('formLoginBtn')?.addEventListener('click', async () => {
    const username = document.getElementById('username')?.value;
    const password = document.getElementById('password')?.value;
    const loginType = document.getElementById('loginType')?.value || '';
    const loginDevice = document.getElementById('loginDevice')?.value || 'default';
    
    if (!username || !password) {
        alert('请输入用户名和密码');
        return;
    }
    
    const result = await formLogin.login(username, password, loginType, loginDevice);
    if (result.success) {
        alert('登录成功!');
        window.location.href = '/dashboard';
    } else {
        alert('登录失败: ' + result.message);
    }
});

// ========== HMAC认证使用示例 ==========

// 1. 基本HMAC认证请求示例
const basicHmacExample = async () => {
    const hmacAuth = new HmacAuth('test-app', 'test-secret');
    
    try {
        const result = await hmacAuth.post('http://localhost:8080/api/protected-resource', {
            operation: 'query',
            data: 'some important data',
            timestamp: Date.now()
        });
        
        if (result.success) {
            console.log('基本请求成功:', result.data);
        } else {
            console.error('基本请求失败:', result.message);
        }
    } catch (error) {
        console.error('请求异常:', error);
    }
};

// 2. HMAC客户端代理用户请求示例
const proxyHmacExample = async () => {
    const proxyAuth = new HmacAuth('test-app', 'test-secret', 'user123', 'admin');
    
    try {
        const result = await proxyAuth.post('http://localhost:8080/api/user-proxy', {
            operation: 'update_profile',
            data: { 
                name: '张三', 
                email: 'zhangsan@example.com' 
            }
        });
        
        console.log('代理请求结果:', result);
    } catch (error) {
        console.error('代理请求异常:', error);
    }
};

// 3. GET请求示例
const getHmacExample = async () => {
    const hmacAuth = new HmacAuth('test-app', 'test-secret');
    
    try {
        const result = await hmacAuth.get('http://localhost:8080/api/info?param1=value1&param2=value2');
        console.log('GET请求结果:', result);
    } catch (error) {
        console.error('GET请求异常:', error);
    }
};

// 4. 使用静态方法的示例
const staticHmacExample = async () => {
    // 基本请求
    const basicResult = await HmacClient.request(
        'test-app', 
        'test-secret', 
        'http://localhost:8080/api/protected-resource',
        { operation: 'query', data: 'test data' }
    );
    console.log('静态方法基本请求:', basicResult);
    
    // 代理请求
    const proxyResult = await HmacClient.proxyRequest(
        'test-app', 
        'test-secret',
        'http://localhost:8080/api/user-proxy',
        { operation: 'update', data: { name: '李四' } },
        'user456',
        'admin'
    );
    console.log('静态方法代理请求:', proxyResult);
};

// 5. 页面加载时执行示例
document.addEventListener('DOMContentLoaded', () => {
    console.log('=== HMAC客户端示例 ===');
    
    // 可以根据需要调用不同的示例
    // basicHmacExample();
    // proxyHmacExample();
    // getHmacExample();
    // staticHmacExample();
});

// 6. 按钮事件绑定示例
if (document.getElementById('hmac-basic-btn')) {
    document.getElementById('hmac-basic-btn').addEventListener('click', basicHmacExample);
}

if (document.getElementById('hmac-proxy-btn')) {
    document.getElementById('hmac-proxy-btn').addEventListener('click', proxyHmacExample);
}

if (document.getElementById('hmac-get-btn')) {
    document.getElementById('hmac-get-btn').addEventListener('click', getHmacExample);
}

if (document.getElementById('hmac-static-btn')) {
    document.getElementById('hmac-static-btn').addEventListener('click', staticHmacExample);
}
```

#### 验证码相关JavaScript示例

```javascript
/**
 * 图形验证码类
 */
class CaptchaCode {
    /**
     * 获取图形验证码
     * @returns {Promise} 验证码结果
     */
    async getCaptcha() {
        try {
            const response = await fetch('/captcha-code', {  // 对应配置: captcha.verifyPath
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                }
            });
            
            if (response.ok) {
                const result = await response.json();
                // CaptchaVerifyCodeResponse格式
                return {
                    success: true,
                    data: result  // { __TOKEN, image, validMinutes }
                };
            } else {
                const errorResult = await response.json();
                return {
                    success: false,
                    error: errorResult.error,
                    message: errorResult.message,
                    status: errorResult.status
                };
            }
        } catch (error) {
            console.error('获取图形验证码失败:', error);
            return {
                success: false,
                message: '获取图形验证码失败: ' + error.message
            };
        }
    }
    
    /**
     * 刷新图形验证码
     */
    async refreshCaptcha() {
        const result = await this.getCaptcha();
        if (result.success) {
            const captchaImg = document.getElementById('captchaImg');
            const tokenInput = document.getElementById('verifyToken');
            
            if (captchaImg && result.data.image) {
                captchaImg.src = 'data:image/png;base64,' + result.data.image;
            }
            
            if (tokenInput && result.data.__TOKEN) {
                tokenInput.value = result.data.__TOKEN;
            }
        }
        return result;
    }
}

// 验证码使用示例
const captchaCode = new CaptchaCode();

// 页面加载时获取验证码
document.addEventListener('DOMContentLoaded', async () => {
    await captchaCode.refreshCaptcha();
});

// 点击图片刷新验证码
document.getElementById('captchaImg')?.addEventListener('click', async () => {
    await captchaCode.refreshCaptcha();
});

// 刷新按钮事件
document.getElementById('refreshCaptchaBtn')?.addEventListener('click', async () => {
    await captchaCode.refreshCaptcha();
});
```

#### 后端接口响应格式

```json
 // 获取短信验证码成功响应 - SmsVerifyCodeResponse格式
 {
   "id": "sms_message_id_from_provider",
   "resendSeconds": 60,
   "validMinutes": 5,
   "message": "验证码内容（仅开发模式显示）"
 }
 
 // 获取图形验证码成功响应 - CaptchaVerifyCodeResponse格式
 {
   "__TOKEN": "captcha_token_uuid",
   "image": "base64_encoded_image_data",
   "validMinutes": 3
 }
 
 // 登录成功响应 - SaTokenInfo格式（短信登录、表单登录通用）
 {
   "tokenName": "Authorization",
   "tokenValue": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
   "isLogin": true,
   "loginId": "user123",
   "loginType": "",
   "tokenTimeout": 2592000,
   "sessionTimeout": -1,
   "tokenSessionTimeout": -1,
   "tokenActiveTimeout": -1,
   "loginDevice": "default"
 }
 
 // 错误响应 - ErrorModel格式（统一错误格式）
 {
   "status": 417,
   "error": "Expectation Failed",
   "message": "具体错误信息（如：验证码错误或已过期）",
   "path": "/sms-login",
   "timestamp": 1234567890123
 }
 
 // HMAC认证说明
 // HMAC认证成功后，后续请求会自动在请求头中添加Sa-Token
 // Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
 ```

### 6. HMAC认证

#### 6.1 JavaScript端使用示例

```javascript
/**
 * HMAC认证类
 * 基于lambda-cloud框架的HMAC认证实现
 * 严格按照HmacGenerator和HmacUtils的Java实现
 */
class HmacAuth {
    constructor(appid, secret, runUserId = null, runType = null) {
        this.appid = appid;
        this.secret = secret;
        this.runUserId = runUserId;
        this.runType = runType;
    }
    
    /**
     * 构建基础签名字符串
     * 严格按照HmacGenerator.baseString的Java实现
     */
    buildBaseString(queryParams, requestBody) {
        const parts = [];
        
        // 1. 处理查询参数（模拟Java的Map<String, String[]>）
        if (queryParams && queryParams.size > 0) {
            const sortedParams = Array.from(queryParams.entries())
                .sort(([a], [b]) => a.localeCompare(b));
            
            for (const [key, values] of sortedParams) {
                // 模拟Java数组格式：[value1, value2]
                const arrayStr = Array.isArray(values) 
                    ? `[${values.join(', ')}]`
                    : `[${values}]`;
                parts.push(`${key}=${arrayStr}`);
            }
        }
        
        // 2. 添加appid
        parts.push(`appid=${this.appid}`);
        
        // 3. 添加时间戳
        const timestamp = Date.now();
        parts.push(`timestamp=${timestamp}`);
        
        // 4. 处理请求体（清理空白字符和反斜杠，与Java的replaceAll("\\s+|\\\\+", "")一致）
        if (requestBody) {
            const cleanedBody = requestBody.replace(/\s+|\\+/g, '');
            if (cleanedBody) {
                parts.push(cleanedBody);
            }
        }
        
        return {
            baseString: parts.join(''),
            timestamp: timestamp
        };
    }
    
    /**
     * 生成HMAC-SHA1签名
     * 使用UTF-8编码，与Java的实现保持一致
     */
    async generateHmacSha1Signature(baseString) {
        const encoder = new TextEncoder();
        const keyData = encoder.encode(this.secret);
        const messageData = encoder.encode(baseString);
        
        const cryptoKey = await crypto.subtle.importKey(
            'raw',
            keyData,
            { name: 'HMAC', hash: 'SHA-1' },
            false,
            ['sign']
        );
        
        const signature = await crypto.subtle.sign('HMAC', cryptoKey, messageData);
        
        // 转换为Base64，与Java的Base64.encodeBase64String一致
        return btoa(String.fromCharCode(...new Uint8Array(signature)));
    }
    
    /**
     * 执行HMAC认证
     * 支持GET和POST请求，支持代理用户参数
     */
    async authenticate(url, requestData = null) {
        try {
            const urlObj = new URL(url);
            const queryParams = new Map();
            
            // 解析现有查询参数
            for (const [key, value] of urlObj.searchParams) {
                queryParams.set(key, value);
            }
            
            // 添加代理用户参数
            if (this.runUserId) {
                queryParams.set('hmac-run-user', this.runUserId);
            }
            if (this.runType) {
                queryParams.set('hmac-run-type', this.runType);
            }
            
            // 构建请求体
            const requestBody = requestData ? JSON.stringify(requestData) : null;
            const { baseString, timestamp } = this.buildBaseString(queryParams, requestBody);
            
            // 生成签名
            const signature = await this.generateHmacSha1Signature(baseString);
            
            // 构建Authorization头（格式：HmacSHA appid:timestamp:signature）
            const authorization = `HmacSHA ${this.appid}:${timestamp}:${signature}`;
            
            // 构建完整URL（添加查询参数）
            const fullPath = new URLSearchParams();
            for (const [key, value] of queryParams) {
                fullPath.append(key, value);
            }
            const fullUrl = `${urlObj.origin}${urlObj.pathname}?${fullPath.toString()}`;
            
            // 发送请求
            const response = await fetch(fullUrl, {
                method: requestData ? 'POST' : 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    'Authorization': authorization
                },
                body: requestBody
            });
            
            const result = await response.json();
            
            if (response.ok && result.code === 200) {
                // 认证成功，保存token信息
                if (result.data && result.data.tokenValue) {
                    localStorage.setItem('satoken', result.data.tokenValue);
                    localStorage.setItem('tokenInfo', JSON.stringify(result.data));
                }
                return { success: true, data: result.data };
            } else {
                return { success: false, message: result.message || '认证失败' };
            }
        } catch (error) {
            console.error('HMAC认证错误:', error);
            return { success: false, message: error.message };
        }
    }
    
    /**
     * 发送GET请求
     * 简化的GET请求方法
     */
    async get(url) {
        return await this.authenticate(url, null);
    }
    
    /**
     * 发送POST请求
     * 简化的POST请求方法
     */
    async post(url, data) {
        return await this.authenticate(url, data);
    }
}

/**
 * HMAC客户端工具类
 * 提供静态方法进行HMAC认证请求
 */
class HmacClient {
    /**
     * 创建HMAC认证实例
     */
    static create(appid, secret, runUserId = null, runType = null) {
        return new HmacAuth(appid, secret, runUserId, runType);
    }
    
    /**
     * 发送基本HMAC请求
     */
    static async request(appid, secret, url, data = null) {
        const auth = new HmacAuth(appid, secret);
        return await auth.authenticate(url, data);
    }
    
    /**
     * 发送代理用户HMAC请求
     */
    static async proxyRequest(appid, secret, url, data, runUserId, runType = 'default') {
        const auth = new HmacAuth(appid, secret, runUserId, runType);
        return await auth.authenticate(url, data);
    }
}

// ========== 使用示例 ==========

// 1. 基本HMAC认证请求
const basicExample = async () => {
    const hmacAuth = new HmacAuth('test-app', 'test-secret');
    
    try {
        const result = await hmacAuth.post('http://localhost:8080/api/protected-resource', {
            operation: 'query',
            data: 'some important data',
            timestamp: Date.now()
        });
        
        if (result.success) {
            console.log('基本请求成功:', result.data);
        } else {
            console.error('基本请求失败:', result.message);
        }
    } catch (error) {
        console.error('请求异常:', error);
    }
};

// 2. HMAC客户端代理用户请求
const proxyExample = async () => {
    const proxyAuth = new HmacAuth('test-app', 'test-secret', 'user123', 'admin');
    
    try {
        const result = await proxyAuth.post('http://localhost:8080/api/user-proxy', {
            operation: 'update_profile',
            data: { 
                name: '张三', 
                email: 'zhangsan@example.com' 
            }
        });
        
        console.log('代理请求结果:', result);
    } catch (error) {
        console.error('代理请求异常:', error);
    }
};

// 3. GET请求示例
const getExample = async () => {
    const hmacAuth = new HmacAuth('test-app', 'test-secret');
    
    try {
        const result = await hmacAuth.get('http://localhost:8080/api/info?param1=value1&param2=value2');
        console.log('GET请求结果:', result);
    } catch (error) {
        console.error('GET请求异常:', error);
    }
};

// 4. 使用静态方法的示例
const staticExample = async () => {
    // 基本请求
    const basicResult = await HmacClient.request(
        'test-app', 
        'test-secret', 
        'http://localhost:8080/api/protected-resource',
        { operation: 'query', data: 'test data' }
    );
    console.log('静态方法基本请求:', basicResult);
    
    // 代理请求
    const proxyResult = await HmacClient.proxyRequest(
        'test-app', 
        'test-secret',
        'http://localhost:8080/api/user-proxy',
        { operation: 'update', data: { name: '李四' } },
        'user456',
        'admin'
    );
    console.log('静态方法代理请求:', proxyResult);
};

// 5. 页面加载时执行示例
document.addEventListener('DOMContentLoaded', () => {
    console.log('=== HMAC客户端示例 ===');
    
    // 可以根据需要调用不同的示例
    // basicExample();
    // proxyExample();
    // getExample();
    // staticExample();
});

// 6. 按钮事件绑定示例
if (document.getElementById('hmac-basic-btn')) {
    document.getElementById('hmac-basic-btn').addEventListener('click', basicExample);
}

if (document.getElementById('hmac-proxy-btn')) {
    document.getElementById('hmac-proxy-btn').addEventListener('click', proxyExample);
}

if (document.getElementById('hmac-get-btn')) {
    document.getElementById('hmac-get-btn').addEventListener('click', getExample);
}
```

#### 6.2 Java端配置

```yaml
# application.yml
spring:
  security:
    hmac:
      enabled: true
      clients:
        - appid: "test-app"
          secret: "test-secret"
          hosts: "127.0.0.1,localhost"  # IP白名单，可选
        - appid: "mobile-app"
          secret: "mobile-secret"
          # 不设置hosts表示不限制IP
```

#### 6.3 Java端服务实现

```java
/**
 * HMAC认证保护的API控制器
 * 基于HmacAuthenticationProcessingFilter自动处理HMAC签名验证
 */
@RestController
@RequestMapping("/api")
public class HmacProtectedController {
    
    /**
     * HMAC认证保护的资源接口
     * 框架会自动处理HMAC签名验证，验证通过后可获取当前用户信息
     */
    @PostMapping("/protected-resource")
    public Result<Map<String, Object>> getProtectedResource(@RequestBody Map<String, Object> requestData) {
        // 认证成功后可以获取当前用户信息
        LoginUser currentUser = StpUtil.getLoginUser();
        
        return Result.success(Map.of(
            "message", "访问成功",
            "user", currentUser.getUsername(),
            "appid", currentUser instanceof HmacClient ? ((HmacClient) currentUser).getAppid() : "unknown",
            "data", requestData,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * HMAC客户端代理用户操作接口
     * 支持HMAC客户端通过hmac-run-user参数代理其他用户执行操作
     */
    @PostMapping("/user-proxy")
    public Result<Map<String, Object>> userProxyOperation(
            @RequestParam("hmac-run-user") String targetUserId,
            @RequestParam(value = "hmac-run-type", defaultValue = "default") String loginType,
            @RequestBody Map<String, Object> requestData) {
        
        // 框架会自动处理用户切换逻辑
        // 当前登录用户已经是目标用户
        LoginUser currentUser = StpUtil.getLoginUser();
        
        return Result.success(Map.of(
            "message", "代理操作成功",
            "proxyUser", currentUser.getUsername(),
            "loginType", loginType,
            "data", requestData,
            "timestamp", System.currentTimeMillis()
        ));
    }
    
    /**
     * GET请求示例
     * 演示HMAC认证对GET请求的支持
     */
    @GetMapping("/info")
    public Result<Map<String, Object>> getInfo(
            @RequestParam(value = "param1", required = false) String param1,
            @RequestParam(value = "param2", required = false) String param2) {
        
        LoginUser currentUser = StpUtil.getLoginUser();
        
        return Result.success(Map.of(
            "message", "GET请求成功",
            "user", currentUser.getUsername(),
            "params", Map.of("param1", param1, "param2", param2),
            "timestamp", System.currentTimeMillis()
        ));
    }
}
```

#### 6.4 自定义HMAC客户端服务

```java
/**
 * 基于数据库的HMAC客户端服务实现
 * 继承MemoryHmacClientService，支持从数据库动态加载客户端信息
 */
@Service
public class DatabaseHmacClientService implements HmacClientService {
    
    @Autowired
    private HmacClientMapper clientMapper;
    
    @Autowired
    private UserDetailService userDetailService;
    
    /**
     * 根据应用ID加载客户端信息
     * 实现HmacClientService接口，支持动态客户端管理
     */
    @Override
    public LoginUser loadClientByAppid(String appid) {
        // 从数据库加载客户端信息
        HmacClientEntity clientEntity = clientMapper.findByAppid(appid);
        if (clientEntity == null) {
            throw new UsernameNotFoundException("Client " + appid + " not found.");
        }
        
        // 转换为HmacClient对象
        HmacClient client = new HmacClient(
            "hmac_" + clientEntity.getAppid(),
            clientEntity.getSecret()
        );
        client.setAppid(clientEntity.getAppid());
        client.setHosts(clientEntity.getHosts());
        client.setEnabled(clientEntity.isEnabled());
        
        return client;
    }
    
    /**
     * 用户代理登录
     * 支持HMAC客户端代理其他用户进行登录
     */
    @Override
    public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        // 委托给用户详情服务进行用户认证
        return userDetailService.loginByUsername(username, loginType);
    }
}
```

#### 6.5 Java客户端HMAC工具类

```java
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.security.web.hmac.utils.HmacUtils;

/**
 * Java客户端HMAC签名工具类
 * 基于HmacGenerator和HmacUtils的实现，提供完整的HMAC客户端功能
 */
public class HmacClientUtils {
    
    /**
     * 生成HMAC Authorization头
     * 严格按照HmacGenerator.authorization的实现
     * 
     * @param appid 应用ID
     * @param secret 应用密钥
     * @param method HTTP方法（GET/POST等）
     * @param uri 请求URI（包含路径和查询参数）
     * @param queryParams 查询参数Map
     * @param requestBody 请求体
     * @return Authorization头值（格式：HMAC appid:timestamp:digest）
     */
    public static String generateAuthorization(String appid, String secret, 
                                              String method, String uri,
                                              Map<String, String[]> queryParams,
                                              String requestBody) {
        try {
            long timestamp = System.currentTimeMillis();
            
            // 使用HmacUtils.getHmacSaltValue生成盐值字符串
            // 格式：METHOD + URI + QUERY + BODY + TIMESTAMP
            String saltValue = HmacUtils.getHmacSaltValue(method, uri, queryParams, requestBody, appid, timestamp);
            
            // 使用HMAC-SHA256算法生成签名
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);
            byte[] digest = mac.doFinal(saltValue.getBytes(StandardCharsets.UTF_8));
            
            // Base64编码
            String signature = Base64.getEncoder().encodeToString(digest);
            
            // 构造Authorization头：HMAC appid:timestamp:digest
            return String.format("HMAC %s:%d:%s", appid, timestamp, signature);
        } catch (Exception e) {
            throw new RuntimeException("生成HMAC签名失败", e);
        }
    }
    
    /**
     * 发送HMAC认证请求
     * 支持GET和POST请求，支持代理用户参数
     * 
     * @param appid 应用ID
     * @param secret 应用密钥
     * @param url 请求URL
     * @param requestBody 请求体（POST请求时使用）
     * @param runUserId 代理用户ID（可选）
     * @param runType 登录类型（可选）
     * @return 响应结果
     */
    public static String sendHmacRequest(String appid, String secret, String url, 
                                       String requestBody, String runUserId, String runType) {
        try {
            URL requestUrl = new URL(url);
            String method = requestBody != null ? "POST" : "GET";
            
            // 构造查询参数Map
            Map<String, String[]> queryParams = new HashMap<>();
            
            // 解析URL中的查询参数
            String query = requestUrl.getQuery();
            if (query != null) {
                String[] pairs = query.split("&");
                for (String pair : pairs) {
                    String[] keyValue = pair.split("=", 2);
                    if (keyValue.length == 2) {
                        String key = URLDecoder.decode(keyValue[0], StandardCharsets.UTF_8);
                        String value = URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8);
                        queryParams.put(key, new String[]{value});
                    }
                }
            }
            
            // 添加代理用户参数
            if (runUserId != null) {
                queryParams.put("hmac-run-user", new String[]{runUserId});
            }
            if (runType != null) {
                queryParams.put("hmac-run-type", new String[]{runType});
            }
            
            // 构造完整URI（包含查询参数）
            StringBuilder uriBuilder = new StringBuilder(requestUrl.getPath());
            if (!queryParams.isEmpty()) {
                uriBuilder.append("?");
                List<String> paramList = new ArrayList<>();
                for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
                    for (String value : entry.getValue()) {
                        paramList.add(entry.getKey() + "=" + URLEncoder.encode(value, StandardCharsets.UTF_8));
                    }
                }
                uriBuilder.append(String.join("&", paramList));
            }
            String uri = uriBuilder.toString();
            
            // 生成Authorization头
            String authorization = generateAuthorization(appid, secret, method, uri, queryParams, requestBody);
            
            // 构造完整的请求URL
            StringBuilder fullUrl = new StringBuilder();
            fullUrl.append(requestUrl.getProtocol()).append("://")
                   .append(requestUrl.getHost());
            if (requestUrl.getPort() != -1) {
                fullUrl.append(":").append(requestUrl.getPort());
            }
            fullUrl.append(uri);  // 使用已构造的URI（包含所有查询参数）
            
            // 发送HTTP请求
            HttpURLConnection connection = (HttpURLConnection) new URL(fullUrl.toString()).openConnection();
            connection.setRequestMethod(requestBody != null ? "POST" : "GET");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setRequestProperty("Authorization", authorization);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            
            if (requestBody != null) {
                connection.setDoOutput(true);
                try (OutputStream os = connection.getOutputStream()) {
                    os.write(requestBody.getBytes(StandardCharsets.UTF_8));
                }
            }
            
            // 读取响应
            int responseCode = connection.getResponseCode();
            InputStream inputStream = responseCode >= 200 && responseCode < 300 
                ? connection.getInputStream() 
                : connection.getErrorStream();
                
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
                return reader.lines().collect(Collectors.joining("\n"));
            }
        } catch (Exception e) {
            throw new RuntimeException("发送HMAC请求失败", e);
        }
    }
    
    /**
     * 发送GET请求
     * 简化的GET请求方法
     */
    public static String sendGetRequest(String appid, String secret, String url) {
        return sendHmacRequest(appid, secret, url, null, null, null);
    }
    
    /**
     * 发送POST请求
     * 简化的POST请求方法
     */
    public static String sendPostRequest(String appid, String secret, String url, Object requestData) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            String requestBody = objectMapper.writeValueAsString(requestData);
            return sendHmacRequest(appid, secret, url, requestBody, null, null);
        } catch (Exception e) {
            throw new RuntimeException("序列化请求数据失败", e);
        }
    }
}
```

#### 6.6 Java客户端使用示例

```java
/**
 * HMAC客户端使用示例
 * 演示如何使用HmacClientUtils进行HMAC认证请求
 */
@Component
public class HmacClientExample {
    
    private static final String APPID = "test-app";
    private static final String SECRET = "test-secret";
    private static final String BASE_URL = "http://localhost:8080";
    
    /**
     * 基本HMAC认证请求示例
     */
    public void basicHmacRequest() {
        try {
            String url = BASE_URL + "/api/protected-resource";
            Map<String, Object> requestData = Map.of(
                "operation", "query",
                "data", "some important data",
                "timestamp", System.currentTimeMillis()
            );
            
            String response = HmacClientUtils.sendPostRequest(APPID, SECRET, url, requestData);
            System.out.println("基本请求响应: " + response);
        } catch (Exception e) {
            System.err.println("基本请求失败: " + e.getMessage());
        }
    }
    
    /**
     * HMAC客户端代理用户请求示例
     */
    public void proxyUserRequest() {
        try {
            String url = BASE_URL + "/api/user-proxy";
            Map<String, Object> requestData = Map.of(
                "operation", "update_profile",
                "data", Map.of("name", "张三", "email", "zhangsan@example.com")
            );
            String targetUserId = "user123";
            String loginType = "admin";
            
            String response = HmacClientUtils.sendHmacRequest(
                APPID, SECRET, url, 
                new ObjectMapper().writeValueAsString(requestData), 
                targetUserId, loginType
            );
            
            System.out.println("代理请求响应: " + response);
        } catch (Exception e) {
            System.err.println("代理请求失败: " + e.getMessage());
        }
    }
    
    /**
     * GET请求示例
     */
    public void getRequest() {
        try {
            String url = BASE_URL + "/api/info?param1=value1&param2=value2";
            
            String response = HmacClientUtils.sendGetRequest(APPID, SECRET, url);
            System.out.println("GET请求响应: " + response);
        } catch (Exception e) {
            System.err.println("GET请求失败: " + e.getMessage());
        }
    }
    
    /**
     * 批量请求示例
     */
    public void batchRequests() {
        System.out.println("=== HMAC客户端示例 ===");
        
        // 1. 基本请求
        System.out.println("\n1. 执行基本HMAC请求:");
        basicHmacRequest();
        
        // 2. 代理用户请求
        System.out.println("\n2. 执行代理用户请求:");
        proxyUserRequest();
        
        // 3. GET请求
        System.out.println("\n3. 执行GET请求:");
        getRequest();
        
        System.out.println("\n=== 示例执行完成 ===");
    }
}
```

### 7. 微信小程序登录

```java
@RestController
public class WxLoginController {
    
    @PostMapping("/thirdPart-login")
    public Result wxLogin(@RequestBody WxLoginRequest request) {
        // 框架会自动处理微信登录逻辑
        // 只需要实现用户信息绑定逻辑
        return Result.success();
    }
}
```

## 高级特性

### 自定义认证处理器

```java
@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {
    
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, 
                                      HttpServletResponse response, 
                                      Authentication authentication) {
        // 自定义登录成功处理逻辑
        // 例如：记录登录日志、发送通知等
    }
}
```

### 自定义验证码存储

```java
@Component
public class CustomSmsVerifyCodeStore implements SmsVerifyCodeStore {
    
    @Override
    public SmsVerifyCode generate(String mobile) {
        // 自定义验证码生成逻辑
        return new SmsVerifyCode(mobile, generateCode(), getExpireTime());
    }
    
    @Override
    public boolean verify(String mobile, String code) {
        // 自定义验证码验证逻辑
        return validateCode(mobile, code);
    }
}
```

### 自定义HMAC客户端服务

```java
@Service
public class DatabaseHmacClientService implements HmacClientService {
    
    @Autowired
    private HmacClientMapper clientMapper;
    
    @Override
    public HmacClient loadClientByAppid(String appid) {
        // 从数据库加载客户端信息
        return clientMapper.findByAppid(appid);
    }
}
```

## 安全最佳实践

### 1. 密码安全
- 使用BCrypt等强加密算法
- 设置密码复杂度要求
- 定期强制更换密码

### 2. 会话安全
- 设置合理的会话超时时间
- 启用同一用户多端登录检查
- 重要操作需要重新验证

### 3. API安全
- HMAC签名防止请求篡改
- 时间戳防止重放攻击
- 限制API调用频率

### 4. 防护策略
- 启用XSS防护
- 配置CSRF防护
- 设置登录失败锁定

## 性能优化

### 1. 缓存策略
- 用户信息缓存
- 权限信息缓存
- 验证码Redis存储

### 2. 连接池配置
```yaml
spring:
  data:
    redis:
     lettuce:
        pool:
        max-active: 20
        max-idle: 10
        min-idle: 5
```

### 3. 异步处理
- 登录日志异步记录
- 短信发送异步处理
- 通知消息异步推送

## 监控和日志

### 1. 关键指标监控
- 登录成功率
- 认证失败次数
- API调用频率
- 会话活跃度

### 2. 安全日志
- 登录失败记录
- 权限拒绝记录
- 异常访问记录
- HMAC签名失败记录

## 故障排查

### 常见问题

1. **登录失败**
   - 检查用户名密码是否正确
   - 确认用户账户是否被锁定
   - 验证验证码是否正确

2. **HMAC认证失败**
   - 检查appid和secret配置
   - 验证时间戳是否在有效范围内
   - 确认签名算法是否正确

3. **短信验证码问题**
   - 检查短信服务配置
   - 确认验证码是否过期
   - 验证手机号格式是否正确

4. **权限拒绝**
   - 检查用户角色和权限配置
   - 确认Sa-Token配置是否正确
   - 验证注解使用是否正确

## 注意事项

## 注意事项

### ⚠️ 重要提醒

#### 1. 生产环境配置
- **短信验证码**：`sms.mock` 必须设置为 `false`
- **XSS防护**：`xssProtected.enabled` 建议设置为 `true`
- **验证码开发模式**：`verify.devMode` 必须设置为 `false`
- **Sa-Token配置**：合理设置 `timeout` 和 `activity-timeout`

#### 2. 依赖要求
- **Redis**：表单登录锁定策略、短信验证码存储需要Redis支持
- **短信服务**：启用短信验证码登录需要集成短信发送服务
- **微信服务**：启用微信小程序登录需要正确的 `appId` 和 `secret`

#### 3. 路径配置
- **默认忽略路径**：`/public/**`, `/v3/**`, `/anon/**`, `*.html`, `*.css`, `*.ico`, `*.js`
- **自定义忽略路径**：通过 `sa-token.ignored` 配置，会与默认路径合并
- **路径匹配**：支持Ant风格路径匹配（`*` 和 `**`）

#### 4. 安全考虑
- **HMAC密钥**：`hmac.clients[].secret` 必须保密存储
- **微信密钥**：`thirdPartLogin.wxMa.secret` 必须保密存储
- **密钥轮换**：定期更换API密钥和应用密钥
- **监控告警**：监控异常登录行为和认证失败
- **安全更新**：及时更新依赖库和安全补丁

#### 5. 配置验证
- **必填配置**：确保启用的功能模块的必要配置项已正确设置
- **配置格式**：注意YAML格式的缩进和数据类型
- **环境隔离**：不同环境使用不同的配置文件
- **敏感信息**：使用环境变量或配置中心管理敏感配置

## 版本兼容性

| 版本     | Spring Boot | Sa-Token | JDK |
|--------|-------------|----------|-----|
| 2025.1 | 3.0.x | 1.36.x | 17+ |

## 许可证

Apache License 2.0

## 贡献指南

欢迎提交Issue和Pull Request来改进这个项目。

## 技术支持

如有问题，请通过以下方式联系：
- 提交GitHub Issue
- 发送邮件至技术支持团队
- 查看在线文档和FAQ
