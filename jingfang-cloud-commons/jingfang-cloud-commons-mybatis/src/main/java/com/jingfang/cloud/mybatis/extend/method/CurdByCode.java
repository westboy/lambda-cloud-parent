package com.jingfang.cloud.mybatis.extend.method;

import com.baomidou.mybatisplus.core.metadata.TableFieldInfo;

import javax.annotation.Nonnull;

/**
 * CurdByCode
 *
 * @author jpjoo
 */
public interface CurdByCode {

    /**
     * 根据 TableCodeField 注解  获取 code字段
     *
     * @param codeField 数据库表反射信息
     * @return 对应的 code 查询sql  如    ‘code = #{code}’
     */
    default String codeSql(@Nonnull TableFieldInfo codeField) {
        return codeField.getColumn() + " = #{" + codeField.getProperty() + "}";
    }
}
