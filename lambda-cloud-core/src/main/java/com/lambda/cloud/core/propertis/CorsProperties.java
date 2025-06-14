package com.lambda.cloud.core.propertis;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.net.HttpHeaders;
import java.util.List;
import lombok.Data;

@Data
public class CorsProperties {

    public static final String ALL_PATH = "/**";

    public static final String ALL = "*";

    public static final ImmutableSet<String> ALLOWED_METHOD = ImmutableSet.copyOf(new String[] {
        "DELETE", "GET", "HEAD", "OPTIONS", "POST", "PUT", "TRACE", "PATCH",
    });

    public static final ImmutableSet<String> EXPOSED_HEADERS =
            ImmutableSet.copyOf(new String[] {HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN});
    public static final ImmutableSet<String> ALLOWED_HEADERS = ImmutableSet.copyOf(
            new String[] {HttpHeaders.AUTHORIZATION, HttpHeaders.CONTENT_TYPE, "X-Content-Type", "x-requested-with"});

    private boolean enabled;

    private List<String> allowedOrigins;

    private long maxAge = 3600L;

    public void setAllowedOrigins(List<String> allowedOrigins) {
        if (allowedOrigins == null) {
            this.allowedOrigins = ImmutableList.of();
        } else {
            this.allowedOrigins = ImmutableList.copyOf(allowedOrigins);
        }
    }
}
