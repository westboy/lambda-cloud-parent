package com.lambda.cloud.mybatis.purview.support;

import com.lambda.cloud.core.principal.LoginUser;
import lombok.Data;

import java.util.Set;

/**
 * @author Jin
 */
@Data
public class Parameters {
    public Parameters(LoginUser operator, DynamicPurview purview, Set<String> permissions) {
        this.purview = purview;
        this.operator = operator;
        this.permissions = permissions;
    }

    private DynamicPurview purview;
    private LoginUser operator;
    private Set<String> permissions;


}
