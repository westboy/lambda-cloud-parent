package com.lambda.cloud.oss.enums;

import com.amazonaws.services.s3.model.CannedAccessControlList;
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
    PRIVATE("private", CannedAccessControlList.Private, PolicyType.WRITE),

    /**
     * public
     */
    PUBLIC("public", CannedAccessControlList.PublicRead, PolicyType.READ),

    /**
     * custom
     */
    CUSTOM("custom",CannedAccessControlList.PublicRead, PolicyType.READ);

    /**
     * 桶 权限类型
     */
    private final String type;

    /**
     * 文件对象 权限类型
     */
    private final CannedAccessControlList acl;

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
