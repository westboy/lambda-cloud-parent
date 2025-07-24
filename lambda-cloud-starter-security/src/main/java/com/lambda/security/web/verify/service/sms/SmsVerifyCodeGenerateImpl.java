package com.lambda.security.web.verify.service.sms;

import cn.hutool.extra.servlet.JakartaServletUtil;
import cn.hutool.json.JSONObject;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.exception.model.ErrorModel;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.cloud.sms.SmsMessageSender;
import com.lambda.cloud.sms.model.SmsSendResult;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.VerifyCodeValidationException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.verify.service.VerifyCodeService;
import com.lambda.security.web.verify.service.captcha.CaptchaVerifyCodeGenerateImpl;
import com.lambda.security.web.verify.service.captcha.store.CaptchaStore;
import com.lambda.security.web.verify.service.sms.model.SmsVerifyCodeResponse;
import com.lambda.security.web.verify.service.sms.store.SmsVerifyCodeStore;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.AntPathMatcher;

/**
 * 短信验证码生成服务实现类
 *
 * <p>该类负责处理短信验证码的生成和发送功能，是安全认证体系中的重要组件。
 * 主要用于用户登录、注册、密码重置等场景下的身份验证。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>安全性</strong>：集成图形验证码防刷机制，防止恶意请求</li>
 *   <li><strong>灵活性</strong>：支持多种登录类型和配置参数</li>
 *   <li><strong>可靠性</strong>：完善的异常处理和错误反馈机制</li>
 *   <li><strong>可配置性</strong>：通过配置文件控制各种行为参数</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>支持检测</strong>：判断当前请求是否需要短信验证码生成</li>
 *   <li><strong>用户验证</strong>：验证手机号对应的用户信息和状态</li>
 *   <li><strong>防刷控制</strong>：检查短信发送频率限制</li>
 *   <li><strong>图形验证</strong>：可选的图形验证码二次验证</li>
 *   <li><strong>短信发送</strong>：生成验证码并通过短信服务发送</li>
 *   <li><strong>响应处理</strong>：返回标准化的JSON响应</li>
 * </ul>
 *
 * <h3>验证流程</h3>
 * <ol>
 *   <li>检查短信功能是否启用</li>
 *   <li>验证请求方法和路径匹配</li>
 *   <li>提取并验证手机号参数</li>
 *   <li>验证登录类型有效性</li>
 *   <li>查询用户信息并检查账号状态</li>
 *   <li>检查短信发送频率限制</li>
 *   <li>可选：验证图形验证码</li>
 *   <li>生成并发送短信验证码</li>
 *   <li>返回发送结果</li>
 * </ol>
 *
 * <h3>支持的登录模式</h3>
 * <ul>
 *   <li><strong>手机号登录</strong>：通过手机号获取用户信息</li>
 *   <li><strong>多类型登录</strong>：支持不同的登录类型标识</li>
 * </ul>
 *
 * <h3>请求参数</h3>
 * <ul>
 *   <li><strong>mobile</strong>：手机号（必需）</li>
 *   <li><strong>loginType</strong>：登录类型（必需）</li>
 *   <li><strong>__TOKEN</strong>：图形验证码令牌（可选，取决于配置）</li>
 *   <li><strong>verifyCode</strong>：图形验证码（可选，取决于配置）</li>
 * </ul>
 *
 * <h3>异常场景</h3>
 * <ul>
 *   <li><strong>参数缺失</strong>：手机号或登录类型为空</li>
 *   <li><strong>用户不存在</strong>：手机号未注册</li>
 *   <li><strong>账号异常</strong>：账号过期、锁定等状态</li>
 *   <li><strong>频率限制</strong>：短信发送过于频繁</li>
 *   <li><strong>验证失败</strong>：图形验证码错误</li>
 *   <li><strong>发送失败</strong>：短信服务异常</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 配置短信验证码生成服务
 * SmsVerifyCodeGenerateImpl smsGenerator = new SmsVerifyCodeGenerateImpl(
 *     securityProperties, objectMapper, smsVerifyCodeStore);
 * smsGenerator.setCaptchaStore(captchaStore);
 * smsGenerator.setUserDetailService(userDetailService);
 * smsGenerator.setSmsMessageSender(smsMessageSender);
 *
 * // 检查是否支持当前请求
 * if (smsGenerator.support(request)) {
 *     smsGenerator.execute(request, response, chain);
 * }
 * }</pre>
 *
 * <h3>配置要求</h3>
 * <ul>
 *   <li>短信服务配置：发送接口、模板等</li>
 *   <li>验证码存储：Redis或其他存储实现</li>
 *   <li>用户服务：手机号查询接口</li>
 *   <li>图形验证码：可选的二次验证</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>该类是线程安全的，可以在多线程环境中安全使用。所有的状态都通过不可变的配置对象管理。</p>
 *
 * @author jpjoo
 * @see VerifyCodeService
 * @see SmsVerifyCodeStore
 * @see SmsMessageSender
 * @see UserDetailService
 * @since 1.0.0
 */
@SuppressFBWarnings(value = "REC_CATCH_EXCEPTION")
@Slf4j
public class SmsVerifyCodeGenerateImpl implements VerifyCodeService {
    /**
     * Ant路径匹配器
     *
     * <p>用于匹配HTTP请求路径，判断当前请求是否需要进行短信验证码生成处理。</p>
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
     *   <li>验证请求路径是否匹配短信验证码生成路径</li>
     *   <li>支持动态路径配置和模式匹配</li>
     * </ul>
     */
    private final AntPathMatcher matcher = new AntPathMatcher();

    /**
     * 安全配置属性
     *
     * <p>包含短信验证码相关的所有配置信息，如发送路径、参数名称、有效期等。</p>
     *
     * <h4>主要配置项</h4>
     * <ul>
     *   <li><strong>短信配置</strong>：发送路径、重发间隔、有效时间等</li>
     *   <li><strong>参数配置</strong>：手机号参数名、验证码参数名等</li>
     *   <li><strong>功能开关</strong>：是否启用短信验证、是否启用图形验证等</li>
     * </ul>
     *
     * <h4>配置示例</h4>
     * <pre>{@code
     * security:
     *   sms:
     *     enabled: true
     *     verifyPath: /sms/send
     *     mobile: mobile
     *     resendSeconds: 60
     *     validMinutes: 5
     * }</pre>
     */
    private final SecurityProperties securityProperties;

    /**
     * JSON对象映射器
     *
     * <p>用于处理HTTP请求和响应的JSON数据序列化和反序列化。</p>
     *
     * <h4>主要用途</h4>
     * <ul>
     *   <li><strong>请求解析</strong>：解析POST请求体中的JSON参数</li>
     *   <li><strong>响应生成</strong>：将响应对象序列化为JSON格式</li>
     *   <li><strong>错误处理</strong>：生成标准化的错误响应JSON</li>
     * </ul>
     *
     * <h4>配置特性</h4>
     * <ul>
     *   <li>支持自定义序列化配置</li>
     *   <li>处理日期、枚举等特殊类型</li>
     *   <li>忽略null值和未知属性</li>
     * </ul>
     */
    private final ObjectMapper objectMapper;

    /**
     * 短信验证码存储服务
     *
     * <p>负责短信验证码的生成、存储、验证和管理，通常基于Redis实现。</p>
     *
     * <h4>核心功能</h4>
     * <ul>
     *   <li><strong>验证码生成</strong>：生成指定长度的数字验证码</li>
     *   <li><strong>存储管理</strong>：将验证码存储到缓存中，设置过期时间</li>
     *   <li><strong>重发控制</strong>：检查验证码重发时间间隔</li>
     *   <li><strong>验证功能</strong>：验证用户输入的验证码是否正确</li>
     * </ul>
     *
     * <h4>存储策略</h4>
     * <ul>
     *   <li>以手机号为键存储验证码</li>
     *   <li>设置验证码有效期（通常5-10分钟）</li>
     *   <li>记录发送时间，控制重发频率</li>
     * </ul>
     */
    private final SmsVerifyCodeStore<String> smsVerifyCodeStore;

    /**
     * 短信登录配置
     *
     * <p>从安全配置中提取的短信相关配置信息，用于快速访问短信功能的各项参数。</p>
     *
     * <h4>配置内容</h4>
     * <ul>
     *   <li><strong>功能开关</strong>：是否启用短信登录功能</li>
     *   <li><strong>路径配置</strong>：短信发送和验证的URL路径</li>
     *   <li><strong>时间配置</strong>：验证码有效期、重发间隔等</li>
     *   <li><strong>参数配置</strong>：手机号参数名等</li>
     * </ul>
     */
    private final SecurityProperties.SmsLogin smsLogin;

    /**
     * 登录类型参数名
     *
     * <p>用于从请求中提取登录类型的参数名称，支持多种登录方式的区分。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li><strong>多端登录</strong>：区分PC端、移动端、小程序等</li>
     *   <li><strong>业务区分</strong>：区分用户登录、管理员登录等</li>
     *   <li><strong>权限控制</strong>：根据登录类型分配不同权限</li>
     * </ul>
     *
     * <h4>默认值</h4>
     * <p>默认为"loginType"，可通过setter方法自定义。</p>
     */
    @Setter
    private String loginTypeParameter = "loginType";

    /**
     * 图形验证码存储服务
     *
     * <p>可选的图形验证码存储和验证服务，用于在短信发送前进行二次验证，防止恶意刷短信。</p>
     *
     * <h4>防刷机制</h4>
     * <ul>
     *   <li><strong>二次验证</strong>：要求用户先通过图形验证码</li>
     *   <li><strong>频率控制</strong>：限制恶意请求和机器人攻击</li>
     *   <li><strong>安全增强</strong>：提高短信接口的安全性</li>
     * </ul>
     *
     * <h4>配置控制</h4>
     * <p>通过{@code smsLogin.enableVerify}配置是否启用图形验证码验证。</p>
     */
    @Setter
    private CaptchaStore captchaStore;

    /**
     * 用户详情服务
     *
     * <p>用于根据手机号查询用户信息，验证用户身份和账号状态。</p>
     *
     * <h4>主要功能</h4>
     * <ul>
     *   <li><strong>用户查询</strong>：根据手机号和登录类型查询用户</li>
     *   <li><strong>状态检查</strong>：验证账号是否过期、锁定等</li>
     *   <li><strong>权限获取</strong>：获取用户的角色和权限信息</li>
     * </ul>
     *
     * <h4>验证项目</h4>
     * <ul>
     *   <li>用户是否存在</li>
     *   <li>账号是否过期</li>
     *   <li>账号是否被锁定</li>
     *   <li>账号是否启用</li>
     * </ul>
     */
    @Setter
    private UserDetailService userDetailService;

    /**
     * 短信消息发送器
     *
     * <p>负责实际的短信发送功能，通过第三方短信服务提供商发送验证码短信。</p>
     *
     * <h4>核心功能</h4>
     * <ul>
     *   <li><strong>验证码发送</strong>：发送包含验证码的短信</li>
     *   <li><strong>模板管理</strong>：使用预定义的短信模板</li>
     *   <li><strong>结果反馈</strong>：返回发送结果和状态信息</li>
     *   <li><strong>异常处理</strong>：处理发送失败的各种情况</li>
     * </ul>
     *
     * <h4>发送参数</h4>
     * <ul>
     *   <li><strong>手机号</strong>：接收短信的手机号码</li>
     *   <li><strong>验证码</strong>：要发送的验证码内容</li>
     *   <li><strong>有效期</strong>：验证码的有效时间（分钟）</li>
     * </ul>
     *
     * <h4>返回信息</h4>
     * <ul>
     *   <li>发送是否成功</li>
     *   <li>短信服务商返回的消息ID</li>
     *   <li>错误信息（如果发送失败）</li>
     * </ul>
     */
    @Setter
    private SmsMessageSender smsMessageSender;

    /**
     * 构造短信验证码生成服务实例
     *
     * <p>初始化短信验证码生成服务，配置必要的依赖组件和参数。</p>
     *
     * <h4>初始化过程</h4>
     * <ol>
     *   <li>保存安全配置属性引用</li>
     *   <li>保存JSON对象映射器引用</li>
     *   <li>保存短信验证码存储服务引用</li>
     *   <li>提取短信登录相关配置</li>
     * </ol>
     *
     * <h4>依赖要求</h4>
     * <ul>
     *   <li><strong>securityProperties</strong>：必须包含完整的短信配置信息</li>
     *   <li><strong>objectMapper</strong>：必须支持JSON序列化和反序列化</li>
     *   <li><strong>smsVerifyCodeStore</strong>：必须实现验证码存储和验证功能</li>
     * </ul>
     *
     * <h4>线程安全</h4>
     * <p>构造函数是线程安全的，所有参数都是不可变的或线程安全的实现。</p>
     *
     * <h4>使用示例</h4>
     * <pre>{@code
     * SmsVerifyCodeGenerateImpl smsGenerator = new SmsVerifyCodeGenerateImpl(
     *     securityProperties,
     *     objectMapper,
     *     redisSmsVerifyCodeStore
     * );
     * }</pre>
     *
     * @param securityProperties 安全配置属性，包含短信验证码相关配置
     * @param objectMapper JSON对象映射器，用于请求和响应的序列化
     * @param smsVerifyCodeStore 短信验证码存储服务，用于验证码的生成和管理
     * @throws IllegalArgumentException 如果任何参数为null
     */
    @SuppressFBWarnings(value = "EI_EXPOSE_REP2", justification = "this is thread safe")
    public SmsVerifyCodeGenerateImpl(
            SecurityProperties securityProperties,
            ObjectMapper objectMapper,
            SmsVerifyCodeStore<String> smsVerifyCodeStore) {
        this.securityProperties = securityProperties;
        this.objectMapper = objectMapper;
        this.smsVerifyCodeStore = smsVerifyCodeStore;
        this.smsLogin = securityProperties.getSms();
    }

    /**
     * 检查当前请求是否需要短信验证码生成处理
     *
     * <p>根据配置和请求特征判断是否应该由此服务处理当前请求。</p>
     *
     * <h4>检查条件</h4>
     * <ol>
     *   <li><strong>功能启用</strong>：短信验证码功能必须在配置中启用</li>
     *   <li><strong>请求方法</strong>：必须是POST请求方法</li>
     *   <li><strong>路径匹配</strong>：请求URI必须匹配配置的短信发送路径</li>
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
     *     verifyPath: /api/sms/send
     *
     * // 匹配的请求：POST /api/sms/send
     * // 不匹配的请求：GET /api/sms/send, POST /api/sms/verify
     * }</pre>
     *
     * @param request HTTP请求对象，包含请求方法、URI等信息
     * @return {@code true} 如果当前请求需要短信验证码生成处理；{@code false} 否则
     * @see SecurityProperties.SmsLogin#isEnabled()
     * @see SecurityProperties.SmsLogin#getVerifyPath()
     */
    @Override
    public boolean support(HttpServletRequest request) {
        final SecurityProperties.SmsLogin smsLogin = securityProperties.getSms();
        return smsLogin.isEnabled()
                && JakartaServletUtil.isPostMethod(request)
                && matcher.match(smsLogin.getVerifyPath(), request.getRequestURI());
    }

    /**
     * 执行短信验证码生成和发送的核心逻辑
     *
     * <p>处理短信验证码的完整生成流程，包括参数验证、用户验证、防刷检查、
     * 图形验证码验证（可选）、短信发送和响应返回。</p>
     *
     * <h4>执行流程</h4>
     * <ol>
     *   <li><strong>请求包装</strong>：将请求包装为可重复读取的格式</li>
     *   <li><strong>参数提取</strong>：从请求中提取JSON参数</li>
     *   <li><strong>参数验证</strong>：验证手机号和登录类型参数</li>
     *   <li><strong>用户验证</strong>：根据手机号查询用户并检查账号状态</li>
     *   <li><strong>频率控制</strong>：检查短信发送频率限制</li>
     *   <li><strong>图形验证</strong>：可选的图形验证码二次验证</li>
     *   <li><strong>验证码生成</strong>：生成短信验证码并存储</li>
     *   <li><strong>短信发送</strong>：通过短信服务发送验证码</li>
     *   <li><strong>响应返回</strong>：返回发送结果的JSON响应</li>
     * </ol>
     *
     * <h4>参数提取</h4>
     * <ul>
     *   <li><strong>mobile</strong>：手机号，从配置的参数名中提取</li>
     *   <li><strong>loginType</strong>：登录类型，用于区分不同的登录场景</li>
     *   <li><strong>__TOKEN</strong>：图形验证码令牌（启用图形验证时必需）</li>
     *   <li><strong>verifyCode</strong>：图形验证码（启用图形验证时必需）</li>
     * </ul>
     *
     * <h4>用户验证逻辑</h4>
     * <ul>
     *   <li>根据手机号和登录类型查询用户信息</li>
     *   <li>检查用户是否存在</li>
     *   <li>检查账号是否过期</li>
     *   <li>检查账号是否被锁定</li>
     * </ul>
     *
     * <h4>防刷机制</h4>
     * <ul>
     *   <li><strong>频率限制</strong>：检查同一手机号的发送间隔</li>
     *   <li><strong>图形验证</strong>：可选的图形验证码防机器人</li>
     *   <li><strong>用户验证</strong>：确保手机号对应有效用户</li>
     * </ul>
     *
     * <h4>响应格式</h4>
     * <p>成功响应（JSON）：</p>
     * <pre>{@code
     * {
     *   "id": "短信服务商返回的消息ID",
     *   "resendSeconds": 60,
     *   "validMinutes": 5,
     *   "message": "验证码内容（仅开发模式）"
     * }
     * }</pre>
     *
     * <p>错误响应（JSON）：</p>
     * <pre>{@code
     * {
     *   "status": 417,
     *   "error": "Expectation Failed",
     *   "message": "具体错误信息",
     *   "path": "/api/sms/send",
     *   "timestamp": 1234567890123
     * }
     * }</pre>
     *
     * <h4>异常处理</h4>
     * <ul>
     *   <li><strong>参数异常</strong>：手机号为空、登录类型无效等</li>
     *   <li><strong>用户异常</strong>：用户不存在、账号状态异常等</li>
     *   <li><strong>验证异常</strong>：图形验证码错误、频率限制等</li>
     *   <li><strong>发送异常</strong>：短信服务异常、网络错误等</li>
     * </ul>
     *
     * <h4>开发模式</h4>
     * <p>当{@code smsLogin.mock}为true时，响应中会包含实际的验证码内容，
     * 便于开发和测试，生产环境应禁用此功能。</p>
     *
     * <h4>性能优化</h4>
     * <ul>
     *   <li>参数为空时直接跳过处理</li>
     *   <li>异常情况统一处理，避免重复代码</li>
     *   <li>使用流式JSON写入，减少内存占用</li>
     * </ul>
     *
     * @param httpServletRequest HTTP请求对象，包含请求参数
     * @param httpServletResponse HTTP响应对象，用于返回结果
     * @param chain 过滤器链，用于请求转发
     * @throws IOException 当响应写入失败时抛出
     * @see SmsVerifyCodeStore#generate(String)
     * @see SmsMessageSender#sendVerifyCode(String, String, Integer)
     * @see UserDetailService#loginByMobile(String, String)
     */
    @Override
    public void execute(
            HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain chain)
            throws IOException {
        try {
            LambdaHttpServletRequestWrapper httpServletRequestWrapper = getRequestWrapper(httpServletRequest);
            JSONObject requestParam = getRequestParam(httpServletRequestWrapper);

            if (MapUtils.isEmpty(requestParam)) {
                chain.doFilter(httpServletRequestWrapper, httpServletResponse);
                return;
            }

            String mobile = requestParam.getStr(securityProperties.getSms().getMobile());

            if (mobile == null) {
                throw new VerifyCodeValidationException("手机号不存在！");
            }

            String loginType = requestParam.getStr(loginTypeParameter);

            boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);
            if (!containsLoginType) {
                throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
            }

            LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

            if (loginUser == null) {
                throw new VerifyCodeValidationException("账号密码错误");
            }

            if (loginUser.getAccountExpired()) {
                throw new VerifyCodeValidationException("账号已过期");
            }

            if (loginUser.getAccountLocked()) {
                throw new VerifyCodeValidationException("账号已锁定");
            }

            if (!smsVerifyCodeStore.verifyReSend(mobile)) {
                throw new VerifyCodeValidationException("短信验证码重复获取!");
            }

            if (smsLogin.isEnableVerify()) {

                String verifyToken = requestParam.getStr(CaptchaVerifyCodeGenerateImpl.TOKEN_KEY);
                Assert.isBlank(verifyToken, "__TOKEN不能为空!");

                String verifyCode = requestParam.getStr(CaptchaVerifyCodeGenerateImpl.VERIFY_CODE_PARAMETER);
                Assert.isBlank(verifyCode, "验证码不能为空!");

                boolean verified = captchaStore.validate(verifyToken, verifyCode);
                if (!verified) {
                    throw new VerifyCodeValidationException("验证码不正确!");
                }
            }

            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            String code = smsVerifyCodeStore.generate(mobile);
            SmsSendResult smsSendResult = smsMessageSender.sendVerifyCode(mobile, code, smsLogin.getValidMinutes());
            if (smsSendResult == null || !smsSendResult.isSuccess()) {
                if (smsSendResult != null) {
                    log.error("短信发送失败, {}", smsSendResult.getMessage());
                }
                throw new VerifyCodeValidationException("短信发送失败");
            }
            SmsVerifyCodeResponse smsVerifyCodeResponse = new SmsVerifyCodeResponse();
            smsVerifyCodeResponse.setId(smsSendResult.getId());
            smsVerifyCodeResponse.setResendSeconds(smsLogin.getResendSeconds());
            smsVerifyCodeResponse.setValidMinutes(smsLogin.getValidMinutes());
            if (smsLogin.isMock()) {
                smsVerifyCodeResponse.setMessage(smsSendResult.getMessage());
            }
            objectMapper.writeValue(httpServletResponse.getWriter(), smsVerifyCodeResponse);
        } catch (Exception ex) {
            httpServletResponse.setStatus(HttpServletResponse.SC_EXPECTATION_FAILED);
            httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
            ErrorModel model = new ErrorModel();
            model.setStatus(httpServletResponse.getStatus());
            model.setError(HttpStatus.EXPECTATION_FAILED.getReasonPhrase());
            model.setMessage(ex.getMessage());
            model.setPath(httpServletRequest.getRequestURI());
            model.setTimestamp(System.currentTimeMillis());
            objectMapper.writeValue(httpServletResponse.getWriter(), model);
        }
    }
}
