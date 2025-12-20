package com.lambda.cloud.mybatis.purview.support;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.PurviewContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import lombok.Data;

/**
 * @author Jin
 */
@Data
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class PurviewProfile {

    public PurviewProfile(LoginUser operator, PurviewContext purview, Set<String> permissions) {
        this.purview = purview;
        this.operator = operator;
        this.permissions = permissions;
    }

    private PurviewContext purview;
    private LoginUser operator;
    private Set<String> permissions;
}
