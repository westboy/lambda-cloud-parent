# Lambda Cloud Starter Test

## 概述

`lambda-cloud-starter-test` 是 Lambda Cloud 微服务框架的测试模块，提供了丰富的测试辅助工具和基础依赖，帮助开发者快速构建高质量的单元测试和集成测试。该模块基于 Spring Boot Test、JUnit 5、Mockito 等主流测试框架，提供了统一的测试注解、增强的断言工具、便捷的数据构建器、Mock 工具集和性能测试工具。

## 功能特性

- **🎯 统一测试注解**：`@LambdaTest` 组合注解，简化测试配置
- **🔧 测试扩展支持**：`LambdaTestExtension` 提供测试生命周期管理和监控
- **🏗️ 数据构建工具**：`TestDataBuilder` 提供便捷的测试数据生成
- **🎭 Mock 工具集**：`MockUtils` 提供强大的 Mock 对象管理功能
- **✅ 断言增强**：`LambdaAssertions` 扩展标准断言，支持业务场景
- **⚡ 性能测试**：`PerformanceTestUtils` 提供性能和基准测试工具
- **📊 测试监控**：自动记录测试执行时间和结果

## 核心组件

### 1. 统一测试注解 - @LambdaTest

`@LambdaTest` 是一个组合注解，集成了常用的测试配置：

```java
@LambdaTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser() {
        // 测试逻辑
    }
}

// 等价于
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(LambdaTestExtension.class)
class UserServiceTest {
    // 测试代码
}
```

**支持的配置选项：**
- `classes`：指定测试使用的配置类
- `properties`：指定测试使用的配置属性
- `webEnvironment`：指定 Web 环境类型

### 2. 测试扩展 - LambdaTestExtension

`LambdaTestExtension` 提供测试生命周期管理和监控功能：

**主要功能：**
- ⏱️ 自动记录测试类和测试方法的执行时间
- 📝 详细的测试结果日志记录（成功/失败/跳过/中止）
- 🔄 测试前后的资源管理
- 📊 测试上下文信息收集

**日志输出示例：**
```
开始执行测试类: UserServiceTest
✅ 测试通过: testCreateUser()
测试方法 testCreateUser() 执行完成，耗时: 125 ms
测试类 UserServiceTest 执行完成，总耗时: 1250 ms
```

### 3. 测试数据构建器 - TestDataBuilder

`TestDataBuilder` 提供便捷的测试数据生成功能：

```java
// 创建单个对象
User user = TestDataBuilder.create(User.class)
    .with("name", "张三")
    .with("age", 25)
    .with("email", "zhangsan@example.com")
    .build();

// 批量创建对象
List<User> users = TestDataBuilder.createList(User.class, 10);

// 深度复制对象
User copiedUser = TestDataBuilder.deepCopy(originalUser);
```

**支持的数据类型：**
- 基础类型：String、Integer、Long、Double、Float、Boolean
- 时间类型：LocalDateTime、LocalDate、Date
- 数值类型：BigDecimal
- 枚举类型：自动随机选择枚举值
- 自定义属性：通过 `with()` 方法设置

### 4. Mock 工具集 - MockUtils

`MockUtils` 提供强大的 Mock 对象管理功能：

```java
// 创建 Mock 对象
UserRepository mockRepo = MockUtils.createMock(UserRepository.class);

// 创建并配置 Mock 对象
UserRepository mockRepo = MockUtils.createMock(UserRepository.class, mock -> {
    when(mock.findById(anyLong())).thenReturn(Optional.of(new User()));
    when(mock.save(any(User.class))).thenReturn(new User());
});

// 创建 Spy 对象
UserService spyService = MockUtils.createSpy(realUserService);

// 批量注入依赖
UserService service = new UserService();
MockUtils.injectMocks(service)
    .inject("userRepository", mockRepo)
    .inject("emailService", mockEmailService)
    .inject("cacheService", mockCacheService);

// Mock 静态方法
try (MockedStatic<DateUtils> mockedStatic = MockUtils.mockStatic(DateUtils.class)) {
    mockedStatic.when(DateUtils::getCurrentTime)
               .thenReturn(LocalDateTime.of(2023, 1, 1, 12, 0));
    // 测试逻辑
}

// 验证交互
MockUtils.verify(mockRepo).findById(1L);
MockUtils.verify(mockRepo, 2).save(any(User.class));
MockUtils.verifyNever(mockRepo).deleteById(anyLong());
```

### 5. 增强断言 - LambdaAssertions

`LambdaAssertions` 扩展了标准断言功能，提供更丰富的断言方法：

#### 对象断言
```java
User user = userService.createUser("张三", "zhangsan@example.com");

// 使用 assertLambda 方法（推荐）
LambdaAssertions.assertLambda(user)
    .isNotNull()
    .isEqualTo(expectedUser)
    .isInstanceOf(User.class)
    .hasProperty("name", "张三")
    .hasProperty("email", "zhangsan@example.com")
    .toStringContains("张三")
    .hasHashCode(expectedHashCode)
    .isSameAs(user)
    .isNotSameAs(otherUser);

// 也可以使用 assertThat 方法
LambdaAssertions.assertThat(user).isNotNull();
```

#### 集合断言
```java
List<User> users = userService.findAll();

LambdaAssertions.assertLambda(users)
    .isNotNull()
    .isNotEmpty()
    .hasSize(3)
    .contains(expectedUser)
    .doesNotContain(unexpectedUser)
    .allMatch(u -> u.getName() != null)
    .anyMatch(u -> u.getAge() > 18)
    .noneMatch(u -> u.getEmail() == null);
```

#### 时间断言
```java
LocalDateTime createTime = user.getCreateTime();

LambdaAssertions.assertLambda(createTime)
    .isNotNull()
    .isAfter(startTime)
    .isBefore(endTime)
    .isAfterOrEqualTo(startTime)
    .isBeforeOrEqualTo(endTime)
    .isBetween(startTime, endTime)
    .isToday()
    .isCloseTo(LocalDateTime.now(), 5); // 5秒容忍度
```

#### 异常断言
```java
// 断言抛出异常
RuntimeException exception = LambdaAssertions.assertThrows(RuntimeException.class, () -> {
    userService.createUser(null, null);
});

LambdaAssertions.assertLambda(exception.getMessage())
    .contains("用户名不能为空");

// 断言不抛出异常
LambdaAssertions.assertDoesNotThrow(() -> {
    userService.createUser("张三", "zhangsan@example.com");
});

// 异步断言
LambdaAssertions.assertEventually(
    () -> asyncService.isCompleted(), 
    Duration.ofSeconds(5)
);

// 在指定时间内完成
LambdaAssertions.assertCompletesWithin(
    () -> longRunningService.process(),
    Duration.ofSeconds(10)
);
```

#### 性能断言
```java
// 断言执行时间
LambdaAssertions.assertExecutionTime(
    () -> service.processLargeDataset(),
    Duration.ofSeconds(5) // 最大允许执行时间
);
```

### 6. 性能测试工具 - PerformanceTestUtils

`PerformanceTestUtils` 提供全面的性能测试功能：

#### 执行时间测量
```java
// 测量执行时间
Duration duration = PerformanceTestUtils.measureTime(() -> {
    service.processLargeDataset();
});

// 测量带返回值的操作
PerformanceTestUtils.TimedResult<String> result = 
    PerformanceTestUtils.measureTimeWithResult(() -> {
        return service.generateReport();
    });

System.out.println("结果: " + result.getResult());
System.out.println("耗时: " + result.getDuration());
```

#### 基准测试
```java
// 基准测试（默认参数：5次预热，10次测量）
PerformanceTestUtils.BenchmarkResult result = 
    PerformanceTestUtils.benchmark(() -> {
        service.processRequest();
    });

// 自定义参数的基准测试
PerformanceTestUtils.BenchmarkResult result = 
    PerformanceTestUtils.benchmark(
        () -> service.processRequest(),
        10,  // 预热次数
        20   // 测量次数
    );

System.out.println(result);
// 输出: BenchmarkResult{平均: PT0.125S, 最小: PT0.100S, 最大: PT0.150S, 标准差: PT0.015S, 样本数: 20, 每秒操作数: 8.00}
```

#### 并发性能测试
```java
// 并发测试：10个线程，每个线程执行100次
PerformanceTestUtils.ConcurrentTestResult result = 
    PerformanceTestUtils.concurrentTest(
        () -> service.handleRequest(),
        10,   // 线程数
        100   // 每个线程的执行次数
    );

System.out.println(result);
// 输出: ConcurrentTestResult{线程数: 10, 每线程操作数: 100, 总时间: PT5.234S, 平均执行时间: PT0.052S, 吞吐量: 191.06 ops/s}
```

#### 吞吐量测试
```java
// 吞吐量测试：持续10秒
PerformanceTestUtils.ThroughputTestResult result = 
    PerformanceTestUtils.throughputTest(
        () -> service.processMessage(),
        Duration.ofSeconds(10)
    );

System.out.println(result);
// 输出: ThroughputTestResult{执行次数: 1250, 测试时长: PT10S, 吞吐量: 125.00 ops/s, 平均执行时间: PT0.008S}
```

#### 内存使用测试
```java
// 内存使用测试
PerformanceTestUtils.MemoryUsageResult result = 
    PerformanceTestUtils.measureMemoryUsage(() -> {
        service.loadLargeDataset();
    });

System.out.println(result);
// 输出: MemoryUsageResult{使用前: 52428800 bytes, 使用后: 104857600 bytes, 使用量: 52428800 bytes (50.00 MB)}
```

## 快速开始

### 1. 添加依赖

在项目的 `pom.xml` 中添加依赖：

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-test</artifactId>
    <version>1.0.0-SNAPSHOT</version>
    <scope>test</scope>
</dependency>
```

### 2. 基础测试示例

```java
@LambdaTest
class UserServiceTest {
    
    @Autowired
    private UserService userService;
    
    @MockBean
    private UserRepository userRepository;
    
    @Test
    void shouldCreateUser() {
        // Given
        User expectedUser = TestDataBuilder.create(User.class)
            .with("name", "张三")
            .with("email", "zhangsan@example.com")
            .build();
        
        when(userRepository.save(any(User.class))).thenReturn(expectedUser);
        
        // When
        User result = userService.createUser("张三", "zhangsan@example.com");
        
        // Then
        LambdaAssertions.assertLambda(result)
            .isNotNull()
            .hasProperty("name", "张三")
            .hasProperty("email", "zhangsan@example.com");
        
        MockUtils.verify(userRepository).save(any(User.class));
    }
    
    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        // When & Then
        IllegalArgumentException exception = LambdaAssertions.assertThrows(
            IllegalArgumentException.class,
            () -> userService.createUser(null, "test@example.com")
        );
        
        LambdaAssertions.assertLambda(exception.getMessage())
            .contains("用户名不能为空");
    }
}
```

### 3. 集成测试示例

```java
@LambdaTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void shouldCreateUserViaApi() {
        // Given
        CreateUserRequest request = new CreateUserRequest("张三", "zhangsan@example.com");
        
        // When
        ResponseEntity<User> response = restTemplate.postForEntity(
            "/api/users", request, User.class);
        
        // Then
        LambdaAssertions.assertLambda(response.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        
        LambdaAssertions.assertLambda(response.getBody())
            .isNotNull()
            .hasProperty("name", "张三")
            .hasProperty("email", "zhangsan@example.com");
    }
}
```

### 4. 性能测试示例

```java
@LambdaTest
class UserServicePerformanceTest {
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUserPerformance() {
        // 基准测试
        PerformanceTestUtils.BenchmarkResult benchmark = 
            PerformanceTestUtils.benchmark(() -> {
                userService.createUser("测试用户", "test@example.com");
            });
        
        // 断言平均执行时间小于100ms
        LambdaAssertions.assertLambda(benchmark.getAverage())
            .isLessThan(Duration.ofMillis(100));
        
        System.out.println("创建用户性能测试结果: " + benchmark);
    }
    
    @Test
    void testConcurrentUserCreation() {
        // 并发测试：5个线程，每个线程创建20个用户
        PerformanceTestUtils.ConcurrentTestResult result = 
            PerformanceTestUtils.concurrentTest(
                () -> userService.createUser(
                    "并发用户" + System.nanoTime(), 
                    "concurrent@example.com"
                ),
                5,   // 线程数
                20   // 每线程执行次数
            );
        
        // 断言吞吐量大于50 ops/s
        LambdaAssertions.assertLambda(result.getThroughput())
            .isGreaterThan(50.0);
        
        System.out.println("并发创建用户测试结果: " + result);
    }
}
```

## 最佳实践

### 1. 测试结构

遵循 **Given-When-Then** 模式：

```java
@LambdaTest
class ServiceTest {
    
    @Test
    void shouldDoSomething() {
        // Given - 准备测试数据和环境
        User user = TestDataBuilder.create(User.class)
            .with("name", "测试用户")
            .build();
        
        UserRepository mockRepo = MockUtils.createMock(UserRepository.class, mock -> {
            when(mock.findById(1L)).thenReturn(Optional.of(user));
        });
        
        MockUtils.injectMocks(userService)
            .inject("userRepository", mockRepo);
        
        // When - 执行被测试的方法
        User result = userService.getUserById(1L);
        
        // Then - 验证结果
        LambdaAssertions.assertLambda(result)
            .isNotNull()
            .isEqualTo(user);
        
        MockUtils.verify(mockRepo).findById(1L);
    }
}
```

### 2. 数据管理

- **使用 TestDataBuilder**：统一管理测试数据创建
- **避免硬编码**：使用构建器模式设置测试数据
- **数据隔离**：每个测试方法使用独立的测试数据

```java
// ✅ 推荐
User user = TestDataBuilder.create(User.class)
    .with("name", "张三")
    .with("age", 25)
    .build();

// ❌ 不推荐
User user = new User();
user.setName("张三");
user.setAge(25);
```

### 3. Mock 使用策略

- **优先集成测试**：对于简单的依赖，优先使用真实对象
- **Mock 外部依赖**：对于数据库、网络调用等外部依赖使用 Mock
- **验证重要交互**：验证关键的方法调用和参数

```java
// Mock 外部服务
EmailService mockEmailService = MockUtils.createMock(EmailService.class);

// 验证重要交互
MockUtils.verify(mockEmailService).sendWelcomeEmail(user.getEmail());
```

### 4. 断言策略

- **使用 LambdaAssertions**：提供更好的错误信息和链式调用
- **断言要具体**：避免过于宽泛的断言
- **组合断言**：使用链式调用组合多个断言

```java
// ✅ 推荐：具体且有意义的断言
LambdaAssertions.assertLambda(user)
    .isNotNull()
    .hasProperty("name", "张三")
    .hasProperty("status", UserStatus.ACTIVE);

// ❌ 不推荐：过于宽泛的断言
assertThat(user).isNotNull();
```

### 5. 性能测试指南

- **基准测试**：用于测量单个操作的性能
- **并发测试**：用于测试系统在并发场景下的表现
- **吞吐量测试**：用于测试系统的处理能力
- **设置合理的性能目标**：根据业务需求设置性能断言

```java
// 设置合理的性能目标
PerformanceTestUtils.BenchmarkResult result = 
    PerformanceTestUtils.benchmark(() -> service.processRequest());

// 根据业务需求设置断言
LambdaAssertions.assertLambda(result.getAverage())
    .isLessThan(Duration.ofMillis(200)); // 平均响应时间小于200ms

LambdaAssertions.assertLambda(result.getOperationsPerSecond())
    .isGreaterThan(100.0); // 每秒处理超过100个请求
```

## 核心依赖

该模块基于以下核心依赖构建：

```xml
<!-- 测试框架 -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
</dependency>

<!-- JUnit 5 -->
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
</dependency>
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter-params</artifactId>
</dependency>

<!-- Mockito -->
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-core</artifactId>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-inline</artifactId>
</dependency>
<dependency>
    <groupId>org.mockito</groupId>
    <artifactId>mockito-junit-jupiter</artifactId>
</dependency>

<!-- 断言库 -->
<dependency>
    <groupId>org.assertj</groupId>
    <artifactId>assertj-core</artifactId>
</dependency>

<!-- 异步测试 -->
<dependency>
    <groupId>org.awaitility</groupId>
    <artifactId>awaitility</artifactId>
</dependency>

<!-- 数据库测试 -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
    <optional>true</optional>
</dependency>

<!-- Lambda Cloud 核心 -->
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-core</artifactId>
</dependency>
```

