package com.lambda.cloud.mybatis.purview.support;

import com.lambda.cloud.mybatis.purview.PurviewContext;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;

@SuppressFBWarnings("EI_EXPOSE_REP")
public record CacheKey(String source, PurviewContext purview, String userId, Set<String> permissions) {}
