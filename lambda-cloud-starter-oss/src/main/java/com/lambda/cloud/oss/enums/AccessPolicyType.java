package com.lambda.cloud.oss.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * AccessPolicyType
 *
 * @author westboy
 */
@Getter
@AllArgsConstructor
public enum AccessPolicyType {

    /**
     * private
     */
    PRIVATE("private", PolicyType.WRITE),

    /**
     * public
     */
    PUBLIC("public", PolicyType.READ),

    /**
     * custom
     */
    CUSTOM("custom", PolicyType.READ);

    /**
     * 桶 权限类型
     */
    private final String type;

    /**
     * 桶策略类型
     */
    private final PolicyType policyType;

    public static AccessPolicyType getByType(String type) {
        for (AccessPolicyType value : values()) {
            if (value.getType().equals(type)) {
                return value;
            }
        }
        throw new RuntimeException("'type' not found By " + type);
    }
}
