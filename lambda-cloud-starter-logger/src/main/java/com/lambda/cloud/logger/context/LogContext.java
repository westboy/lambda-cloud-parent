package com.lambda.cloud.logger.context;

import org.slf4j.MDC;

/**
 * @author jpjoo
 */
public final class LogContext {
    private static final String DETAIL = "detail";
    private static final String DESCRIPTION = "description";

    private LogContext() {}

    public static void setDetail(String detail) {
        MDC.put(DETAIL, detail);
    }

    public static String getDetail() {
        return MDC.get(DETAIL);
    }

    public static void setDescription(String description) {
        MDC.put(DESCRIPTION, description);
    }

    public static String getDescription() {
        return MDC.get(DESCRIPTION);
    }

    private static void clear() {
        MDC.clear();
    }
}
