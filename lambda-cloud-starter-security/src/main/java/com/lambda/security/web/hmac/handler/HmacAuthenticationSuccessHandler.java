package com.lambda.security.web.hmac.handler;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpLogic;
import cn.dev33.satoken.stp.parameter.SaLoginParameter;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.utils.HmacUtils;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * HMAC认证成功处理器
 * <p>
 * 处理HMAC认证成功后的逻辑，负责生成和管理HMAC认证的会话令牌。
 * 当HMAC认证成功后，该处理器会为用户创建或更新Sa-Token会话，
 * 并在请求头中添加相应的认证令牌。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>会话管理</strong> - 为HMAC认证用户创建和管理会话</li>
 *   <li><strong>令牌生成</strong> - 生成安全的认证令牌</li>
 *   <li><strong>状态维护</strong> - 维护用户的登录状态</li>
 *   <li><strong>请求增强</strong> - 在请求中添加认证信息</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>令牌生成</strong> - 基于用户名和凭据生成SHA256令牌</li>
 *   <li><strong>会话检查</strong> - 检查用户是否已有活跃会话</li>
 *   <li><strong>会话创建</strong> - 为新用户创建Sa-Token会话</li>
 *   <li><strong>请求头设置</strong> - 在请求中添加认证令牌头</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li>验证请求是否为HmacRequestWrapper类型</li>
 *   <li>从请求中解析HMAC授权信息</li>
 *   <li>基于用户凭据生成安全令牌</li>
 *   <li>检查用户是否已有活跃会话</li>
 *   <li>如果有会话，直接添加令牌到请求头</li>
 *   <li>如果无会话，创建新的登录会话</li>
 *   <li>在会话中存储用户信息</li>
 * </ol>
 *
 * <h3>令牌生成算法：</h3>
 * <pre>{@code
 * token = SHA256(username + credentials)
 * }</pre>
 *
 * <h3>会话管理：</h3>
 * <ul>
 *   <li>使用HMAC专用的StpLogic实例</li>
 *   <li>支持会话复用和状态检查</li>
 *   <li>自动处理令牌前缀和格式</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li><strong>令牌安全</strong> - 使用SHA256算法生成令牌</li>
 *   <li><strong>会话隔离</strong> - HMAC会话与其他认证方式隔离</li>
 *   <li><strong>状态一致性</strong> - 确保会话状态的一致性</li>
 *   <li><strong>错误处理</strong> - 优雅处理异常情况</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 配置HMAC认证成功处理器
 * @Bean
 * public HmacAuthenticationSuccessHandler hmacSuccessHandler() {
 *     return new HmacAuthenticationSuccessHandler();
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AuthenticationSuccessHandler
 * @see HmacRequestWrapper
 * @see HmacAuthorization
 * @see StpLogic
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
public class HmacAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    /**
     * 处理HMAC认证成功事件
     * <p>
     * 当HMAC认证成功后，该方法负责为用户创建或更新Sa-Token会话，
     * 生成安全的认证令牌，并将令牌添加到请求头中。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li><strong>请求类型验证</strong> - 确认请求为HmacRequestWrapper类型</li>
     *   <li><strong>授权信息解析</strong> - 从请求中提取HMAC授权信息</li>
     *   <li><strong>令牌生成</strong> - 基于用户凭据生成SHA256安全令牌</li>
     *   <li><strong>会话状态检查</strong> - 检查用户是否已有活跃的Sa-Token会话</li>
     *   <li><strong>会话处理</strong> - 根据会话状态执行相应操作：
     *     <ul>
     *       <li>已有会话：直接在请求头中添加令牌</li>
     *       <li>无会话：创建新的登录会话并存储用户信息</li>
     *     </ul>
     *   </li>
     * </ol>
     *
     * <h3>令牌处理：</h3>
     * <ul>
     *   <li><strong>生成算法</strong> - SHA256(username + credentials)</li>
     *   <li><strong>令牌格式</strong> - 自动添加配置的令牌前缀</li>
     *   <li><strong>头部设置</strong> - 使用StpLogic配置的令牌名称</li>
     * </ul>
     *
     * <h3>会话管理：</h3>
     * <ul>
     *   <li><strong>会话检索</strong> - 通过用户名检索现有会话</li>
     *   <li><strong>会话创建</strong> - 使用SaLoginParameter创建新会话</li>
     *   <li><strong>用户存储</strong> - 在令牌会话中存储LoginUser对象</li>
     * </ul>
     *
     * <h3>错误处理：</h3>
     * <ul>
     *   <li><strong>类型检查</strong> - 验证请求包装器类型</li>
     *   <li><strong>授权验证</strong> - 检查HMAC授权信息的有效性</li>
     *   <li><strong>异常记录</strong> - 记录处理过程中的错误信息</li>
     * </ul>
     *
     * @param request HTTP请求对象，应为HmacRequestWrapper类型
     * @param response HTTP响应对象
     * @param loginUser 认证成功的用户信息，包含用户名和凭据
     *
     * @see HmacRequestWrapper
     * @see HmacAuthorization
     * @see LoginUser
     * @see StpLogic
     */
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, LoginUser loginUser) {
        if (request instanceof HmacRequestWrapper hmacRequestWrapper) {
            HmacAuthorization hmacAuthorization = HmacUtils.getHmacAuthorization(hmacRequestWrapper);
            if (hmacAuthorization == null) {
                log.error(" hmacAuthorization is null");
                return;
            }
            String saToken = SaSecureUtil.sha256(loginUser.getName() + loginUser.getCredentials());
            StpLogic stpLogic = StpLogicUtils.getStpLogic(Constants.HMAC);
            SaSession saSession = stpLogic.getSessionByLoginId(loginUser.getName(), false);
            if (saSession != null) {
                String tokenName = stpLogic.getTokenName();
                hmacRequestWrapper.addHeader(
                        tokenName, stpLogic.getConfigOrGlobal().getTokenPrefix() + " " + saToken);
            } else {
                SaLoginParameter saLoginParameter = stpLogic.createSaLoginParameter();
                saLoginParameter.setToken(saToken);
                stpLogic.login(loginUser.getName(), saLoginParameter);
                stpLogic.getTokenSession().set(Constants.LOGIN_USER, loginUser);
            }
        }
    }
}
