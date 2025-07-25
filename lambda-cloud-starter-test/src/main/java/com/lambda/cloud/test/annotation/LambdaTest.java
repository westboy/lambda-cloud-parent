package com.lambda.cloud.test.annotation;

import com.lambda.cloud.test.extension.LambdaTestExtension;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

/**
 * Lambda Cloud 测试注解。
 * <p>
 * 该注解是一个组合注解，集成了常用的测试注解和扩展，
 * 为 Lambda Cloud 项目提供统一的测试配置。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>自动配置 Spring Boot 测试环境</li>
 *     <li>启用 Lambda Cloud 测试扩展</li>
 *     <li>提供统一的测试配置</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * {@code @LambdaTest}
 * class UserServiceTest {
 *
 *     {@code @Autowired}
 *     private UserService userService;
 *
 *     {@code @Test}
 *     void testCreateUser() {
 *         // 测试逻辑
 *     }
 * }
 * </pre>
 *
 * @author Jin
 * @since 1.0.0
 * @see SpringBootTest
 * @see LambdaTestExtension
 */
@SuppressWarnings("all")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@SpringBootTest
@SpringJUnitConfig
@ExtendWith(LambdaTestExtension.class)
public @interface LambdaTest {

    /**
     * 指定测试使用的配置类。
     * <p>
     * 等同于 {@link SpringBootTest#classes()}
     *
     * @return 配置类数组
     */
    Class<?>[] classes() default {};

    /**
     * 指定测试使用的配置属性。
     * <p>
     * 等同于 {@link SpringBootTest#properties()}
     *
     * @return 配置属性数组
     */
    String[] properties() default {};

    /**
     * 指定 Web 环境类型。
     * <p>
     * 等同于 {@link SpringBootTest#webEnvironment()}
     *
     * @return Web 环境类型
     */
    SpringBootTest.WebEnvironment webEnvironment() default SpringBootTest.WebEnvironment.MOCK;
}
