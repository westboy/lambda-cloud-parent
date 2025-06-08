# Lambda Cloud Core

项目核心模块，提供基础模型、工具类和异常处理等通用功能。

## 核心功能

### 1. 基础模型
- **BaseDO**: 数据对象基类，包含基础字段(id, createTime等)
- **BaseDTO**: 数据传输对象基类
- **BaseVO**: 视图对象基类
- **BasePageDTO**: 分页查询参数基类
- **BaseWrapper**: 通用包装器基类
- **BaseEnum**: 枚举基类接口

### 2. 工具类
- **Assert**: 参数校验工具
```java
Assert.notNull(object, "对象不能为空");
```
- **HmacGenerator**: HMAC签名生成器
```java
String signature = HmacGenerator.hmacSha256("data", "secret");
```
- **OperatorUtils**: 获取当前操作员信息
```java
String operator = OperatorUtils.getCurrentOperator();
```
- **Converter**: 类型转换接口

### 3. 异常处理
- **基础异常**:
  - IllegalAccessException
  - IllegalArgumentException
  - IllegalStateException
  - NotSupportedException

- **Feign异常**:
  - AbstractFeignException
  - FeignAccessDeniedException
  - FeignArgumentNotValidException
  - FeignInternalServerErrorException
  - FeignServiceNotAvailableException
  - FeignUnauthorizedException

- **异常模型**:
  - ErrorCode: 错误码枚举
  - ErrorModel: 错误响应模型
  - ArgumentError: 参数错误详情

### 4. Jackson定制
- **LambdaObjectMapper**: 定制ObjectMapper
- **序列化/反序列化**:
  - LambdaCloudLocalDateTimeSerializer
  - LambdaCloudLocalDateTimeDeserializer
  - PageSerializer
- **ExtendDateFormat**: 扩展日期格式处理

### 5. 其他核心
- **Constants**: 全局常量定义
- **CorsProperties**: CORS跨域配置属性
```yaml
lambda:
  core:
    cors:
      allowed-origins: "*"
      allowed-methods: "*"
```
- **LoginUser**: 登录用户信息模型
- **LoginType**: 登录类型枚举

## 使用示例

### 基础模型继承
```java
public class UserDO extends BaseDO {
    private String username;
    private String password;
}
```

### 异常处理
```java
throw new FeignServiceNotAvailableException("服务不可用");
```

### Jackson配置
```java
@Bean
public LambdaObjectMapper lambdaObjectMapper() {
    return new LambdaObjectMapper();
}
```

## 依赖
- Spring Boot Starter
- Jackson Databind
- Lombok
- Spring Cloud OpenFeign

## 注意事项
1. 基础模型类需配合Lombok使用
2. Feign异常需在Feign拦截器中处理
3. Jackson定制需通过LambdaObjectMapper生效
