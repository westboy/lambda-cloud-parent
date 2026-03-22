package com.lambda.cloud.mybatis.datascope.support;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import lombok.Data;

/**
 * @author Jin
 */
@Data
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class DataScopeEvaluationContext {

    public DataScopeEvaluationContext(LoginUser operator, DataScopeContext context, Set<String> permissions) {
        this.context = context;
        this.operator = operator;
        this.permissions = permissions;
    }

    private DataScopeContext context;
    private LoginUser operator;
    private Set<String> permissions;
}
