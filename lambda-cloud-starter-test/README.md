# lambda-cloud-starter-test

`lambda-cloud-starter-test` 是 Lambda Cloud 的测试基础模块，聚合常用测试依赖并提供统一测试注解、断言增强、测试数据构建、Mock 辅助和性能测试工具。

## 模块定位

- 提供可复用的测试工具层，减少业务模块重复编写测试基础代码。
- 统一 JUnit 5 + Spring Test 测试入口与执行日志行为。
- 面向单元测试、集成测试、性能测试提供轻量能力。

## 目录结构（src/main）

```text
src/main/java/com/lambda/cloud/test/
├─ annotation/
│  └─ LambdaTest.java
├─ extension/
│  └─ LambdaTestExtension.java
├─ assertion/
│  ├─ LambdaAssertions.java
│  ├─ LambdaCollectionAssert.java
│  └─ LambdaDateTimeAssert.java
├─ builder/
│  └─ TestDataBuilder.java
├─ mock/
│  └─ MockUtils.java
└─ performance/
   └─ PerformanceTestUtils.java
```

## 设计说明

- 本模块不提供 Spring Boot 自动装配，也不包含 `AutoConfiguration.imports`。
- 能力主要通过静态工具类、JUnit5 扩展和组合注解暴露。
- 推荐在业务模块中以 `test` 作用域引入。

## 核心能力

### 统一测试注解

`@LambdaTest` 是组合注解，等价整合：

- `@SpringBootTest`
- `@SpringJUnitConfig`
- `@ExtendWith(LambdaTestExtension.class)`

支持参数：

- `classes`
- `properties`
- `webEnvironment`（默认 `MOCK`）

### 测试生命周期扩展

`LambdaTestExtension` 实现：

- `BeforeAllCallback`
- `AfterAllCallback`
- `BeforeEachCallback`
- `AfterEachCallback`
- `TestWatcher`

行为：

- 记录测试类和测试方法耗时。
- 输出成功、失败、中止、禁用状态日志。
- 使用 `ExtensionContext.Namespace.GLOBAL` 存储开始时间。

### 断言增强

`LambdaAssertions` 继承 AssertJ 的 `Assertions`，并增加：

- `newAssert(T)`：对象断言（`LambdaObjectAssert`）
- `newAssert(Collection<T>)`：集合断言（`LambdaCollectionAssert`）
- `newAssert(LocalDateTime)`：时间断言（`LambdaDateTimeAssert`）
- `assertEventually(...)`：轮询直到条件成立
- `assertCompletesWithin(...)`：执行时限断言
- `assertThrows(...) / assertDoesNotThrow(...)`
- `assertExecutionTime(...)`：执行时间区间断言

说明：

- 本模块通过 `newAssert(...)` 进入扩展断言，不是 `assertLambda(...)` 方法。

### 测试数据构建

`TestDataBuilder<T>` 提供：

- `create(Class<T>)` + `with(field, value)` + `build()`
- `createList(Class<T>, count)` 批量创建
- `deepCopy(source)` 基于 Jackson JSON 深拷贝

自动填充类型包括：

- `String`
- `int/Integer`、`long/Long`、`double/Double`、`float/Float`
- `boolean/Boolean`
- `BigDecimal`
- `LocalDateTime`、`LocalDate`、`Date`
- `enum`

### Mock 工具

`MockUtils` 提供：

- `createMock(...)`、`createSpy(...)`
- `injectMocks(target).inject(field, dependency)`
- `mockStatic(...)`、`clearStaticMocks()`
- `verify(...)`、`verify(..., times)`、`verifyNever(...)`
- `resetMocks(...)`

说明：

- 依赖注入使用 `ReflectionTestUtils.setField(...)`。
- 静态 Mock 使用内部 `Map<Class<?>, MockedStatic<?>>` 管理。

### 性能测试工具

`PerformanceTestUtils` 提供：

- `measureTime(...)` / `measureTimeWithResult(...)`
- `benchmark(...)`（默认预热 5 次、测量 10 次）
- `concurrentTest(...)`
- `throughputTest(...)`
- `measureMemoryUsage(...)`

结果模型：

- `TimedResult`
- `BenchmarkResult`
- `ConcurrentTestResult`
- `ThroughputTestResult`
- `MemoryUsageResult`

## 快速使用

### 1. 引入依赖

```xml
<dependency>
    <groupId>com.lambda.cloud</groupId>
    <artifactId>lambda-cloud-starter-test</artifactId>
    <version>${lambda-cloud.version}</version>
    <scope>test</scope>
</dependency>
```

### 2. 基础测试示例

```java
import static com.lambda.cloud.test.assertion.LambdaAssertions.*;

@LambdaTest
class UserServiceTest {

    @Test
    void should_validate_user() {
        User user = TestDataBuilder.create(User.class)
                .with("name", "张三")
                .build();

        newAssert(user)
                .isNotNull()
                .hasProperty("name", "张三");
    }
}
```

### 3. 性能测试示例

```java
BenchmarkResult result = PerformanceTestUtils.benchmark(() -> service.process());
assertThat(result.getAverage()).isLessThan(Duration.ofMillis(200));
```

## 依赖说明

关键依赖（见 `pom.xml`）：

- `org.springframework.boot:spring-boot-starter-test`
- `org.junit.jupiter:junit-jupiter`
- `org.junit.jupiter:junit-jupiter-params`
- `org.assertj:assertj-core`
- `org.awaitility:awaitility`
- `org.mockito:mockito-core`
- `org.mockito:mockito-junit-jupiter`
- `org.skyscreamer:jsonassert`
- `io.projectreactor:reactor-test`
- `com.lambda.cloud:lambda-cloud-core`

## 当前实现约束

- `TestDataBuilder` 仅填充当前类 `declaredFields`，不会递归构建嵌套对象。
- `deepCopy` 基于 JSON 序列化，依赖对象可序列化且具备可反序列化结构。
- `LambdaTestExtension` 使用 `Namespace.GLOBAL` 存储计时键，键名固定。
- `MockUtils` 的静态 Mock 需要业务测试主动调用 `clearStaticMocks()` 释放。
- `PerformanceTestUtils#getThroughput()` 在极短时长场景依赖 `Duration.toSeconds()`，可能受秒级精度影响。
