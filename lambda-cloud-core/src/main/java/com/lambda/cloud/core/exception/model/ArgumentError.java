package com.lambda.cloud.core.exception.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serial;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * 参数错误信息类
 * <p>
 * 用于封装请求参数验证失败时的具体错误信息，包含出错的字段名称和详细错误描述。
 * 通常在表单验证、API参数校验等场景中使用。
 *
 * <p>该类实现了 {@link Serializable} 接口，支持序列化操作，
 * 并使用Jackson注解支持JSON序列化和反序列化。
 *
 * <p>使用示例：
 * <pre>{@code
 * ArgumentError error = new ArgumentError("username", "用户名不能为空");
 * }</pre>
 *
 * @author jin
 * @since 1.0.0
 */
@Getter
@Setter
public class ArgumentError implements Serializable {

    @Serial
    private static final long serialVersionUID = 20230615L;

    /**
     * 错误字段名称
     * <p>指出具体哪个字段出现了验证错误，如"username"、"email"等
     */
    private String field;

    /**
     * 错误详细描述
     * <p>对字段错误的具体描述，如"不能为空"、"格式不正确"等
     */
    private String details;

    /**
     * 构造方法
     * <p>
     * 使用Jackson的 {@link JsonCreator} 注解支持从JSON反序列化。
     *
     * @param field 错误字段名称，不应为null
     * @param details 错误详细描述，不应为null
     */
    @JsonCreator
    public ArgumentError(@JsonProperty("field") String field, @JsonProperty("details") String details) {
        this.field = field;
        this.details = details;
    }
}
