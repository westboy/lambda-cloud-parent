# Lambda Cloud Web Starter

Web基础模块，提供Spring Web MVC相关的基础配置和组件。

## 核心功能

### 1. Web MVC自动配置
- CORS跨域配置
- Jackson序列化配置
- 日期格式化（支持多种日期格式转换）
- 国际化支持

### 2. 全局异常处理
- 400错误处理（参数校验、文件上传等）
- 401/403/500等HTTP状态码处理
- Feign异常处理
- 参数校验错误格式化

### 3. 安全过滤器
- X-Frame-Options响应头设置（防止点击劫持攻击）
- 请求耗时统计

### 4. 租户上下文
- 基于ThreadLocal的租户ID管理
- 支持多租户数据源切换
- 提供租户ID设置、获取和清除方法

### 5. 类型转换
- 字符串到Date类型的自动转换
- 支持多种日期格式解析

## 使用说明

### 1. 添加依赖
```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-web</artifactId>
    <version>${latest.version}</version>
</dependency>
```

### 2. 自动生效功能
以下功能在引入依赖后自动生效：
- Web MVC基础配置
- 全局异常处理
- 安全过滤器
- 日期类型转换

### 3. 租户上下文使用示例
```java
// 设置当前线程租户ID
TenantHolder.setTenantId("tenant1");

try {
    // 业务逻辑...
    String tenantId = TenantHolder.getTenantId();
} finally {
    // 清除租户ID
    TenantHolder.clear();
}
```

## 注意事项
1. 日期格式转换支持标准ISO格式和常见中文格式
2. X-Frame-Options默认设置为SAMEORIGIN
3. 租户上下文基于ThreadLocal实现，需注意及时清理
