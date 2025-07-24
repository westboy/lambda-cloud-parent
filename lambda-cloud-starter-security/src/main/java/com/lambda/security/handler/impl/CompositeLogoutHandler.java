package com.lambda.security.handler.impl;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.security.handler.LogoutHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * 组合登出处理器
 * <p>
 * 该类实现了{@link LogoutHandler}接口，采用组合模式将多个登出处理器组合在一起，
 * 按顺序执行所有注册的登出处理器，实现复杂的登出处理逻辑。
 * </p>
 *
 * <h3>设计模式：</h3>
 * <ul>
 *   <li><strong>组合模式：</strong>将多个LogoutHandler组合成一个统一的处理器</li>
 *   <li><strong>责任链模式：</strong>按顺序执行每个处理器的登出逻辑</li>
 *   <li><strong>策略模式：</strong>支持不同的登出处理策略组合</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>多处理器支持：</strong>支持注册多个登出处理器</li>
 *   <li><strong>顺序执行：</strong>按注册顺序依次执行所有处理器</li>
 *   <li><strong>灵活组合：</strong>可以灵活组合不同的登出处理逻辑</li>
 *   <li><strong>统一接口：</strong>对外提供统一的LogoutHandler接口</li>
 * </ul>
 *
 * <h3>执行流程：</h3>
 * <ol>
 *   <li>接收登出请求</li>
 *   <li>遍历所有注册的登出处理器</li>
 *   <li>按顺序调用每个处理器的logout方法</li>
 *   <li>完成所有处理器的执行</li>
 * </ol>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li><strong>多步骤登出：</strong>需要执行多个登出步骤时</li>
 *   <li><strong>插件化架构：</strong>支持插件化的登出处理扩展</li>
 *   <li><strong>业务解耦：</strong>将不同的登出逻辑分离到不同处理器</li>
 *   <li><strong>功能组合：</strong>组合基础登出功能和扩展功能</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 创建多个登出处理器
 * LogoutHandler basicHandler = new CommonLogoutHandler();
 * LogoutHandler auditHandler = new AuditLogoutHandler();
 * LogoutHandler cacheHandler = new CacheClearLogoutHandler();
 *
 * // 组合多个处理器
 * LogoutHandler compositeHandler = new CompositeLogoutHandler(
 *     basicHandler, auditHandler, cacheHandler
 * );
 *
 * // 或者使用List方式
 * List<LogoutHandler> handlers = Arrays.asList(
 *     basicHandler, auditHandler, cacheHandler
 * );
 * LogoutHandler compositeHandler = new CompositeLogoutHandler(handlers);
 * }</pre>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>处理器执行顺序很重要，应该合理安排</li>
 *   <li>如果某个处理器抛出异常，会影响后续处理器的执行</li>
 *   <li>建议将核心登出逻辑放在前面，扩展功能放在后面</li>
 *   <li>处理器列表不能为空，否则会抛出异常</li>
 * </ul>
 *
 * @author jpjoo
 * @see LogoutHandler
 * @see CommonLogoutHandler
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
public final class CompositeLogoutHandler implements LogoutHandler {
    /**
     * 登出处理器列表
     * <p>
     * 存储所有注册的登出处理器，按注册顺序执行。
     * </p>
     */
    private final List<LogoutHandler> logoutHandlers;

    /**
     * 构造函数（可变参数方式）
     * <p>
     * 通过可变参数的方式注册多个登出处理器。
     * 处理器将按照参数传入的顺序依次执行。
     * </p>
     *
     * @param logoutHandlers 登出处理器数组，不能为空
     * @throws IllegalArgumentException 当处理器数组为空时抛出
     */
    public CompositeLogoutHandler(LogoutHandler... logoutHandlers) {
        Assert.notEmpty(logoutHandlers, "LogoutHandlers are required");
        this.logoutHandlers = Arrays.asList(logoutHandlers);
    }

    /**
     * 构造函数（List方式）
     * <p>
     * 通过List的方式注册多个登出处理器。
     * 处理器将按照List中的顺序依次执行。
     * </p>
     *
     * @param logoutHandlers 登出处理器列表，不能为空
     * @throws IllegalArgumentException 当处理器列表为空时抛出
     */
    public CompositeLogoutHandler(List<LogoutHandler> logoutHandlers) {
        Assert.notEmpty(logoutHandlers, "LogoutHandlers are required");
        this.logoutHandlers = logoutHandlers;
    }

    /**
     * 执行组合登出处理
     * <p>
     * 按照注册顺序依次调用所有登出处理器的logout方法，
     * 实现复合的登出处理逻辑。
     * </p>
     *
     * <h3>执行策略：</h3>
     * <ul>
     *   <li><strong>顺序执行：</strong>严格按照注册顺序执行</li>
     *   <li><strong>全部执行：</strong>尝试执行所有处理器</li>
     *   <li><strong>异常传播：</strong>如果某个处理器抛出异常，会中断后续执行</li>
     * </ul>
     *
     * <h3>典型执行顺序：</h3>
     * <ol>
     *   <li>核心登出处理（清理会话、令牌）</li>
     *   <li>审计日志记录</li>
     *   <li>缓存清理</li>
     *   <li>第三方系统通知</li>
     *   <li>统计信息更新</li>
     * </ol>
     *
     * @param request HTTP请求对象，传递给所有处理器
     * @param response HTTP响应对象，传递给所有处理器
     * @param loginUser 登出用户对象，传递给所有处理器
     */
    public void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        // 按注册顺序依次执行所有登出处理器
        for (LogoutHandler handler : this.logoutHandlers) {
            handler.logout(request, response, loginUser);
        }
    }
}
