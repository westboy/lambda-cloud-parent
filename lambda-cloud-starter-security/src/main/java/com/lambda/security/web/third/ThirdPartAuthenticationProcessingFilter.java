package com.lambda.security.web.third;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.LoginError;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.BadCredentialsException;
import com.lambda.security.provider.ThirdPartLoginProvider;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.collections4.MapUtils;

/**
 * 第三方认证处理过滤器
 *
 * <p>专门处理第三方平台（如微信、支付宝、QQ等）的用户认证请求。该过滤器继承自
 * AbstractAuthenticationProcessingFilter，实现了第三方登录的完整流程，包括参数验证、
 * 提供者选择、认证委托等功能。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>多平台支持</strong> - 支持多种第三方登录平台的统一接入</li>
 *   <li><strong>提供者模式</strong> - 通过提供者模式实现不同平台的认证逻辑</li>
 *   <li><strong>配置驱动</strong> - 基于配置文件动态配置第三方登录参数</li>
 *   <li><strong>扩展性强</strong> - 易于添加新的第三方登录平台支持</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>参数验证</strong> - 验证第三方登录所需的参数完整性</li>
 *   <li><strong>提供者选择</strong> - 根据第三方平台ID选择对应的登录提供者</li>
 *   <li><strong>认证委托</strong> - 将具体的认证逻辑委托给对应的提供者</li>
 *   <li><strong>登录类型管理</strong> - 支持不同登录类型的验证和处理</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li><strong>请求解析</strong> - 从JSON请求体中解析第三方登录参数</li>
 *   <li><strong>参数验证</strong> - 验证第三方ID和认证参数不为空</li>
 *   <li><strong>登录类型检查</strong> - 验证登录类型是否支持</li>
 *   <li><strong>提供者查找</strong> - 根据第三方ID查找对应的登录提供者</li>
 *   <li><strong>认证执行</strong> - 调用提供者执行具体的认证逻辑</li>
 *   <li><strong>用户信息返回</strong> - 返回认证成功的用户信息</li>
 * </ol>
 *
 * <h3>支持的第三方平台</h3>
 * <ul>
 *   <li><strong>微信</strong> - 微信小程序、微信公众号登录</li>
 *   <li><strong>支付宝</strong> - 支付宝小程序登录</li>
 *   <li><strong>QQ</strong> - QQ开放平台登录</li>
 *   <li><strong>其他</strong> - 可通过实现ThirdPartLoginProvider接口扩展</li>
 * </ul>
 *
 * <h3>请求格式</h3>
 * <pre>{@code
 * {
 *   "thirdPartId": "wechat",
 *   "params": {
 *     "code": "wx_auth_code",
 *     "appId": "wx_app_id"
 *   }
 * }
 * }</pre>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * lambda:
 *   security:
 *     third-part:
 *       enabled: true
 *       login-url: "/api/auth/third-part"
 *       providers:
 *         - id: wechat
 *           name: 微信登录
 *           enabled: true
 * }</pre>
 *
 * <h3>异常处理</h3>
 * <ul>
 *   <li><strong>参数缺失</strong> - 抛出BadCredentialsException</li>
 *   <li><strong>提供者未找到</strong> - 抛出AuthenticationException</li>
 *   <li><strong>登录类型不支持</strong> - 抛出AuthenticationException</li>
 *   <li><strong>认证失败</strong> - 由具体提供者抛出相应异常</li>
 * </ul>
 *
 * <h3>安全考虑</h3>
 * <ul>
 *   <li><strong>参数验证</strong> - 严格验证所有输入参数</li>
 *   <li><strong>提供者隔离</strong> - 不同提供者之间相互隔离</li>
 *   <li><strong>错误信息</strong> - 避免泄露敏感的错误信息</li>
 *   <li><strong>登录限制</strong> - 支持登录频率限制</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 微信小程序登录
 * POST /api/auth/third-part
 * {
 *   "thirdPartId": "wechat-miniprogram",
 *   "params": {
 *     "code": "wx_login_code",
 *     "encryptedData": "encrypted_user_info",
 *     "iv": "initialization_vector"
 *   }
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AbstractAuthenticationProcessingFilter
 * @see ThirdPartLoginProvider
 * @see SecurityProperties
 * @since 1.0.0
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 配置过滤器
 * @Bean
 * public ThirdPartAuthenticationProcessingFilter thirdPartFilter(
 *         SecurityProperties.ThirdPartLogin config,
 *         List<ThirdPartLoginProvider> providers) {
 *     return new ThirdPartAuthenticationProcessingFilter(config, providers);
 * }
 * }</pre>
 *
 * @author Jin
 * @see AbstractAuthenticationProcessingFilter
 * @see ThirdPartLoginProvider
 * @see SecurityProperties.ThirdPartLogin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class ThirdPartAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 第三方登录配置
     *
     * <p>包含第三方登录的相关配置信息，如登录路径、参数名称等。
     * 这些配置通常来自于application.yml配置文件。</p>
     */
    private final SecurityProperties.ThirdPartLogin thirdPartLogin;

    /**
     * 第三方登录提供者列表
     *
     * <p>包含所有可用的第三方登录提供者实现。每个提供者负责处理
     * 特定平台（如微信、支付宝等）的认证逻辑。</p>
     */
    private final List<ThirdPartLoginProvider> thirdPartLoginProviders;

    /**
     * 构造第三方认证处理过滤器
     *
     * <p>初始化第三方认证过滤器，设置登录路径和提供者列表。
     * 过滤器将根据配置的登录路径拦截相应的请求。</p>
     *
     * @param thirdPartLogin 第三方登录配置，包含路径和参数名称
     * @param thirdPartLoginProviders 第三方登录提供者列表
     */
    public ThirdPartAuthenticationProcessingFilter(
            SecurityProperties.ThirdPartLogin thirdPartLogin, List<ThirdPartLoginProvider> thirdPartLoginProviders) {
        super(thirdPartLogin.getLoginPath());
        this.thirdPartLogin = thirdPartLogin;
        this.thirdPartLoginProviders = thirdPartLoginProviders;
    }

    /**
     * 尝试进行第三方认证
     *
     * <p>这是第三方认证的核心方法，负责完整的第三方登录验证流程。
     * 包括参数解析、验证、提供者选择和认证委托等步骤。</p>
     *
     * <p><strong>认证流程：</strong></p>
     * <ol>
     *   <li><strong>参数解析</strong> - 从JSON请求体中解析第三方登录参数</li>
     *   <li><strong>请求验证</strong> - 验证必需参数的完整性</li>
     *   <li><strong>参数提取</strong> - 提取第三方ID、认证参数和登录类型</li>
     *   <li><strong>登录类型验证</strong> - 检查登录类型是否支持</li>
     *   <li><strong>提供者查找</strong> - 根据第三方ID查找对应的登录提供者</li>
     *   <li><strong>认证委托</strong> - 调用提供者执行具体的认证逻辑</li>
     * </ol>
     *
     * <p><strong>参数说明：</strong></p>
     * <ul>
     *   <li>第三方ID - 标识具体的第三方平台</li>
     *   <li>认证参数 - 第三方平台返回的认证码或token</li>
     *   <li>登录类型 - 可选，默认使用当前系统登录类型</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return 认证成功的用户信息
     * @throws AuthenticationException 如果认证失败或参数无效
     */
    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        Map<String, Object> thirdLogin = getUserLoginForRequestBody(request);

        this.validateRequest(thirdLogin, thirdPartLogin);

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        String authParam = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthParam(), "");
        String loginType = (String) thirdLogin.getOrDefault("loginType", StpUtil.getLoginType());

        boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);
        if (!containsLoginType) {
            throw new AuthenticationException(LoginError.LOGIN_TYPE_ERROR.getCode(), "登录类型错误！");
        }

        ThirdPartLoginProvider loginProvider = getThirdPartLoginProvider(thirdId)
                .orElseThrow(() -> new AuthenticationException(thirdPartLogin.getThirdName() + " is empty"));

        return loginProvider.authenticate(authParam, loginType);
    }

    /**
     * 验证第三方登录请求参数的有效性
     * <p>
     * 该方法负责验证第三方登录请求中的关键参数，确保请求的完整性和有效性。
     * 验证失败时会抛出相应的异常，阻止无效请求的进一步处理。
     * </p>
     *
     * <h3>验证规则：</h3>
     * <ul>
     *   <li>请求体不能为空</li>
     *   <li>第三方平台名称(thirdName)不能为空</li>
     *   <li>第三方授权码(thirdAuthCode)不能为空</li>
     * </ul>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>参数为空时抛出 {@link BadCredentialsException}</li>
     *   <li>异常信息包含具体的错误原因</li>
     * </ul>
     *
     * @param thirdLogin 第三方登录信息Map，包含从请求体解析的参数
     * @param thirdPartLogin 第三方登录配置对象，包含参数名称配置
     * @throws BadCredentialsException 当请求参数无效时抛出
     *
     * @see SecurityProperties.ThirdPartLogin
     * @see BadCredentialsException
     */
    private void validateRequest(Map<String, Object> thirdLogin, SecurityProperties.ThirdPartLogin thirdPartLogin) {
        if (MapUtils.isEmpty(thirdLogin)) {
            throw new BadCredentialsException("Request body is empty");
        }

        String thirdId = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdName(), "");
        if (StrUtil.isBlank(thirdId)) {
            throw new BadCredentialsException("thirdName is empty");
        }

        String code = (String) thirdLogin.getOrDefault(thirdPartLogin.getThirdAuthParam(), "");
        if (StrUtil.isBlank(code)) {
            throw new BadCredentialsException("thirdAuthCode is empty");
        }
    }

    /**
     * 根据第三方平台ID获取对应的登录提供者
     * <p>
     * 该方法从已注册的第三方登录提供者列表中查找支持指定平台的提供者。
     * 使用策略模式，每个提供者负责处理特定平台的认证逻辑。
     * </p>
     *
     * <h3>查找逻辑：</h3>
     * <ul>
     *   <li>遍历所有已注册的 {@link ThirdPartLoginProvider}</li>
     *   <li>调用每个提供者的 {@code support(thirdId)} 方法</li>
     *   <li>返回第一个支持该平台的提供者</li>
     *   <li>如果没有找到支持的提供者，返回空Optional</li>
     * </ul>
     *
     * <h3>支持的平台：</h3>
     * <p>具体支持的平台取决于已注册的提供者实现，常见的包括：</p>
     * <ul>
     *   <li>微信 (wechat)</li>
     *   <li>QQ (qq)</li>
     *   <li>微博 (weibo)</li>
     *   <li>支付宝 (alipay)</li>
     *   <li>钉钉 (dingtalk)</li>
     * </ul>
     *
     * @param thirdId 第三方平台ID，如"wechat"、"qq"等
     * @return 包含支持该平台的第三方登录提供者的Optional，如果没有找到则为空
     *
     * @see ThirdPartLoginProvider
     * @see ThirdPartLoginProvider#support(String)
     */
    private Optional<ThirdPartLoginProvider> getThirdPartLoginProvider(String thirdId) {
        return thirdPartLoginProviders.stream()
                .filter(provider -> provider.support(thirdId))
                .findFirst();
    }
}
