package com.lambda.cloud.mybatis.datascope.support;

import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;

@SuppressFBWarnings("EI_EXPOSE_REP")
public record DataScopeCacheKey(
        String source, DataScopeContext dataScopeContext, String userId, Set<String> permissions) {}
