# lambda-cloud-starter-feign Feign客户端模块

## 功能概述
本模块提供Feign客户端的Spring Boot Starter支持，主要功能包括：
1. Feign客户端自动配置
2. 自定义错误解码器(CustomErrorDecoder)
3. 请求拦截器(AuthorizationRequestHeaderInterceptor, HmacClientRequestInterceptor, ClearAuthorizationHeaderInterceptor)
4. WebFlux属性支持(AttributeHolder)
5. 重试机制配置
6. 日志级别配置(FULL)

## 核心依赖
- org.springframework.cloud:spring-cloud-starter-openfeign
- org.springframework.cloud:spring-cloud-starter-loadbalancer
- io.github.openfeign:feign-okhttp
- com.lambda.cloud:lambda-cloud-core
- com.lambda.cloud:lambda-cloud-starter-logger
- com.github.ben-manes.caffeine:caffeine
- org.springframework.retry:spring-retry

## 配置说明
基础配置示例：
```properties
# Feign客户端扫描路径
spring.cloud.openfeign.client.basePackage=com.lambda.cloud

# 重试配置
spring.cloud.openfeign.client.retry.enabled=true
spring.cloud.openfeign.client.retry.maxAttempts=3

# 日志级别
logging.level.com.lambda.cloud.feign=DEBUG
```

## 拦截器说明
### AuthorizationRequestHeaderInterceptor
1. 功能：
   - 自动添加Content-Type: application/json头
   - 处理授权令牌(Authorization头)
   - 支持从请求头或Cookie获取令牌

2. 令牌获取逻辑：
   - 优先从请求头Authorization获取
   - 其次从Cookie(x-authorized-token)获取
   - 如果请求已包含x-security-policy头，则不添加Authorization头

3. 优先级：最高(Integer.MIN_VALUE)

### HmacClientRequestInterceptor
1. 功能：
   - 生成HMAC签名认证
   - 自动添加Authorization头

2. 签名参数：
   - 需要提供appid和secret
   - 包含时间戳(timestamp)
   - 包含查询参数(queries)
   - 包含请求体(body，仅POST/PUT请求)

3. 使用方式：
```java
@Bean
public HmacClientRequestInterceptor hmacInterceptor(
    @Value("${hmac.appid}") String appid,
    @Value("${hmac.secret}") String secret) {
    return new HmacClientRequestInterceptor(appid, secret);
}
```

### ClearAuthorizationHeaderInterceptor
1. 功能：
   - 清除请求中的Authorization头
   - 防止敏感信息泄露

## WebFlux支持
### AttributeHolder
1. 功能：
   - 在WebFlux环境中传递请求属性
   - 支持Reactive上下文

## 注意事项
1. 默认使用OkHttp作为HTTP客户端
2. 内置了请求拦截器和错误处理
3. 版本号继承自父项目${project.parent.version}
4. 需要配合负载均衡器使用
5. HMAC拦截器需要手动配置appid和secret
