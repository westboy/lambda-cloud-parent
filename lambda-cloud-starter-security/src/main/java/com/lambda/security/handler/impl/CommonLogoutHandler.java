package com.lambda.security.handler.impl;

import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.handler.LogoutHandler;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * 通用登出处理器
 * <p>
 * 该类实现了{@link LogoutHandler}接口，提供了用户登出的标准处理逻辑。
 * 主要负责清理用户会话、注销令牌和相关的安全上下文清理工作。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>会话清理：</strong>清除用户的登录会话信息</li>
 *   <li><strong>令牌注销：</strong>使当前用户的访问令牌失效</li>
 *   <li><strong>安全上下文清理：</strong>清理Sa-Token相关的安全上下文</li>
 *   <li><strong>多用户类型支持：</strong>自动识别并处理不同类型用户的登出</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>获取当前活跃的StpLogic实例</li>
 *   <li>执行用户登出操作</li>
 *   <li>清理相关的会话和令牌信息</li>
 * </ol>
 *
 * <h3>设计特点：</h3>
 * <ul>
 *   <li><strong>简洁高效：</strong>采用最简化的登出处理逻辑</li>
 *   <li><strong>自动识别：</strong>自动获取当前用户对应的StpLogic</li>
 *   <li><strong>安全可靠：</strong>确保完整的会话清理</li>
 *   <li><strong>框架集成：</strong>与Sa-Token框架深度集成</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 在安全配置中注册处理器
 * @Bean
 * public LogoutHandler logoutHandler() {
 *     return new CommonLogoutHandler();
 * }
 *
 * // 登出处理流程
 * // 1. 用户发起登出请求
 * // 2. 系统调用logout方法
 * // 3. 清理用户会话和令牌
 * // 4. 用户登出完成
 * }</pre>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>该处理器只负责核心的登出逻辑，不处理响应</li>
 *   <li>响应处理由{@link com.lambda.security.handler.LogoutSuccessHandler}负责</li>
 *   <li>支持多设备登出和单设备登出</li>
 * </ul>
 *
 * @author jpjoo
 * @see LogoutHandler
 * @see com.lambda.security.handler.LogoutSuccessHandler
 * @see cn.dev33.satoken.stp.StpLogic
 * @see StpLogicUtils
 */
public class CommonLogoutHandler implements LogoutHandler {

    /**
     * 执行用户登出操作
     * <p>
     * 该方法负责清理用户的登录状态，包括会话信息和访问令牌。
     * 使用Sa-Token框架的StpLogic进行标准的登出处理。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li><strong>获取StpLogic：</strong>自动获取当前用户对应的StpLogic实例</li>
     *   <li><strong>执行登出：</strong>调用logout方法清理用户状态</li>
     * </ol>
     *
     * <h3>清理内容：</h3>
     * <ul>
     *   <li>用户的登录会话信息</li>
     *   <li>当前设备的访问令牌</li>
     *   <li>相关的安全上下文</li>
     *   <li>临时存储的用户数据</li>
     * </ul>
     *
     * <h3>自动识别机制：</h3>
     * <p>
     * 通过{@link StpLogicUtils#getActiveStpLogic()}方法自动识别当前用户
     * 对应的StpLogic实例，支持多用户类型的登出处理。
     * </p>
     *
     * @param request HTTP请求对象，包含登出上下文信息
     * @param response HTTP响应对象（本方法中未使用）
     * @param loginUser 当前登录用户对象（本方法中未使用，但保持接口一致性）
     */
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        // 获取当前活跃的StpLogic实例（自动识别用户类型）
        StpLogic stpLogic = StpLogicUtils.getActiveStpLogic();

        // 执行用户登出操作（清理会话和令牌）
        stpLogic.logout();
    }
}
