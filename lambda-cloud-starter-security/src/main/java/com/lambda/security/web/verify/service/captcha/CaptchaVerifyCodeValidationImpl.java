package com.lambda.security.web.verify.service.captcha;

import cn.hutool.core.util.StrUtil;
import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import com.lambda.security.LoginMode;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.collections4.MapUtils;
import org.springframework.util.AntPathMatcher;

/**
 * 图形验证码校验服务实现类
 *
 * <p>本类负责在用户登录过程中验证图形验证码的正确性，确保登录请求的安全性。
 * 通过与{@link CaptchaVerifyCodeGenerateImpl}配合使用，形成完整的图形验证码生成和验证流程。
 *
 * <p>设计目标：
 * <ul>
 *   <li>安全验证：防止暴力破解和机器人攻击</li>
 *   <li>灵活配置：支持通过配置启用/禁用验证码功能</li>
 *   <li>模式识别：仅对密码登录模式进行验证码校验</li>
 *   <li>路径匹配：精确匹配登录处理URL</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>支持检测：判断当前请求是否需要验证码校验</li>
 *   <li>参数提取：从请求中提取验证码和令牌</li>
 *   <li>验证执行：调用CaptchaStore进行验证码校验</li>
 *   <li>异常处理：验证失败时抛出相应异常</li>
 * </ul>
 *
 * <p>验证流程：
 * <ol>
 *   <li>检查是否启用验证码功能</li>
 *   <li>验证请求方法为POST</li>
 *   <li>匹配登录处理URL路径</li>
 *   <li>提取请求参数（表单和JSON）</li>
 *   <li>检查登录模式是否为密码模式</li>
 *   <li>提取验证码和令牌参数</li>
 *   <li>调用CaptchaStore进行验证</li>
 *   <li>验证通过则继续过滤链</li>
 * </ol>
 *
 * <p>支持的登录模式：
 * <ul>
 *   <li>密码模式（PWD）：需要验证码校验</li>
 *   <li>其他模式：跳过验证码校验</li>
 * </ul>
 *
 * <p>请求参数：
 * <ul>
 *   <li>loginMode：登录模式标识</li>
 *   <li>__token：验证码令牌（由生成服务提供）</li>
 *   <li>verifyCode：用户输入的验证码答案</li>
 * </ul>
 *
 * <p>异常情况：
 * <ul>
 *   <li>登录模式为空：抛出VerifyCodeValidationException</li>
 *   <li>验证码为空：抛出VerifyCodeValidationException</li>
 *   <li>令牌为空：抛出VerifyCodeValidationException</li>
 *   <li>验证码错误：抛出VerifyCodeValidationException</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 配置验证码校验服务
 * @Bean
 * public CaptchaVerifyCodeValidationImpl captchaValidator(
 *         SecurityProperties properties,
 *         CaptchaStore store) {
 *     return new CaptchaVerifyCodeValidationImpl(properties, store);
 * }
 *
 * // 登录请求示例
 * POST /login
 * Content-Type: application/json
 *
 * {
 *   "username": "user",
 *   "password": "pass",
 *   "loginMode": "PWD",
 *   "__token": "abc123",
 *   "verifyCode": "42"
 * }
 * }</pre>
 *
 * <p>配置要求：
 * <ul>
 *   <li>securityProperties.form.enableVerify：启用验证码功能</li>
 *   <li>securityProperties.form.loginProcessingUrl：登录处理URL</li>
 *   <li>captchaStore：验证码存储实现</li>
 * </ul>
 *
 * <p>线程安全性：
 * <ul>
 *   <li>无状态设计：所有字段都是final和不可变的</li>
 *   <li>并发安全：可以安全地在多线程环境中使用</li>
 *   <li>请求隔离：每个请求的处理相互独立</li>
 * </ul>
 *
 * @author jpjoo
 * @see CaptchaVerifyCodeGenerateImpl
 * @see CaptchaStore
 * @see VerifyCodeService
 * @see LoginMode
 */
@SuppressWarnings("all")
public class CaptchaVerifyCodeValidationImpl implements VerifyCodeService {

    /**
     * Ant路径匹配器
     *
     * <p>用于匹配登录处理URL，支持Ant风格的路径模式匹配。
     * 通过精确的路径匹配确保验证码校验只在登录请求时触发。
     *
     * <p>匹配特性：
     * <ul>
     *   <li>精确匹配：完全匹配配置的登录URL</li>
     *   <li>模式支持：支持*、**、?等通配符</li>
     *   <li>性能优化：使用Spring的高效匹配算法</li>
     * </ul>
     */
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 安全配置属性
     *
     * <p>包含验证码相关的所有配置信息，如是否启用验证码、登录处理URL等。
     * 通过配置驱动的方式实现验证码功能的灵活控制。
     *
     * <p>相关配置：
     * <ul>
     *   <li>form.enableVerify：是否启用验证码功能</li>
     *   <li>form.loginProcessingUrl：登录处理URL路径</li>
     * </ul>
     */
    private final SecurityProperties securityProperties;

    /**
     * 验证码存储服务
     *
     * <p>负责验证码的存储和校验，通常使用Redis等缓存实现。
     * 提供验证码的生命周期管理和一次性使用特性。
     *
     * <p>主要功能：
     * <ul>
     *   <li>验证码校验：根据令牌和答案进行验证</li>
     *   <li>自动清理：验证成功后自动删除验证码</li>
     *   <li>过期处理：自动处理过期的验证码</li>
     * </ul>
     */
    private final CaptchaStore captchaStore;

    /**
     * 构造图形验证码校验服务实例
     *
     * <p>初始化验证码校验服务所需的依赖组件，包括安全配置和验证码存储服务。
     * 通过依赖注入的方式获取配置信息和存储实现，确保服务的可配置性和可扩展性。
     *
     * <p>初始化过程：
     * <ol>
     *   <li>保存安全配置属性引用</li>
     *   <li>保存验证码存储服务引用</li>
     *   <li>确保所有依赖都已正确注入</li>
     * </ol>
     *
     * <p>依赖要求：
     * <ul>
     *   <li>securityProperties：不能为null，包含验证码相关配置</li>
     *   <li>captchaStore：不能为null，提供验证码存储和校验功能</li>
     * </ul>
     *
     * <p>线程安全性：
     * <ul>
     *   <li>不可变设计：所有字段都是final的</li>
     *   <li>无状态服务：不维护任何可变状态</li>
     *   <li>并发安全：可以安全地在多线程环境中使用</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * @Configuration
     * public class SecurityConfig {
     *
     *     @Bean
     *     public CaptchaVerifyCodeValidationImpl captchaValidator(
     *             SecurityProperties securityProperties,
     *             CaptchaStore captchaStore) {
     *         return new CaptchaVerifyCodeValidationImpl(
     *             securityProperties,
     *             captchaStore
     *         );
     *     }
     * }
     * }</pre>
     *
     * @param securityProperties 安全配置属性，包含验证码相关配置信息
     * @param captchaStore 验证码存储服务，用于验证码的存储和校验
     * @throws NullPointerException 如果任何参数为null
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "VerifyCodeFilter is thread safe")
    public CaptchaVerifyCodeValidationImpl(SecurityProperties securityProperties, CaptchaStore captchaStore) {
        this.securityProperties = securityProperties;
        this.captchaStore = captchaStore;
    }

    /**
     * 检查当前请求是否需要进行验证码校验
     *
     * <p>通过多个条件的组合判断来确定是否需要对当前请求进行验证码校验。
     * 只有同时满足所有条件时才会进行验证码校验，确保校验的精确性和性能。
     *
     * <p>判断条件：
     * <ol>
     *   <li>验证码功能已启用：通过配置securityProperties.form.enableVerify控制</li>
     *   <li>请求方法为POST：只对POST请求进行验证码校验</li>
     *   <li>URL路径匹配：请求URI匹配配置的登录处理URL</li>
     * </ol>
     *
     * <p>路径匹配逻辑：
     * <ul>
     *   <li>使用AntPathMatcher进行路径匹配</li>
     *   <li>支持Ant风格的通配符模式</li>
     *   <li>精确匹配登录处理URL</li>
     * </ul>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>短路求值：条件按照执行成本从低到高排列</li>
     *   <li>配置缓存：配置值在启动时加载，运行时直接使用</li>
     *   <li>高效匹配：使用Spring优化的路径匹配算法</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>登录请求：POST /login</li>
     *   <li>表单提交：Content-Type为application/x-www-form-urlencoded</li>
     *   <li>Ajax请求：Content-Type为application/json</li>
     * </ul>
     *
     * <p>配置示例：
     * <pre>{@code
     * # application.yml
     * lambda:
     *   security:
     *     form:
     *       enable-verify: true
     *       login-processing-url: /api/login
     * }</pre>
     *
     * @param request HTTP请求对象，用于获取请求方法和URI
     * @return true表示需要进行验证码校验，false表示跳过校验
     */
    @Override
    public boolean support(HttpServletRequest request) {
        boolean captchaEnabled = securityProperties.getForm().isEnableVerify();
        boolean isPostMethod = JakartaServletUtil.isPostMethod(request);
        return captchaEnabled
                && isPostMethod
                && matcher.match(securityProperties.getForm().getLoginProcessingUrl(), request.getRequestURI());
    }

    /**
     * 执行验证码校验逻辑
     *
     * <p>这是验证码校验的核心方法，负责从请求中提取参数、验证登录模式、
     * 校验验证码的正确性，并在验证通过后继续过滤链的执行。
     *
     * <p>执行流程：
     * <ol>
     *   <li>包装HTTP请求以支持多次读取请求体</li>
     *   <li>提取表单参数和JSON参数并合并</li>
     *   <li>检查请求参数是否为空</li>
     *   <li>验证登录模式是否为密码模式</li>
     *   <li>提取验证码和令牌参数</li>
     *   <li>校验参数的有效性</li>
     *   <li>调用CaptchaStore进行验证码校验</li>
     *   <li>验证通过后继续过滤链</li>
     * </ol>
     *
     * <p>参数提取逻辑：
     * <ul>
     *   <li>表单参数：从application/x-www-form-urlencoded中提取</li>
     *   <li>JSON参数：从application/json请求体中提取</li>
     *   <li>参数合并：JSON参数会覆盖同名的表单参数</li>
     * </ul>
     *
     * <p>登录模式处理：
     * <ul>
     *   <li>密码模式（PWD）：需要验证码校验</li>
     *   <li>其他模式：跳过验证码校验，直接继续过滤链</li>
     * </ul>
     *
     * <p>验证码校验：
     * <ul>
     *   <li>令牌验证：检查__token参数是否存在</li>
     *   <li>答案验证：检查verifyCode参数是否存在</li>
     *   <li>存储校验：调用CaptchaStore.validate进行最终校验</li>
     * </ul>
     *
     * <p>异常处理：
     * <ul>
     *   <li>登录模式为空：抛出VerifyCodeValidationException</li>
     *   <li>验证码为空：抛出VerifyCodeValidationException</li>
     *   <li>令牌为空：抛出VerifyCodeValidationException</li>
     *   <li>验证码错误：抛出VerifyCodeValidationException</li>
     * </ul>
     *
     * <p>请求示例：
     * <pre>{@code
     * // 表单请求
     * POST /login
     * Content-Type: application/x-www-form-urlencoded
     *
     * username=user&password=pass&loginMode=PWD&__token=abc123&verifyCode=42
     *
     * // JSON请求
     * POST /login
     * Content-Type: application/json
     *
     * {
     *   "username": "user",
     *   "password": "pass",
     *   "loginMode": "PWD",
     *   "__token": "abc123",
     *   "verifyCode": "42"
     * }
     * }</pre>
     *
     * <p>性能优化：
     * <ul>
     *   <li>早期返回：非密码模式直接跳过校验</li>
     *   <li>参数缓存：避免重复解析请求参数</li>
     *   <li>异常快速失败：参数验证失败立即抛出异常</li>
     * </ul>
     *
     * @param httpServletRequest HTTP请求对象，包含登录参数和验证码信息
     * @param httpServletResponse HTTP响应对象，用于设置响应头和状态
     * @param chain 过滤器链，验证通过后继续执行
     * @throws ServletException 过滤器执行过程中的异常
     * @throws IOException 读取请求或写入响应时的IO异常
     * @throws VerifyCodeValidationException 验证码校验失败时抛出
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

        String loginMode = requestParam.getStr("loginMode");
        if (StrUtil.isEmpty(loginMode)) {
            throw new VerifyCodeValidationException("登录模式不能为空!");
        }

        LoginMode mode = LoginMode.get(loginMode);

        if (!LoginMode.PWD.equals(mode)) {
            chain.doFilter(httpServletRequestWrapper, httpServletResponse);
            return;
        }

        String verifyCode = obtainVerifyCode(requestParam);
        String verifyToken = obtainVerifyToken(requestParam);

        if (StrUtil.isBlank(verifyCode)) {
            throw new VerifyCodeValidationException("验证码不能为空!");
        }

        if (StrUtil.isBlank(verifyToken)) {
            throw new VerifyCodeValidationException("__TOKEN不能为空!");
        }

        boolean verified = captchaStore.validate(verifyToken, verifyCode);
        if (!verified) {
            throw new VerifyCodeValidationException("验证码不正确!");
        }

        chain.doFilter(httpServletRequestWrapper, httpServletResponse);
    }

    /**
     * 从请求参数中提取验证码令牌
     *
     * <p>从合并后的请求参数Map中提取验证码令牌（__token），
     * 该令牌是验证码生成时创建的唯一标识符，用于在存储中定位对应的验证码答案。
     *
     * <p>提取逻辑：
     * <ul>
     *   <li>参数名称：使用CaptchaVerifyCodeGenerateImpl.TOKEN_KEY常量</li>
     *   <li>默认值：如果参数不存在，返回空字符串</li>
     *   <li>类型转换：将Object类型转换为String类型</li>
     * </ul>
     *
     * <p>令牌特性：
     * <ul>
     *   <li>唯一性：每个验证码都有唯一的令牌标识</li>
     *   <li>时效性：令牌有过期时间，过期后无法使用</li>
     *   <li>一次性：验证成功后令牌会被删除</li>
     * </ul>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>防伪造：令牌由服务端生成，客户端无法伪造</li>
     *   <li>防重放：令牌使用后即失效，防止重放攻击</li>
     *   <li>防猜测：使用UUID生成，具有足够的随机性</li>
     * </ul>
     *
     * @param map 请求参数Map，包含表单和JSON参数
     * @return 验证码令牌字符串，如果不存在则返回空字符串
     */
    private String obtainVerifyToken(Map<String, Object> map) {
        Object token = map.getOrDefault(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY, "");
        return String.valueOf(token);
    }

    /**
     * 从请求参数中提取用户输入的验证码答案
     *
     * <p>从合并后的请求参数Map中提取用户输入的验证码答案，
     * 该答案将与存储中的正确答案进行比较以完成验证。
     *
     * <p>提取逻辑：
     * <ul>
     *   <li>参数名称：使用CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER常量</li>
     *   <li>默认值：如果参数不存在，返回空字符串</li>
     *   <li>类型转换：将Object类型转换为String类型</li>
     * </ul>
     *
     * <p>答案格式：
     * <ul>
     *   <li>数字答案：数学运算的计算结果，如"42"</li>
     *   <li>字符串格式：统一转换为字符串进行比较</li>
     *   <li>大小写敏感：字符串比较区分大小写</li>
     * </ul>
     *
     * <p>验证特性：
     * <ul>
     *   <li>精确匹配：答案必须与存储的正确答案完全一致</li>
     *   <li>类型容错：支持数字和字符串类型的自动转换</li>
     *   <li>空值处理：空值或null会被转换为空字符串</li>
     * </ul>
     *
     * <p>使用示例：
     * <ul>
     *   <li>数学题"3+5"的答案：用户输入"8"</li>
     *   <li>数学题"7*6"的答案：用户输入"42"</li>
     *   <li>数学题"9-4"的答案：用户输入"5"</li>
     * </ul>
     *
     * @param map 请求参数Map，包含表单和JSON参数
     * @return 用户输入的验证码答案字符串，如果不存在则返回空字符串
     */
    private String obtainVerifyCode(Map<String, Object> map) {
        Object verify = map.getOrDefault(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER, "");
        return String.valueOf(verify);
    }
}
