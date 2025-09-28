package com.lambda.cloud.core.shared;

import java.io.Serial;
import java.io.Serializable;
import lombok.Data;
import lombok.experimental.SuperBuilder;

/**
 * 键值对模型类
 * <p>
 * 用于表示简单的键值对数据结构，通常用于下拉选项、字典数据等场景。
 * 包含编码（code）和描述（desc）两个字段。
 *
 * <p>该类实现了 {@link Serializable} 接口，支持序列化操作。
 *
 * @author lambda
 * @since 1.0.0
 */
@SuperBuilder
@Data
public class KeyValue implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 编码值
     * <p>通常用作唯一标识符或键值
     */
    private String code;

    /**
     * 描述信息
     * <p>对编码的文字描述，通常用于显示给用户
     */
    private String desc;
}
