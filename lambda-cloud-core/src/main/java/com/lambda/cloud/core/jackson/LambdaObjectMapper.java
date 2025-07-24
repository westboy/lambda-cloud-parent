package com.lambda.cloud.core.jackson;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;
import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL;
import static com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY;
import static com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping.NON_FINAL;
import static com.fasterxml.jackson.databind.SerializationFeature.INDENT_OUTPUT;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.PolymorphicTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lambda.cloud.core.jackson.deserializer.LambdaLocalDateTimeDeserializer;
import com.lambda.cloud.core.jackson.serializer.LambdaLocalDateTimeSerializer;
import com.lambda.cloud.core.jackson.text.ExtendDateFormat;
import java.time.LocalDateTime;

/**
 * Lambda自定义ObjectMapper
 * <p>
 * 该类继承自Jackson的ObjectMapper，提供了项目特定的JSON序列化和反序列化配置。
 * 主要用于统一项目中的JSON处理行为，包括日期格式、空值处理、时间模块等。
 * </p>
 *
 * <h3>主要配置特性：</h3>
 * <ul>
 *   <li>自定义日期格式处理 - 使用ExtendDateFormat</li>
 *   <li>禁用格式化输出 - 减少JSON体积</li>
 *   <li>空值和空集合处理 - 序列化时排除null和空值</li>
 *   <li>Java 8时间API支持 - 自定义LocalDateTime序列化器</li>
 *   <li>多态类型处理 - 支持继承关系的序列化</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>REST API的JSON响应处理</li>
 *   <li>缓存数据的序列化存储</li>
 *   <li>消息队列的数据传输</li>
 *   <li>配置文件的读写操作</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * LambdaObjectMapper mapper = new LambdaObjectMapper();
 *
 * // 序列化对象
 * String json = mapper.writeValueAsString(userObject);
 *
 * // 反序列化对象
 * User user = mapper.readValue(json, User.class);
 *
 * // 复制mapper实例
 * ObjectMapper copy = mapper.copy();
 * }</pre>
 *
 * @author Jin
 * @see com.fasterxml.jackson.databind.ObjectMapper
 * @see ExtendDateFormat
 * @see LambdaLocalDateTimeSerializer
 * @see LambdaLocalDateTimeDeserializer
 */
public class LambdaObjectMapper extends ObjectMapper {

    /**
     * 构造方法
     * <p>
     * 初始化LambdaObjectMapper并配置所有必要的序列化选项。
     * 该构造方法会设置日期格式、序列化选项、时间模块和多态类型验证器。
     * </p>
     *
     * <h3>配置详情：</h3>
     * <ul>
     *   <li>设置自定义日期格式 - ExtendDateFormat</li>
     *   <li>禁用缩进输出 - 减少JSON大小</li>
     *   <li>排除null值和空值 - 优化传输效率</li>
     *   <li>注册Java时间模块 - 支持LocalDateTime</li>
     *   <li>激活默认类型信息 - 支持多态序列化</li>
     * </ul>
     */
    public LambdaObjectMapper() {
        super();
        this.setDateFormat(new ExtendDateFormat());
        this.disable(INDENT_OUTPUT);
        this.setSerializationInclusion(NON_NULL);
        this.setSerializationInclusion(NON_EMPTY);
        final JavaTimeModule javaTimeModule = new JavaTimeModule();
        javaTimeModule.addSerializer(LocalDateTime.class, new LambdaLocalDateTimeSerializer());
        javaTimeModule.addDeserializer(LocalDateTime.class, new LambdaLocalDateTimeDeserializer());
        this.registerModule(javaTimeModule);
        final PolymorphicTypeValidator polymorphicTypeValidator = getPolymorphicTypeValidator();
        this.activateDefaultTyping(polymorphicTypeValidator, NON_FINAL, PROPERTY);
    }

    /**
     * 复制ObjectMapper实例
     * <p>
     * 创建一个新的LambdaObjectMapper实例，包含与当前实例相同的配置。
     * 这对于需要在不同线程或上下文中使用独立mapper实例的场景很有用。
     * </p>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>返回的是全新的实例，不是浅拷贝</li>
     *   <li>所有配置都会重新初始化</li>
     *   <li>适用于多线程环境下的安全使用</li>
     * </ul>
     *
     * @return 新的LambdaObjectMapper实例
     */
    @Override
    public ObjectMapper copy() {
        return new LambdaObjectMapper();
    }
}
