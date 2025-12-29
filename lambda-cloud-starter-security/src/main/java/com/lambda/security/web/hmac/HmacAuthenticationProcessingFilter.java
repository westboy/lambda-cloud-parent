package com.lambda.security.web.hmac;

import cn.dev33.satoken.context.SaTokenContextForThreadLocalStaff;
import cn.dev33.satoken.servlet.model.SaRequestForServlet;
import cn.dev33.satoken.servlet.model.SaResponseForServlet;
import cn.dev33.satoken.servlet.model.SaStorageForServlet;
import cn.dev33.satoken.stp.StpUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.encode.HmacShaEncoder;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.BadCredentialsException;
import com.lambda.security.exception.UsernameNotFoundException;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.model.HmacClient;
import com.lambda.security.web.hmac.utils.HmacUtils;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

/**
 * HMAC认证处理过滤器
 *
 * <p>专门处理基于HMAC（Hash-based Message Authentication Code）签名的API认证请求。
 * 该过滤器继承自AbstractAuthenticationProcessingFilter，实现了HMAC认证的完整流程，
 * 包括签名验证、客户端验证、IP白名单检查、用户切换等功能。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>API安全</strong> - 提供基于HMAC签名的API安全认证机制</li>
 *   <li><strong>防重放攻击</strong> - 通过时间戳和签名防止请求重放</li>
 *   <li><strong>访问控制</strong> - 支持IP白名单和客户端权限控制</li>
 *   <li><strong>用户代理</strong> - 支持HMAC客户端代理其他用户进行操作</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>HMAC验证</strong> - 验证请求的HMAC签名是否正确</li>
 *   <li><strong>客户端认证</strong> - 根据appid加载和验证客户端信息</li>
 *   <li><strong>IP白名单</strong> - 检查请求IP是否在客户端白名单中</li>
 *   <li><strong>用户切换</strong> - 支持HMAC客户端代理其他用户登录</li>
 *   <li><strong>上下文管理</strong> - 管理SaToken认证上下文</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li><strong>请求包装</strong> - 将HttpServletRequest包装为HmacRequestWrapper</li>
 *   <li><strong>解析授权</strong> - 从请求头中解析HMAC授权信息</li>
 *   <li><strong>客户端验证</strong> - 根据appid加载客户端并检查IP白名单</li>
 *   <li><strong>签名验证</strong> - 计算期望签名并与请求签名比较</li>
 *   <li><strong>用户切换</strong> - 如果指定了运行用户，则切换到该用户</li>
 *   <li><strong>认证成功</strong> - 设置认证上下文并继续过滤链</li>
 * </ol>
 *
 * <h3>HMAC签名算法</h3>
 * <pre>{@code
 * // 签名计算步骤
 * String salt = appid + timestamp + requestBody;
 * String signature = HMAC-SHA256(clientSecret, salt);
 * }</pre>
 *
 * <h3>请求格式</h3>
 * <pre>{@code
 * // HTTP头部
 * Authorization: HMAC appid=xxx,digest=xxx,timestamp=xxx
 *
 * // 可选参数
 * hmac-run-user: 目标用户ID（用于用户切换）
 * hmac-run-type: 登录类型（默认使用当前登录类型）
 * }</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 配置过滤器
 * @Bean
 * public HmacAuthenticationProcessingFilter hmacFilter(
 *         HmacClientService clientService,
 *         HmacShaEncoder encoder) {
 *     return new HmacAuthenticationProcessingFilter(clientService, encoder);
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AbstractAuthenticationProcessingFilter
 * @see HmacClientService
 * @see HmacShaEncoder
 * @see HmacRequestWrapper
 */
@Slf4j
public class HmacAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * HMAC运行用户参数名
     *
     * <p>用于指定HMAC客户端要代理的目标用户ID。当HMAC客户端需要以其他用户身份
     * 执行操作时，可以通过此参数指定目标用户。</p>
     */
    private static final String REQUEST_HMAC_RUN_USER = "hmac-run-user";

    /**
     * HMAC运行类型参数名
     *
     * <p>用于指定登录类型。如果未指定，则使用当前默认的登录类型。
     * 不同的登录类型可能对应不同的用户体系或权限范围。</p>
     */
    private static final String REQUEST_HMAC_TYPE = "hmac-run-type";

    /**
     * HMAC客户端服务
     *
     * <p>用于加载和验证HMAC客户端信息，包括客户端密钥、权限、IP白名单等。
     * 也负责处理用户切换逻辑。</p>
     */
    private final HmacClientService hmacClientService;

    /**
     * HMAC密码编码器
     *
     * <p>用于HMAC签名的计算和验证。负责将客户端密钥和盐值进行HMAC-SHA编码，
     * 生成用于比较的签名摘要。</p>
     */
    private final HmacShaEncoder passwordEncoder;

    /**
     * 构造HMAC认证处理过滤器
     *
     * <p>初始化HMAC认证过滤器，设置必要的服务依赖。该过滤器不需要指定
     * 特定的URL模式，因为它通过请求特征来判断是否需要处理。</p>
     *
     * @param hmacClientService HMAC客户端服务，不能为null
     * @param passwordEncoder HMAC密码编码器，用于签名验证
     * @throws IllegalArgumentException 如果hmacClientService为null
     */
    public HmacAuthenticationProcessingFilter(HmacClientService hmacClientService, HmacShaEncoder passwordEncoder) {
        super(null);
        this.hmacClientService = hmacClientService;
        Assert.notNull(hmacClientService, "hmacClientService must not be null");
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 检索并验证HMAC客户端
     *
     * <p>根据应用ID（appid）加载HMAC客户端信息，并验证请求来源IP是否在
     * 客户端的白名单中。这是HMAC认证的第一步验证。</p>
     *
     * <p><strong>验证流程：</strong></p>
     * <ol>
     *   <li>通过appid从客户端服务加载用户信息</li>
     *   <li>检查加载的用户是否为HmacClient类型</li>
     *   <li>如果客户端配置了IP白名单，验证请求IP是否在白名单中</li>
     *   <li>返回验证通过的客户端信息</li>
     * </ol>
     *
     * <p><strong>安全考虑：</strong></p>
     * <ul>
     *   <li>IP白名单验证可以防止客户端密钥泄露后的滥用</li>
     *   <li>只有配置了hosts的客户端才会进行IP验证</li>
     *   <li>验证失败时抛出BadCredentialsException而不是具体的错误信息</li>
     * </ul>
     *
     * @param username 客户端应用ID（appid）
     * @param remoteAddr 请求来源IP地址
     * @return 验证通过的HMAC客户端信息
     * @throws UsernameNotFoundException 如果客户端不存在或类型不匹配
     * @throws BadCredentialsException 如果IP地址不在白名单中
     */
    protected HmacClient retrieveUser(String username, String remoteAddr) {
        LoginUser loadedUser = this.hmacClientService.loadClientByAppid(username);
        if (loadedUser instanceof HmacClient client) {
            if (StringUtils.isNotBlank(client.getHosts())) {
                Set<String> hosts = client.getWhitelist();
                if (!hosts.contains(remoteAddr)) {
                    log.debug("actual: {}, expected: {}", remoteAddr, hosts);
                    throw new BadCredentialsException("Password does not match stored value");
                }
            }
            return client;
        }
        throw new UsernameNotFoundException("Client " + username + "not found.");
    }

    /**
     * 处理认证成功后的逻辑
     *
     * <p>重写父类的认证成功处理方法，主要负责设置SaToken上下文环境，
     * 确保后续的请求处理能够正确访问认证信息。</p>
     *
     * <p><strong>处理流程：</strong></p>
     * <ol>
     *   <li>设置SaToken的ThreadLocal上下文模型</li>
     *   <li>调用父类的认证成功处理逻辑</li>
     *   <li>继续执行过滤器链</li>
     *   <li>在finally块中清理ThreadLocal上下文</li>
     * </ol>
     *
     * <p><strong>上下文管理：</strong></p>
     * <ul>
     *   <li>SaRequestForServlet - 包装HTTP请求对象</li>
     *   <li>SaResponseForServlet - 包装HTTP响应对象</li>
     *   <li>SaStorageForServlet - 提供会话存储功能</li>
     * </ul>
     *
     * <p><strong>注意事项：</strong>必须在finally块中清理ThreadLocal，
     * 防止内存泄漏和上下文污染。</p>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @param chain 过滤器链
     * @param loginUser 认证成功的用户信息
     * @throws IOException 如果I/O操作失败
     * @throws ServletException 如果Servlet处理失败
     */
    @Override
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser)
            throws IOException, ServletException {
        SaTokenContextForThreadLocalStaff.setModelBox(
                new SaRequestForServlet(request), new SaResponseForServlet(response), new SaStorageForServlet(request));
        try {
            super.successfulAuthentication(request, response, chain, loginUser);
            chain.doFilter(request, response);
        } finally {
            SaTokenContextForThreadLocalStaff.clearModelBox();
        }
    }

    /**
     * 尝试进行HMAC认证
     *
     * <p>这是HMAC认证的核心方法，负责完整的HMAC签名验证流程。
     * 包括授权信息解析、客户端验证、签名计算与比较、用户切换等步骤。</p>
     *
     * <p><strong>认证流程：</strong></p>
     * <ol>
     *   <li><strong>请求验证</strong> - 确认请求已被包装为HmacRequestWrapper</li>
     *   <li><strong>授权解析</strong> - 从Authorization头部解析HMAC认证信息</li>
     *   <li><strong>客户端验证</strong> - 根据appid加载客户端并检查IP白名单</li>
     *   <li><strong>签名验证</strong> - 计算期望签名并与请求签名比较</li>
     *   <li><strong>登录类型处理</strong> - 验证和设置登录类型</li>
     *   <li><strong>用户切换</strong> - 如果指定了运行用户，则切换到该用户</li>
     *   <li><strong>上下文设置</strong> - 设置登录类型和设备信息</li>
     * </ol>
     *
     * <p><strong>签名验证算法：</strong></p>
     * <pre>{@code
     * // 1. 构造盐值
     * String salt = appid + timestamp + requestBody;
     *
     * // 2. 计算期望签名
     * String expectedDigest = HMAC-SHA256(clientSecret, salt);
     *
     * // 3. 比较签名
     * boolean valid = expectedDigest.equals(requestDigest);
     * }</pre>
     *
     * <p><strong>用户切换机制：</strong></p>
     * <ul>
     *   <li>HMAC客户端可以通过hmac-run-user参数指定要代理的用户</li>
     *   <li>可以通过hmac-run-type参数指定登录类型</li>
     *   <li>用户切换需要客户端具有相应的权限</li>
     * </ul>
     *
     * @param request HTTP请求对象，应该是HmacRequestWrapper类型
     * @param response HTTP响应对象
     * @return 认证成功的用户信息（可能是HMAC客户端或切换后的用户）
     * @throws AuthenticationException 如果认证失败
     * @throws BadCredentialsException 如果签名验证失败或请求格式错误
     */
    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        try {
            if (request instanceof HmacRequestWrapper requestWrapper) {
                HmacAuthorization authorization = HmacUtils.getHmacAuthorization(requestWrapper);
                if (authorization == null) {
                    throw new BadCredentialsException("Password does not match stored value");
                }
                String appid = authorization.getAppid();
                String digest = authorization.getDigest();
                String timestamp = authorization.getTimestamp();
                String salt = HmacUtils.getHmacSaltValue(requestWrapper, appid, timestamp);
                LoginUser hmacClient = retrieveUser(appid, requestWrapper.getRemoteAddr());
                String encodedPassword = passwordEncoder.encode(hmacClient.getCredentials(), salt);
                if (!passwordEncoder.matches(digest, encodedPassword)) {
                    log.error("actual: {}, expected: {}, salt: {}", digest, encodedPassword, salt);
                    throw new BadCredentialsException("Password does not match stored value");
                }

                String runUserType = requestWrapper.getParameter(REQUEST_HMAC_TYPE);
                if (runUserType == null) {
                    runUserType = StpUtil.getLoginType();
                }

                boolean containsLoginType = StpLogicUtils.containsLoginType(runUserType);
                if (!containsLoginType) {
                    throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
                }

                String runUserId = requestWrapper.getParameter(REQUEST_HMAC_RUN_USER);
                if (runUserId != null) {
                    hmacClient = hmacClientService.loginByUsername(runUserId, runUserType);
                    log.debug("hmac user {} changed to user: {}", hmacClient.getName(), runUserId);
                }

                requestWrapper.setAttribute("loginType", runUserType);
                requestWrapper.setAttribute("loginDevice", "default");
                return hmacClient;
            } else {
                throw new BadCredentialsException("此请求不支持 Hmac 认证失败！");
            }
        } catch (Exception failed) {
            throw new AuthenticationException("Hmac 认证失败！", failed);
        }
    }

    /**
     * 包装HTTP请求
     *
     * <p>将标准的HttpServletRequest包装为HmacRequestWrapper，
     * 以便支持HMAC认证所需的特殊功能，如请求体缓存、参数解析等。</p>
     *
     * <p><strong>包装功能：</strong></p>
     * <ul>
     *   <li>缓存请求体内容，支持多次读取</li>
     *   <li>提供HMAC签名计算所需的数据访问</li>
     *   <li>支持参数和属性的设置与获取</li>
     * </ul>
     *
     * @param request 原始HTTP请求对象
     * @return 包装后的HMAC请求对象
     * @throws IOException 如果读取请求体失败
     */
    @Override
    protected HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        return new HmacRequestWrapper(request);
    }

    /**
     * 判断请求是否不需要HMAC认证
     *
     * <p>通过检查请求特征来判断是否为HMAC请求。如果不是HMAC请求，
     * 则跳过此过滤器的处理。</p>
     *
     * <p><strong>判断依据：</strong></p>
     * <ul>
     *   <li>检查Authorization头部是否包含HMAC标识</li>
     *   <li>检查请求格式是否符合HMAC规范</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return true表示不需要HMAC认证，false表示需要HMAC认证
     */
    @Override
    protected boolean nonRequiresAuthentication(HttpServletRequest request) {
        return WebHttpUtils.isNotHmacRequest(request);
    }

    /**
     * 属性设置完成后的初始化
     *
     * <p>在所有属性设置完成后进行必要的初始化工作。
     * 调用父类的初始化方法以确保过滤器正确配置。</p>
     *
     * @throws ServletException 如果初始化失败
     */
    @Override
    public void afterPropertiesSet() throws ServletException {
        super.afterPropertiesSet();
    }
}
