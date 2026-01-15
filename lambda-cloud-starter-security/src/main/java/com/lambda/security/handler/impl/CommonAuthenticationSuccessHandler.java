package com.lambda.security.handler.impl;

import cn.dev33.satoken.SaManager;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpLogic;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.LoginResponse;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.http.MediaType;

/**
 * 通用认证成功处理器
 * <p>
 * 该类实现了{@link AuthenticationSuccessHandler}接口，提供了认证成功后的标准处理逻辑。
 * 主要负责用户登录后的令牌生成、会话管理、响应处理和事件发布等功能。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>Sa-Token集成：</strong>使用Sa-Token框架进行用户登录和令牌管理</li>
 *   <li><strong>多用户类型支持：</strong>支持不同类型的用户登录（如管理员、普通用户等）</li>
 *   <li><strong>设备识别：</strong>支持多设备登录管理</li>
 *   <li><strong>会话存储：</strong>将用户信息存储到令牌会话中</li>
 *   <li><strong>响应处理：</strong>根据请求类型返回JSON或进行页面重定向</li>
 *   <li><strong>事件发布：</strong>发布用户登录事件供其他组件监听</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>获取登录设备和用户类型信息</li>
 *   <li>使用Sa-Token进行用户登录</li>
 *   <li>存储用户信息到令牌会话</li>
 *   <li>生成访问令牌</li>
 *   <li>根据请求类型返回响应（JSON或重定向）</li>
 *   <li>收集登录统计信息</li>
 *   <li>发布用户登录事件</li>
 * </ol>
 *
 * <h3>响应策略：</h3>
 * <ul>
 *   <li><strong>Ajax请求：</strong>返回JSON格式的令牌信息</li>
 *   <li><strong>普通请求：</strong>重定向到指定页面，令牌信息作为参数传递</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 在安全配置中注册处理器
 * @Bean
 * public AuthenticationSuccessHandler authenticationSuccessHandler(ObjectMapper objectMapper) {
 *     return new CommonAuthenticationSuccessHandler(objectMapper);
 * }
 *
 * // Ajax登录成功响应格式
 * {
 *   "tokenName": "satoken",
 *   "tokenValue": "xxxx-xxxx-xxxx",
 *   "isLogin": true,
 *   "loginId": "user123",
 *   "loginType": "login",
 *   "tokenTimeout": 2592000,
 *   "sessionTimeout": 2592000,
 *   "tokenSessionTimeout": -2,
 *   "tokenActiveTimeout": -1,
 *   "loginDevice": "default-device"
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AuthenticationSuccessHandler
 * @see cn.dev33.satoken.stp.StpLogic
 */
@Slf4j
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP2"},
        justification = "springboot properties")
@SuppressWarnings("all")
public class CommonAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * JSON序列化器
     * <p>
     * 用于将令牌信息序列化为JSON格式，支持Ajax请求的响应处理。
     * </p>
     */
    private final ObjectMapper objectMapper;

    /**
     * 构造函数
     *
     * @param objectMapper JSON序列化器，用于处理令牌信息的序列化
     */
    public CommonAuthenticationSuccessHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 处理认证成功事件
     * <p>
     * 当用户认证成功后，执行登录处理逻辑，包括令牌生成、会话管理、
     * 响应处理和登录事件发布等操作。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li><strong>获取登录上下文：</strong>提取设备信息和用户类型</li>
     *   <li><strong>执行Sa-Token登录：</strong>使用对应的StpLogic进行用户登录</li>
     *   <li><strong>存储用户信息：</strong>将用户对象存储到令牌会话中</li>
     *   <li><strong>生成令牌信息：</strong>获取完整的令牌信息对象</li>
     *   <li><strong>响应处理：</strong>根据请求类型返回JSON或重定向</li>
     *   <li><strong>统计信息收集：</strong>记录登录IP、端口和耗时</li>
     *   <li><strong>事件发布：</strong>发布用户登录事件</li>
     * </ol>
     *
     * <h3>响应处理策略：</h3>
     * <ul>
     *   <li><strong>Ajax请求：</strong>
     *     <ul>
     *       <li>设置200状态码</li>
     *       <li>配置缓存控制头</li>
     *       <li>返回JSON格式的令牌信息</li>
     *     </ul>
     *   </li>
     *   <li><strong>普通请求：</strong>
     *     <ul>
     *       <li>将令牌信息序列化为字符串</li>
     *       <li>构建重定向URL（包含令牌参数）</li>
     *       <li>重定向到目标页面或默认首页</li>
     *     </ul>
     *   </li>
     * </ul>
     *
     * <h3>安全考虑：</h3>
     * <ul>
     *   <li>支持多设备登录管理</li>
     *   <li>支持多用户类型隔离</li>
     *   <li>记录真实客户端IP（需要Nginx配置）</li>
     *   <li>防止IP伪造攻击</li>
     * </ul>
     *
     * @param request   HTTP请求对象，包含登录上下文信息
     * @param response  HTTP响应对象，用于返回登录结果
     * @param loginUser 已认证的用户对象，包含用户详细信息
     * @throws IOException 当I/O操作失败时抛出
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser)
            throws IOException {
        // ========== Sa-Token登录处理 ==========

        // 获取登录设备信息（用于多设备管理）
        String device = (String) request.getAttribute(Constants.LOGIN_DEVICE);

        // 获取用户类型（支持多用户类型登录）
        String loginType = (String) request.getAttribute(Constants.LOGIN_TYPE);

        // 获取对应的StpLogic实例
        StpLogic stpLogic = SaManager.getStpLogic(loginType);

        // 执行用户登录（生成令牌和会话）
        stpLogic.login(loginUser.getName(), device);

        // 将用户信息存储到令牌会话中
        stpLogic.getTokenSession().set(Constants.LOGIN_USER, loginUser);

        // 获取完整的令牌信息
        SaTokenInfo tokenInfo = stpLogic.getTokenInfo();

        LoginResponse loginResponse = new LoginResponse();
        loginResponse.setAccessToken(tokenInfo.getTokenValue());
        loginResponse.setExpiresIn(tokenInfo.getTokenTimeout());
        loginResponse.setDeviceType(tokenInfo.getLoginDeviceType());
        loginResponse.setSubject(tokenInfo.getLoginId());

        // ========== 响应处理 ==========

        if (WebHttpUtils.isAjaxRequest(request)) {
            // Ajax请求：返回JSON格式的令牌信息
            java.io.PrintWriter writer = null;
            try {
                writer = response.getWriter();

                // 设置响应状态和头信息
                response.setStatus(HttpServletResponse.SC_OK);
                response.setHeader("Expires", "0");
                response.setHeader("Pragma", "No-cache");
                response.setHeader("Cache-Control", "no-cache");
                response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                // 序列化令牌信息并返回
                objectMapper.writeValue(writer, loginResponse);
            } finally {
                // 确保资源正确释放
                if (writer != null) {
                    writer.flush();
                    writer.close();
                }
            }
        } else {
            // 普通请求：页面重定向处理

            // 将令牌信息序列化为字符串（用于URL参数）
            String tokenString = objectMapper.writeValueAsString(tokenInfo);

            // 构建重定向URL（包含令牌信息）
            String redirectUrl = WebHttpUtils.getRedirectParameter(request, tokenString);

            if (StringUtils.isNotBlank(redirectUrl)) {
                // 重定向到指定URL
                log.debug("redirectUrl: {}", redirectUrl);
                WebHttpUtils.sendRedirect(request, response, redirectUrl);
            } else {
                // 默认重定向到首页
                WebHttpUtils.sendRedirect(request, response, "/");
            }
        }
    }
}
