# Lambda Cloud Test Starter

测试基础依赖模块，提供单元测试和集成测试所需的核心依赖。

## 功能特性

- 提供Spring Boot测试框架支持
- 集成Mockito测试工具
- 包含Lambda Cloud核心模块依赖

## 依赖说明

模块包含以下核心测试依赖：

1. `spring-boot-starter-test` - Spring Boot测试框架
2. `mockito-junit-jupiter` - Mockito JUnit5支持
3. `mockito-inline` - Mockito内联Mock支持

## 使用示例

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.lambda</groupId>
    <artifactId>lambda-cloud-starter-test</artifactId>
    <version>${latest.version}</version>
    <scope>test</scope>
</dependency>
```

### 2. 基础测试类

```java
@SpringBootTest
@AutoConfigureMockMvc
class ExampleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void testExample() throws Exception {
        mockMvc.perform(get("/example"))
               .andExpect(status().isOk());
    }
}
```

### 3. Mock测试示例

```java
@ExtendWith(MockitoExtension.class)
class ExampleServiceTest {

    @Mock
    private ExampleRepository repository;

    @InjectMocks
    private ExampleService service;

    @Test
    void testMockExample() {
        when(repository.findById(any())).thenReturn(Optional.of(new Example()));
        
        Example result = service.getExample(1L);
        assertNotNull(result);
    }
}
```

## 注意事项

1. 该模块仅提供测试依赖，不包含实际业务代码
2. 所有依赖默认scope为test
3. 适用于JUnit5测试框架
