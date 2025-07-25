# Lambda Cloud Starter Test

## 概述

`lambda-cloud-starter-test` 是 Lambda Cloud 微服务框架的测试模块，提供了丰富的测试辅助工具和基础依赖，帮助开发者快速构建高质量的单元测试和集成测试。

## 功能特性

- **统一测试注解**：提供 `@LambdaTest` 组合注解，简化测试配置
- **测试扩展支持**：内置 `LambdaTestExtension`，提供测试生命周期管理
- **数据构建工具**：`TestDataBuilder` 提供便捷的测试数据生成
- **Mock 工具集**：`MockUtils` 提供强大的 Mock 对象管理功能
- **断言增强**：`LambdaAssertions` 扩展标准断言，支持业务场景
- **数据库测试**：`TestDatabaseUtils` 提供数据库测试支持
- **性能测试**：`PerformanceTestUtils` 提供性能和基准测试工具
- **测试配置**：自动配置测试环境和属性

## 核心组件

### 1. 统一测试注解

**LambdaTest**：组合注解，集成常用测试配置

```java
@LambdaTest
class MyServiceTest {
    // 测试代码
}

// 等价于
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(LambdaTestExtension.class)
class MyServiceTest {
    // 测试代码
}
```

### 2. 测试数据构建器

**TestDataBuilder**：提供便捷的测试数据生成功能

```java
@Autowired
private TestDataBuilder testDataBuilder;

@Test
void testUserCreation() {
    // 创建测试用户
    User user = testDataBuilder.create(User.class)
        .with("name", "John Doe")
        .with("email", "john@example.com")
        .build();
    
    // 批量创建
    List<User> users = testDataBuilder.createList(User.class, 5);
}
```

### 3. Mock 工具集

**MockUtils**：提供强大的 Mock 对象管理功能

```java
@Test
void testWithMocks() {
    // 创建 Mock 对象
    UserService mockService = MockUtils.createMock(UserService.class);
    
    // 配置 Mock 行为
    MockUtils.when(mockService.findById(1L))
        .thenReturn(new User("John"));
    
    // 验证调用
    MockUtils.verify(mockService).findById(1L);
}
```

### 4. 增强断言

**LambdaAssertions**：扩展标准断言功能

```java
@Test
void testWithEnhancedAssertions() {
    User user = userService.createUser("John", "john@example.com");
    
    // 对象断言
    LambdaAssertions.assertThat(user)
        .isNotNull()
        .hasProperty("name", "John")
        .matches(u -> u.getEmail().contains("@"));
    
    // 集合断言
    List<User> users = userService.findAll();
    LambdaAssertions.assertThat(users)
        .hasSize(1)
        .allMatch(u -> u.getName() != null);
    
    // 时间断言
    LambdaAssertions.assertThat(user.getCreateTime())
        .isRecentWithin(Duration.ofMinutes(1));
    
    // 异步断言
    LambdaAssertions.assertEventually(
        () -> asyncService.isCompleted(), 
        Duration.ofSeconds(5));
}
```

### 5. 数据库测试工具

**TestDatabaseUtils**：提供数据库测试支持

```java
@Autowired
private TestDatabaseUtils dbUtils;

@Test
void testDatabaseOperations() {
    // 清理数据库
    dbUtils.cleanDatabase();
    
    // 执行SQL
    dbUtils.executeSql("INSERT INTO users (name) VALUES ('John')");
    
    // 验证数据
    long count = dbUtils.countRecords("users");
    assertEquals(1, count);
    
    // 查询数据
    List<Map<String, Object>> users = dbUtils.queryForList("SELECT * FROM users");
    assertFalse(users.isEmpty());
}
```

### 6. 性能测试工具

**PerformanceTestUtils**：提供性能和基准测试功能

```java
@Test
void testPerformance() {
    // 测量执行时间
    Duration duration = PerformanceTestUtils.measureTime(() -> {
        service.processLargeDataset();
    });
    
    // 基准测试
    BenchmarkResult result = PerformanceTestUtils.benchmark(
        () -> service.processRequest(), 5, 10);
    
    System.out.println(result); // 输出性能统计
    
    // 并发测试
    ConcurrentTestResult concurrentResult = PerformanceTestUtils.concurrentTest(
        () -> service.handleRequest(), 10, 100);
    
    // 吞吐量测试
    ThroughputTestResult throughputResult = PerformanceTestUtils.throughputTest(
        () -> service.processMessage(), Duration.ofSeconds(10));
}
```

## 快速开始

### 1. 添加依赖

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
    private TestDataBuilder testDataBuilder;
    
    @Autowired
    private TestDatabaseUtils dbUtils;
    
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @BeforeEach
    void setUp() {
        dbUtils.cleanDatabase();
    }
    
    @Test
    void shouldCreateUser() {
        // Given
        User user = testDataBuilder.create(User.class)
            .with("name", "John Doe")
            .with("email", "john@example.com")
            .build();
        
        when(userRepository.save(any(User.class))).thenReturn(user);
        
        // When
        User result = userService.createUser("John Doe", "john@example.com");
        
        // Then
        LambdaAssertions.assertThat(result)
            .isNotNull()
            .hasProperty("name", "John Doe")
            .hasProperty("email", "john@example.com");
        
        verify(userRepository).save(any(User.class));
    }
}
```

### 3. 集成测试示例

```java
@LambdaTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserControllerIntegrationTest {
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Autowired
    private TestDatabaseUtils dbUtils;
    
    @BeforeEach
    void setUp() {
        dbUtils.cleanDatabase();
    }
    
    @Test
    void shouldCreateUserViaApi() {
        // Given
        CreateUserRequest request = new CreateUserRequest("John", "john@example.com");
        
        // When
        ResponseEntity<User> response = restTemplate.postForEntity(
            "/api/users", request, User.class);
        
        // Then
        LambdaAssertions.assertThat(response.getStatusCode())
            .isEqualTo(HttpStatus.CREATED);
        
        LambdaAssertions.assertThat(response.getBody())
            .isNotNull()
            .hasProperty("name", "John");
        
        // 验证数据库
        long userCount = dbUtils.countRecords("users");
        assertEquals(1, userCount);
    }
}
```

## 配置说明

### 测试属性配置

测试模块会自动配置以下属性：

```properties
# 数据库配置
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop

# 日志配置
logging.level.com.lambda.cloud=DEBUG
logging.level.org.springframework=WARN

# Lambda Cloud 配置
lambda.logging.operation.kafka.enabled=false
lambda.cache.enabled=false
```

### 自定义配置

可以通过 `TestConfigurationHelper` 进行自定义配置：

```java
@Autowired
private TestConfigurationHelper configHelper;

@Test
void testWithCustomConfig() {
    // 启用调试模式
    configHelper.enableDebugMode();
    
    // 设置自定义属性
    configHelper.setTestProperty("custom.property", "test-value");
    
    // 执行测试
    // ...
    
    // 清理配置
    configHelper.clearTestProperty("custom.property");
}
```

## 最佳实践

### 1. 测试结构

```java
@LambdaTest
class ServiceTest {
    
    // Given - 准备测试数据
    @BeforeEach
    void setUp() {
        // 初始化测试环境
    }
    
    // When - 执行测试操作
    @Test
    void shouldDoSomething() {
        // Given
        // 准备测试数据
        
        // When
        // 执行被测试的方法
        
        // Then
        // 验证结果
    }
    
    // Then - 清理测试环境
    @AfterEach
    void tearDown() {
        // 清理测试数据
    }
}
```

### 2. 数据管理

- 使用 `TestDataBuilder` 创建测试数据
- 使用 `TestDatabaseUtils` 管理数据库状态
- 每个测试方法前清理数据库
- 使用事务回滚保证测试隔离

### 3. Mock 使用

- 优先使用真实对象进行集成测试
- 对外部依赖使用 Mock
- 使用 `MockUtils` 简化 Mock 配置
- 验证重要的交互行为

### 4. 断言策略

- 使用 `LambdaAssertions` 提供更好的错误信息
- 断言应该具体和有意义
- 避免过度断言
- 使用自定义断言提高可读性

## 核心依赖

- `spring-boot-starter-test`：Spring Boot 测试框架
- `junit-jupiter`：JUnit 5 测试引擎
- `mockito-core`：Mockito Mock 框架
- `mockito-inline`：Mockito 内联支持
- `assertj-core`：AssertJ 断言库
- `awaitility`：异步测试工具
- `h2`：内存数据库（测试用）
- `spring-boot-starter-data-jpa`：JPA 测试支持
- `lambda-cloud-core`：Lambda Cloud 核心模块

## 版本兼容性

- Spring Boot 2.7+
- JUnit 5.8+
- Mockito 4.6+
- Java 8+
