package com.lambda.cloud.logger.context;

import org.slf4j.MDC;

/**
 * 日志上下文工具类，基于 SLF4J MDC 实现线程本地日志变量管理。
 * <p>
 * 该工具类提供了线程安全的日志上下文管理功能，主要用于在操作日志记录过程中
 * 临时存储和传递日志相关的上下文信息，如详情描述、操作说明等。
 * <p>
 * 主要功能：
 * <ul>
 *     <li>设置和获取日志详情信息</li>
 *     <li>设置和获取日志描述信息</li>
 *     <li>清除当前线程的所有日志上下文</li>
 * </ul>
 * <p>
 * 使用示例：
 * <pre>
 * // 设置日志上下文
 * LogContext.setDetail("用户ID: 12345");
 * LogContext.setDescription("用户登录操作");
 * 
 * // 在其他地方获取上下文信息
 * String detail = LogContext.getDetail();
 * String description = LogContext.getDescription();
 * 
 * // 清除上下文（通常在操作完成后）
 * LogContext.clear();
 * </pre>
 * <p>
 * <strong>注意：</strong>由于使用了 MDC，上下文信息仅在当前线程内有效。
 * 在异步操作或线程切换时需要特别注意上下文的传递和清理。
 *
 * @author jpjoo
 * @since 1.0.0
 * @see org.slf4j.MDC
 */
public final class LogContext {

    /**
     * 日志详情在 MDC 中的键名。
     */
    private static final String DETAIL_KEY = "detail";
    
    /**
     * 日志描述在 MDC 中的键名。
     */
    private static final String DESCRIPTION_KEY = "description";

    /**
     * 私有构造方法，防止实例化。
     * <p>
     * 该类设计为工具类，所有方法均为静态方法，不需要实例化。
     */
    private LogContext() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * 设置当前线程的日志详情信息。
     * <p>
     * 日志详情通常包含操作的具体参数、状态信息等详细内容，
     * 用于在日志记录时提供更丰富的上下文信息。
     *
     * @param detail 日志详情内容，可以为 {@code null}
     */
    public static void setDetail(String detail) {
        if (detail != null) {
            MDC.put(DETAIL_KEY, detail);
        } else {
            MDC.remove(DETAIL_KEY);
        }
    }

    /**
     * 获取当前线程的日志详情信息。
     * <p>
     * 如果当前线程未设置日志详情，则返回 {@code null}。
     *
     * @return 日志详情内容，可能为 {@code null}
     */
    public static String getDetail() {
        return MDC.get(DETAIL_KEY);
    }

    /**
     * 设置当前线程的日志描述信息。
     * <p>
     * 日志描述通常是对操作的简要说明，用于快速了解操作的目的和性质。
     *
     * @param description 日志描述内容，可以为 {@code null}
     */
    public static void setDescription(String description) {
        if (description != null) {
            MDC.put(DESCRIPTION_KEY, description);
        } else {
            MDC.remove(DESCRIPTION_KEY);
        }
    }

    /**
     * 获取当前线程的日志描述信息。
     * <p>
     * 如果当前线程未设置日志描述，则返回 {@code null}。
     *
     * @return 日志描述内容，可能为 {@code null}
     */
    public static String getDescription() {
        return MDC.get(DESCRIPTION_KEY);
    }

    /**
     * 清除当前线程的所有日志上下文信息。
     * <p>
     * 该方法会清除当前线程 MDC 中的所有键值对，
     * 建议在操作完成后调用以避免内存泄漏和上下文污染。
     * <p>
     * <strong>注意：</strong>此方法会清除 MDC 中的所有信息，
     * 不仅仅是本工具类设置的日志上下文。
     */
    public static void clear() {
        MDC.clear();
    }
}
