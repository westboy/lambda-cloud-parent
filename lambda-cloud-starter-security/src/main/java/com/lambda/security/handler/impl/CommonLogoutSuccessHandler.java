package com.lambda.security.handler.impl;

import cn.hutool.extra.spring.SpringUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.RequestTimeHolder;
import com.lambda.security.events.UserLogoutEvent;
import com.lambda.security.handler.LogoutSuccessHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;

/**
 * 通用登出成功处理器
 * <p>
 * 该类实现了{@link LogoutSuccessHandler}接口，提供了用户登出成功后的标准处理逻辑。
 * 主要负责处理登出成功后的响应返回、统计信息收集和事件发布等功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>响应处理：</strong>根据请求类型返回适当的登出成功响应</li>
 *   <li><strong>统计收集：</strong>记录登出处理的耗时信息</li>
 *   <li><strong>事件发布：</strong>发布用户登出事件供其他组件监听</li>
 *   <li><strong>多请求类型支持：</strong>支持Ajax和普通请求的不同处理策略</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>判断请求类型（Ajax或普通请求）</li>
 *   <li>根据请求类型返回相应的成功响应</li>
 *   <li>计算登出处理耗时</li>
 *   <li>发布用户登出事件</li>
 * </ol>
 *
 * <h3>响应策略：</h3>
 * <ul>
 *   <li><strong>Ajax请求：</strong>
 *     <ul>
 *       <li>设置200状态码</li>
 *       <li>刷新响应流</li>
 *       <li>返回空响应体</li>
 *     </ul>
 *   </li>
 *   <li><strong>普通请求：</strong>
 *     <ul>
 *       <li>获取重定向参数</li>
 *       <li>默认重定向到首页</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 在安全配置中注册处理器
 * @Bean
 * public LogoutSuccessHandler logoutSuccessHandler() {
 *     return new CommonLogoutSuccessHandler();
 * }
 *
 * // Ajax登出成功响应
 * // HTTP 200 OK
 * // (空响应体)
 *
 * // 普通请求登出成功
 * // 重定向到首页或指定页面
 * }</pre>
 *
 * <h3>事件发布：</h3>
 * <p>
 * 登出成功后会发布{@link UserLogoutEvent}事件，包含以下信息：
 * </p>
 * <ul>
 *   <li>登出用户信息</li>
 *   <li>登出处理耗时</li>
 *   <li>登出时间戳</li>
 * </ul>
 *
 * @author jpjoo
 * @see LogoutSuccessHandler
 * @see UserLogoutEvent
 * @see com.lambda.security.handler.LogoutHandler
 */
@SuppressWarnings("all")
public class CommonLogoutSuccessHandler implements LogoutSuccessHandler {

    private final String redirectUrl;

    public CommonLogoutSuccessHandler(String redirectUrl) {
        this.redirectUrl = redirectUrl;
    }

    /**
     * 处理登出成功事件
     * <p>
     * 当用户成功登出后，根据请求类型返回相应的成功响应，
     * 并发布登出事件供系统其他组件进行后续处理。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li><strong>响应处理：</strong>根据请求类型返回适当响应</li>
     *   <li><strong>耗时统计：</strong>计算登出处理的总耗时</li>
     *   <li><strong>事件发布：</strong>发布用户登出事件</li>
     * </ol>
     *
     * <h3>响应处理策略：</h3>
     * <ul>
     *   <li><strong>Ajax请求：</strong>
     *     <ul>
     *       <li>设置HTTP 200状态码</li>
     *       <li>刷新响应流确保数据发送</li>
     *       <li>不返回具体内容（空响应体）</li>
     *     </ul>
     *   </li>
     *   <li><strong>普通请求：</strong>
     *     <ul>
     *       <li>获取重定向参数</li>
     *       <li>默认重定向到首页（"/"）</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <h3>事件发布：</h3>
     * <p>
     * 发布{@link UserLogoutEvent}事件，包含登出用户信息和处理耗时，
     * 供审计日志、统计分析等组件进行后续处理。
     * </p>
     *
     * @param request   HTTP请求对象，用于判断请求类型和获取重定向参数
     * @param response  HTTP响应对象，用于设置响应状态和内容
     * @param loginUser 已登出的用户对象，包含用户详细信息
     * @throws IOException      当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException, ServletException {

        if (WebHttpUtils.isAjaxRequest(request)) {
            response.setStatus(HttpStatus.OK.value());
            response.getWriter().flush();
        } else {
            WebHttpUtils.sendRedirect(request, response, redirectUrl);
        }

        long cast = System.currentTimeMillis() - RequestTimeHolder.getTime();
        SpringUtil.publishEvent(new UserLogoutEvent(loginUser, cast));
    }
}
