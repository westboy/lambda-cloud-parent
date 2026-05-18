---
name: "lambda-cloud-test"
description: "Lambda Cloud 测试辅助指南。当用户需要编写单元测试、集成测试、使用@LambdaTest注解、断言增强或测试数据构建时调用。"
---

# Lambda Cloud 测试辅助

## 模块引入

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

## @LambdaTest 组合注解

等价整合: `@SpringBootTest` + `@SpringJUnitConfig` + `@ExtendWith(LambdaTestExtension.class)`

```java
@LambdaTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    void testGetUserById() {
        UserVO user = userService.getUserById(1L);
        assertNotNull(user);
    }
}
```

### 参数配置

```java
@LambdaTest(
    classes = UserService.class,
    properties = {"spring.datasource.url=jdbc:h2:mem:testdb"},
    webEnvironment = SpringBootTest.WebEnvironment.MOCK
)
class UserServiceTest {
    // ...
}
```

## LambdaTestExtension 生命周期

自动记录:
- 测试类和测试方法耗时
- 成功/失败/中止/禁用状态日志

```
[INFO] Test UserServiceTest started
[INFO] Test testGetUserById passed (45ms)
[INFO] Test testCreateUser failed (120ms)
```

## LambdaAssertions 断言增强

继承 AssertJ 的 `Assertions`, 增加扩展断言:

### 对象断言

```java
import static com.lambda.cloud.test.assertion.LambdaAssertions.*;

@Test
void testUserObject() {
    UserVO user = userService.getUserById(1L);

    newAssert(user)
        .isNotNull()
        .extracting(UserVO::getUsername)
        .isEqualTo("zhangsan");
}
```

### 集合断言

```java
@Test
void testUserList() {
    List<UserVO> users = userService.listUsers();

    newAssert(users)
        .isNotEmpty()
        .hasSize(10)
        .extracting(UserVO::getUsername)
        .contains("zhangsan", "lisi");
}
```

### 时间断言

```java
@Test
void testCreateTime() {
    UserVO user = userService.getUserById(1L);

    newAssert(user.getCreateTime())
        .isBefore(LocalDateTime.now())
        .isAfter(LocalDateTime.of(2020, 1, 1, 0, 0));
}
```

### 轮询断言

```java
@Test
void testEventually() {
    // 轮询直到条件成立, 最多等待 10 秒
    assertEventually(() -> {
        OrderVO order = orderService.getOrderById(1L);
        assertEquals("PAID", order.getStatus());
    }, Duration.ofSeconds(10), Duration.ofMillis(500));
}
```

### 执行时限断言

```java
@Test
void testCompletesWithin() {
    // 断言在 5 秒内完成
    assertCompletesWithin(() -> {
        userService.processLargeData();
    }, Duration.ofSeconds(5));
}
```

### 异常断言

```java
@Test
void testThrows() {
    assertThrows(BusinessException.class, () -> {
        userService.getUserById(999L);
    });
}

@Test
void testDoesNotThrow() {
    assertDoesNotThrow(() -> {
        userService.getUserById(1L);
    });
}
```

### 执行时间断言

```java
@Test
void testExecutionTime() {
    assertExecutionTime(() -> {
        userService.processData();
    }, Duration.ofMillis(100), Duration.ofSeconds(5));
}
```

## TestDataBuilder 测试数据构建

### 基本用法

```java
@Test
void testWithTestData() {
    // 创建单个对象
    UserEntity user = TestDataBuilder.create(UserEntity.class)
        .with(UserEntity::setUsername, "zhangsan")
        .with(UserEntity::setNickname, "张三")
        .with(UserEntity::setEmail, "zhangsan@example.com")
        .build();

    // 创建列表
    List<UserEntity> users = TestDataBuilder.createList(UserEntity.class, 10)
        .stream()
        .map(builder -> builder
            .with(UserEntity::setUsername, "user_" + System.nanoTime())
            .build())
        .collect(Collectors.toList());
}
```

### 自动填充类型

`TestDataBuilder` 自动为以下类型生成随机值:

| 类型 | 生成规则 |
|------|----------|
| `String` | 随机 UUID |
| `int/Integer` | 随机整数 |
| `long/Long` | 随机长整数 |
| `double/Double` | 随机浮点数 |
| `float/Float` | 随机浮点数 |
| `boolean/Boolean` | 随机布尔值 |
| `BigDecimal` | 随机 BigDecimal |
| `LocalDateTime` | 随机日期时间 |
| `LocalDate` | 随机日期 |
| `Date` | 随机日期 |
| `enum` | 随机枚举值 |

### 深拷贝

```java
@Test
void testDeepCopy() {
    UserEntity original = TestDataBuilder.create(UserEntity.class)
        .with(UserEntity::setUsername, "zhangsan")
        .build();

    UserEntity copy = TestDataBuilder.deepCopy(original);
    assertNotSame(original, copy);
    assertEquals(original.getUsername(), copy.getUsername());
}
```

## MockUtils Mock 辅助

```java
@Test
void testWithMock() {
    UserService mockService = MockUtils.mock(UserService.class);

    MockUtils.when(mockService.getUserById(1L))
        .thenReturn(TestDataBuilder.create(UserVO.class)
            .with(UserVO::setUsername, "mockUser")
            .build());

    UserVO user = mockService.getUserById(1L);
    assertEquals("mockUser", user.getUsername());
}
```

## PerformanceTestUtils 性能测试

```java
@Test
void testPerformance() {
    PerformanceTestUtils.measure("getUserById", 1000, () -> {
        userService.getUserById(1L);
    });
    // 输出: getUserById - 1000 iterations, avg=5ms, min=2ms, max=50ms
}
```

## 完整测试示例

```java
@LambdaTest
class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserMapper userMapper;

    @Test
    void testCreateUser() {
        // 构建测试数据
        UserCreateDTO dto = TestDataBuilder.create(UserCreateDTO.class)
            .with(UserCreateDTO::setUsername, "testuser")
            .with(UserCreateDTO::setNickname, "测试用户")
            .build();

        // 执行
        UserVO user = userService.createUser(dto);

        // 断言
        newAssert(user)
            .isNotNull()
            .extracting(UserVO::getUsername)
            .isEqualTo("testuser");

        // 验证数据库
        UserEntity dbUser = userMapper.selectById(user.getId());
        assertNotNull(dbUser);
        assertEquals("testuser", dbUser.getUsername());
    }

    @Test
    void testGetUserNotFound() {
        assertThrows(BusinessException.class, () -> {
            userService.getUserById(999L);
        });
    }

    @Test
    void testListUsers() {
        // 批量创建测试数据
        List<UserEntity> users = TestDataBuilder.createList(UserEntity.class, 10);
        users.forEach(userMapper::insert);

        // 查询
        List<UserVO> result = userService.listUsers();

        // 断言
        newAssert(result)
            .isNotEmpty()
            .hasSizeGreaterThanOrEqualTo(10);
    }
}
```

## 最佳实践

1. **测试隔离**: 每个测试方法独立, 不依赖其他测试的执行顺序
2. **测试数据**: 使用 `TestDataBuilder` 构建, 避免硬编码
3. **断言选择**: 优先使用 `newAssert()` 扩展断言, 提高可读性
4. **性能测试**: 对关键接口使用 `PerformanceTestUtils` 进行基准测试
5. **Mock 使用**: 外部依赖使用 Mock, 保持测试独立性
6. **覆盖率**: 关注核心业务逻辑的测试覆盖率
