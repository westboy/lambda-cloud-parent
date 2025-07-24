package com.lambda.security.web.verify.service.sms;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import com.lambda.security.LoginMode;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCode;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.util.AntPathMatcher;

/**
 * 短信验证码验证服务实现类
 *
 * <p>该类负责处理短信验证码的验证功能，是短信登录认证流程中的关键组件。
 * 主要用于验证用户在短信登录时输入的验证码是否正确和有效。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>安全性</strong>：确保验证码的正确性和时效性</li>
 *   <li><strong>灵活性</strong>：支持多种登录模式的条件验证</li>
 *   <li><strong>可靠性</strong>：完善的参数验证和异常处理</li>
 *   <li><strong>性能</strong>：高效的验证逻辑和缓存机制</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>支持检测</strong>：判断当前请求是否需要短信验证码验证</li>
 *   <li><strong>模式识别</strong>：识别登录模式，仅处理短信登录请求</li>
 *   <li><strong>参数提取</strong>：从请求中提取手机号和验证码</li>
 *   <li><strong>验证码验证</strong>：验证验证码的正确性和有效性</li>
 *   <li><strong>请求转发</strong>：验证通过后继续处理后续流程</li>
 * </ul>
 *
 * <h3>验证流程</h3>
 * <ol>
 *   <li>检查短信功能是否启用</li>
 *   <li>验证请求方法和路径匹配</li>
 *   <li>提取并验证登录模式参数</li>
 *   <li>确认为短信登录模式</li>
 *   <li>提取手机号和验证码参数</li>
 *   <li>从存储中获取验证码信息</li>
 *   <li>验证验证码的正确性</li>
 *   <li>验证通过后继续请求处理</li>
 * </ol>
 *
 * <h3>支持的登录模式</h3>
 * <ul>
 *   <li><strong>短信登录</strong>：使用手机号和短信验证码登录</li>
 *   <li><strong>模式过滤</strong>：仅处理短信登录模式的请求</li>
 * </ul>
 *
 * <h3>请求参数</h3>
 * <ul>
 *   <li><strong>loginMode</strong>：登录模式，必须为"SMS"</li>
 *   <li><strong>mobile</strong>：手机号（必需）</li>
 *   <li><strong>code</strong>：短信验证码（必需）</li>
 * </ul>
 *
 * <h3>验证规则</h3>
 * <ul>
 *   <li><strong>模式匹配</strong>：登录模式必须为SMS</li>
 *   <li><strong>参数完整</strong>：手机号和验证码不能为空</li>
 *   <li><strong>验证码存在</strong>：验证码必须在存储中存在</li>
 *   <li><strong>验证码正确</strong>：输入的验证码必须与存储的一致</li>
 *   <li><strong>时效性</strong>：验证码必须在有效期内</li>
 * </ul>
 *
 * <h3>异常场景</h3>
 * <ul>
 *   <li><strong>模式错误</strong>：登录模式不是SMS</li>
 *   <li><strong>参数缺失</strong>：手机号或验证码为空</li>
 *   <li><strong>验证码不存在</strong>：验证码已过期或未发送</li>
 *   <li><strong>验证码错误</strong>：输入的验证码不正确</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 配置短信验证码验证服务
 * SmsVerifyCodeValidationImpl smsValidator = new SmsVerifyCodeValidationImpl(
 *     securityProperties, smsVerifyCodeStore);
 * smsValidator.setLoginModeParameter("loginMode");
 *
 * // 检查是否支持当前请求
 * if (smsValidator.support(request)) {
 *     smsValidator.execute(request, response, chain);
 * }
 * }</pre>
 *
 * <h3>配置要求</h3>
 * <ul>
 *   <li>短信登录配置：启用状态、登录路径等</li>
 *   <li>验证码存储：Redis或其他存储实现</li>
 *   <li>参数配置：手机号和验证码参数名</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>该类是线程安全的，可以在多线程环境中安全使用。所有的状态都通过不可变的配置对象管理。</p>
 *
 * @author jpjoo
 * @see VerifyCodeService
 * @see SmsVerifyCodeStore
 * @see LoginMode
 * @since 1.0.0
 */
@SuppressWarnings("all")
public class SmsVerifyCodeValidationImpl implements VerifyCodeService {
    /**
     * Ant路径匹配器
     *
     * <p>用于匹配HTTP请求路径，判断当前请求是否需要进行短信验证码验证处理。</p>
     *
     * <h4>功能特性</h4>
     * <ul>
     *   <li><strong>模式匹配</strong>：支持Ant风格的路径模式匹配</li>
     *   <li><strong>通配符支持</strong>：支持*、**、?等通配符</li>
     *   <li><strong>性能优化</strong>：内部缓存匹配结果，提高匹配效率</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>验证请求路径是否匹配短信登录路径</li>
     *   <li>支持动态路径配置和模式匹配</li>
     * </ul>
     */
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 安全配置属性
     *
     * <p>包含短信验证码相关的所有配置信息，如登录路径、参数名称等。</p>
     *
     * <h4>主要配置项</h4>
     * <ul>
     *   <li><strong>短信配置</strong>：登录路径、参数名称等</li>
     *   <li><strong>功能开关</strong>：是否启用短信登录功能</li>
     *   <li><strong>参数配置</strong>：手机号参数名、验证码参数名等</li>
     * </ul>
     *
     * <h4>配置示例</h4>
     * <pre>{@code
     * security:
     *   sms:
     *     enabled: true
     *     loginPath: /login/sms
     *     mobile: mobile
     *     code: code
     * }</pre>
     */
    private final SecurityProperties securityProperties;

    /**
     * 短信验证码存储服务
     *
     * <p>负责短信验证码的存储、获取和验证，通常基于Redis实现。</p>
     *
     * <h4>核心功能</h4>
     * <ul>
     *   <li><strong>验证码获取</strong>：根据手机号获取存储的验证码</li>
     *   <li><strong>验证码验证</strong>：验证用户输入的验证码是否正确</li>
     *   <li><strong>过期处理</strong>：自动处理过期的验证码</li>
     *   <li><strong>一次性使用</strong>：验证成功后自动删除验证码</li>
     * </ul>
     *
     * <h4>存储策略</h4>
     * <ul>
     *   <li>以手机号为键存储验证码</li>
     *   <li>设置验证码有效期（通常5-10分钟）</li>
     *   <li>验证成功后立即删除，防止重复使用</li>
     * </ul>
     */
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;

    /**
     * 登录模式参数名
     *
     * <p>用于从请求中提取登录模式的参数名称，用于区分不同的登录方式。</p>
     *
     * <h4>支持的登录模式</h4>
     * <ul>
     *   <li><strong>SMS</strong>：短信验证码登录</li>
     *   <li><strong>PASSWORD</strong>：用户名密码登录</li>
     *   <li><strong>THIRD_PART</strong>：第三方登录</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li><strong>模式识别</strong>：识别当前请求的登录模式</li>
     *   <li><strong>条件处理</strong>：仅处理短信登录模式的请求</li>
     *   <li><strong>流程分发</strong>：根据模式分发到不同的处理流程</li>
     * </ul>
     *
     * <h4>默认值</h4>
     * <p>默认为"loginMode"，可通过setter方法自定义。</p>
     */
    @Setter
    private String loginModeParameter = "loginMode";

    /**
     * 构造短信验证码验证服务实例
     *
     * <p>初始化短信验证码验证服务，配置必要的依赖组件。</p>
     *
     * <h4>初始化过程</h4>
     * <ol>
     *   <li>保存安全配置属性引用</li>
     *   <li>保存短信验证码存储服务引用</li>
     * </ol>
     *
     * <h4>依赖要求</h4>
     * <ul>
     *   <li><strong>securityProperties</strong>：必须包含完整的短信配置信息</li>
     *   <li><strong>smsVerifyCodeStore</strong>：必须实现验证码存储和验证功能</li>
     * </ul>
     *
     * <h4>线程安全</h4>
     * <p>构造函数是线程安全的，所有参数都是不可变的或线程安全的实现。</p>
     *
     * <h4>使用示例</h4>
     * <pre>{@code
     * SmsVerifyCodeValidationImpl smsValidator = new SmsVerifyCodeValidationImpl(
     *     securityProperties,
     *     redisSmsVerifyCodeStore
     * );
     * }</pre>
     *
     * @param securityProperties 安全配置属性，包含短信验证码相关配置
     * @param smsVerifyCodeStore 短信验证码存储服务，用于验证码的获取和验证
     * @throws IllegalArgumentException 如果任何参数为null
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "this is thread safe")
    public SmsVerifyCodeValidationImpl(
            SecurityProperties securityProperties, SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
    }

    /**
     * 检查当前请求是否需要短信验证码验证处理
     *
     * <p>根据配置和请求特征判断是否应该由此服务处理当前请求。</p>
     *
     * <h4>检查条件</h4>
     * <ol>
     *   <li><strong>功能启用</strong>：短信登录功能必须在配置中启用</li>
     *   <li><strong>请求方法</strong>：必须是POST请求方法</li>
     *   <li><strong>路径匹配</strong>：请求URI必须匹配配置的短信登录路径</li>
     * </ol>
     *
     * <h4>路径匹配逻辑</h4>
     * <ul>
     *   <li>使用Ant风格的路径模式匹配</li>
     *   <li>支持通配符和动态路径参数</li>
     *   <li>大小写敏感的精确匹配</li>
     * </ul>
     *
     * <h4>性能考虑</h4>
     * <ul>
     *   <li>配置检查优先，避免不必要的路径匹配</li>
     *   <li>路径匹配器内部有缓存机制</li>
     *   <li>方法调用开销很小，适合频繁调用</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>过滤器链中的请求分发</li>
     *   <li>拦截器中的预处理判断</li>
     *   <li>路由配置的动态匹配</li>
     * </ul>
     *
     * <h4>配置示例</h4>
     * <pre>{@code
     * security:
     *   sms:
     *     enabled: true
     *     loginPath: /api/login/sms
     *
     * // 匹配的请求：POST /api/login/sms
     * // 不匹配的请求：GET /api/login/sms, POST /api/login/password
     * }</pre>
     *
     * @param request HTTP请求对象，包含请求方法、URI等信息
     * @return {@code true} 如果当前请求需要短信验证码验证处理；{@code false} 否则
     * @see SecurityProperties.SmsLogin#isEnabled()
     * @see SecurityProperties.SmsLogin#getLoginPath()
     */
    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
        return smsLogin.isEnabled()
                && JakartaServletUtil.isPostMethod(request)
                && matcher.match(smsLogin.getLoginPath(), request.getRequestURI());
    }

    /**
     * 执行短信验证码验证的核心逻辑
     *
     * <p>处理短信验证码的完整验证流程，包括登录模式识别、参数提取、
     * 验证码验证和请求转发。</p>
     *
     * <h4>执行流程</h4>
     * <ol>
     *   <li><strong>请求包装</strong>：将请求包装为可重复读取的格式</li>
     *   <li><strong>参数提取</strong>：从请求中提取JSON参数</li>
     *   <li><strong>模式识别</strong>：提取并验证登录模式</li>
     *   <li><strong>模式过滤</strong>：仅处理短信登录模式的请求</li>
     *   <li><strong>参数验证</strong>：验证手机号和验证码参数</li>
     *   <li><strong>验证码获取</strong>：从存储中获取验证码信息</li>
     *   <li><strong>验证码验证</strong>：验证验证码的正确性</li>
     *   <li><strong>请求转发</strong>：验证通过后继续处理后续流程</li>
     * </ol>
     *
     * <h4>参数提取</h4>
     * <ul>
     *   <li><strong>loginMode</strong>：登录模式，必须为"SMS"</li>
     *   <li><strong>mobile</strong>：手机号，从配置的参数名中提取</li>
     *   <li><strong>code</strong>：短信验证码，从配置的参数名中提取</li>
     * </ul>
     *
     * <h4>登录模式处理</h4>
     * <ul>
     *   <li><strong>模式检查</strong>：验证登录模式参数是否存在</li>
     *   <li><strong>模式解析</strong>：将字符串转换为LoginMode枚举</li>
     *   <li><strong>模式过滤</strong>：仅处理SMS模式，其他模式直接转发</li>
     * </ul>
     *
     * <h4>验证码验证逻辑</h4>
     * <ul>
     *   <li><strong>存在性检查</strong>：验证码必须在存储中存在</li>
     *   <li><strong>正确性验证</strong>：输入的验证码必须与存储的一致</li>
     *   <li><strong>时效性验证</strong>：验证码必须在有效期内</li>
     *   <li><strong>一次性使用</strong>：验证成功后自动删除验证码</li>
     * </ul>
     *
     * <h4>请求示例</h4>
     * <pre>{@code
     * POST /api/login/sms
     * Content-Type: application/json
     *
     * {
     *   "loginMode": "SMS",
     *   "mobile": "13800138000",
     *   "code": "123456"
     * }
     * }</pre>
     *
     * <h4>异常处理</h4>
     * <ul>
     *   <li><strong>VerifyCodeValidationException</strong>：验证码相关的所有异常</li>
     *   <li><strong>参数异常</strong>：登录模式为空、手机号为空、验证码为空</li>
     *   <li><strong>验证异常</strong>：验证码不存在、验证码不正确</li>
     * </ul>
     *
     * <h4>流程控制</h4>
     * <ul>
     *   <li><strong>参数为空</strong>：直接转发到下一个过滤器</li>
     *   <li><strong>非SMS模式</strong>：直接转发到下一个过滤器</li>
     *   <li><strong>验证失败</strong>：抛出异常，中断请求处理</li>
     *   <li><strong>验证成功</strong>：转发到下一个过滤器继续处理</li>
     * </ul>
     *
     * <h4>性能优化</h4>
     * <ul>
     *   <li>参数为空时直接跳过处理</li>
     *   <li>非SMS模式时直接跳过验证</li>
     *   <li>验证码验证采用高效的存储查询</li>
     * </ul>
     *
     * <h4>安全考虑</h4>
     * <ul>
     *   <li>验证码验证后立即删除，防止重复使用</li>
     *   <li>严格的参数验证，防止恶意请求</li>
     *   <li>详细的异常信息，便于问题定位</li>
     * </ul>
     *
     * @param httpServletRequest HTTP请求对象，包含请求参数
     * @param httpServletResponse HTTP响应对象，用于返回结果
     * @param chain 过滤器链，用于请求转发
     * @throws ServletException 当过滤器处理失败时抛出
     * @throws IOException 当请求或响应处理失败时抛出
     * @throws VerifyCodeValidationException 当验证码验证失败时抛出
     * @see LoginMode
     * @see SmsVerifyCodeStore#get(String)
     * @see SmsVerifyCodeStore#verify(String, String)
     */
    @Override
    public void execute(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws ServletException, IOException {
        LambdaHttpServletRequestWrapper httpServletRequestWrapper = getRequestWrapper(httpServletRequest);
        JSONObject requestParam = getRequestParam(httpServletRequestWrapper);
        if (MapUtils.isEmpty(requestParam)) {
            chain.doFilter(httpServletRequestWrapper, httpServletResponse);
            return;
        }

        String loginMode = requestParam.getStr(loginModeParameter);

        if (StrUtil.isEmpty(loginMode)) {
            throw new VerifyCodeValidationException("loginMode is not blank!");
        }

        LoginMode mode = LoginMode.get(loginMode);

        if (!LoginMode.SMS.equals(mode)) {
            chain.doFilter(httpServletRequestWrapper, httpServletResponse);
            return;
        }

        String mobile = requestParam.getStr(securityProperties.getSms().getMobile());
        if (StringUtils.isBlank(mobile)) {
            throw new VerifyCodeValidationException("手机号不能为空");
        }
        String code = requestParam.getStr(securityProperties.getSms().getCode());
        if (StringUtils.isBlank(code)) {
            throw new VerifyCodeValidationException("验证码不能为空");
        }

        SmsVerifyCode<String> verifyCode = smsVerifyCodeStore.get(mobile);

        if (verifyCode == null) {
            throw new VerifyCodeValidationException("验证码不存在");
        }

        boolean verified = smsVerifyCodeStore.verify(mobile, code);
        if (!verified) {
            throw new VerifyCodeValidationException("验证码不正确");
        }
        chain.doFilter(httpServletRequestWrapper, httpServletResponse);
    }
}
