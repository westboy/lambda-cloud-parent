package com.lambda.security.web.form;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.Constants;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.Assert;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.exception.*;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.lang.Nullable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 表单认证处理过滤器
 *
 * <p>这是处理基于表单的用户名密码认证的过滤器实现。它继承自AbstractAuthenticationProcessingFilter，
 * 专门用于处理传统的表单登录请求，支持用户名密码认证、账户锁定策略、多设备登录等功能。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>表单认证</strong> - 处理基于HTML表单的用户名密码登录</li>
 *   <li><strong>安全防护</strong> - 集成账户锁定策略防止暴力破解</li>
 *   <li><strong>多端支持</strong> - 支持不同设备类型的登录管理</li>
 *   <li><strong>灵活配置</strong> - 支持自定义参数名称和认证逻辑</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>参数提取</strong> - 从请求中提取用户名、密码、设备类型等参数</li>
 *   <li><strong>JSON支持</strong> - 支持从JSON请求体中获取登录参数</li>
 *   <li><strong>密码验证</strong> - 使用PasswordEncoder进行密码加密验证</li>
 *   <li><strong>账户检查</strong> - 检查账户状态（过期、锁定等）</li>
 *   <li><strong>失败处理</strong> - 记录登录失败次数并实施锁定策略</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>验证请求方法（仅支持POST）</li>
 *   <li>提取认证参数（用户名、密码、设备、登录类型）</li>
 *   <li>检查参数有效性</li>
 *   <li>检查账户锁定状态</li>
 *   <li>验证登录类型</li>
 *   <li>加载用户信息</li>
 *   <li>验证密码</li>
 *   <li>更新登录状态</li>
 * </ol>
 *
 * <h3>支持的参数</h3>
 * <ul>
 *   <li><strong>username</strong> - 用户名（可配置参数名）</li>
 *   <li><strong>password</strong> - 密码（可配置参数名）</li>
 *   <li><strong>loginType</strong> - 登录类型（如admin、user等）</li>
 *   <li><strong>device</strong> - 设备类型（如web、mobile、app等）</li>
 * </ul>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * @Bean
 * public FormAuthenticationProcessingFilter formAuthenticationFilter() {
 *     FormAuthenticationProcessingFilter filter = new FormAuthenticationProcessingFilter("/login");
 *     filter.setUserDetailService(userDetailService);
 *     filter.setPasswordEncoder(passwordEncoder);
 *     filter.setFormLockingStrategy(formLockingStrategy);
 *     filter.setUsernameParameter("email"); // 自定义用户名参数
 *     return filter;
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AbstractAuthenticationProcessingFilter
 * @see FormLockingStrategy
 * @see UserDetailService
 */
@Setter
@Getter
public class FormAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 用户名参数名
     * <p>
     * 用于从HTTP请求中提取用户名的参数名称。默认值为"username"，
     * 可以通过setUsernameParameter方法自定义，以适应不同的前端实现。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <ul>
     *   <li>默认：username=admin</li>
     *   <li>自定义：email=admin@example.com</li>
     *   <li>手机号：mobile=13800138000</li>
     * </ul>
     */
    private String usernameParameter = "username";

    /**
     * 密码参数名
     * <p>
     * 用于从HTTP请求中提取密码的参数名称。默认值为"password"，
     * 可以通过setPasswordParameter方法自定义。
     * </p>
     */
    private String passwordParameter = "password";

    /**
     * 表单锁定策略
     * <p>
     * 用于防止暴力破解攻击的锁定策略实现。当用户连续登录失败达到
     * 指定次数时，会临时锁定账户一段时间。
     * </p>
     *
     * <h3>主要功能：</h3>
     * <ul>
     *   <li>记录登录失败次数</li>
     *   <li>检查账户锁定状态</li>
     *   <li>实施临时锁定策略</li>
     *   <li>清除成功登录的失败记录</li>
     * </ul>
     *
     * @see FormLockingStrategy
     */
    private FormLockingStrategy formLockingStrategy;

    /**
     * 用户详情服务
     * <p>
     * 用于加载用户信息的服务接口实现。负责根据用户名和登录类型
     * 查询用户详细信息，包括密码、权限、账户状态等。
     * </p>
     *
     * <h3>主要职责：</h3>
     * <ul>
     *   <li>根据用户名查询用户信息</li>
     *   <li>支持多种登录类型</li>
     *   <li>返回完整的用户详情</li>
     *   <li>处理用户不存在的情况</li>
     * </ul>
     *
     * @see UserDetailService
     */
    private UserDetailService userDetailService;

    /**
     * 密码编码器
     * <p>
     * 用于密码加密和验证的编码器。支持多种加密算法，
     * 如BCrypt、PBKDF2、SCrypt等，确保密码安全存储和验证。
     * </p>
     *
     * <h3>主要功能：</h3>
     * <ul>
     *   <li>密码加密：将明文密码转换为密文</li>
     *   <li>密码验证：验证明文密码与密文是否匹配</li>
     *   <li>安全算法：使用强加密算法保护密码</li>
     *   <li>盐值处理：自动生成和处理盐值</li>
     * </ul>
     *
     * @see PasswordEncoder
     */
    private PasswordEncoder passwordEncoder;

    private List<FormLoginValidator> formLoginValidators = new ArrayList<>();

    public void setFormLoginValidators(List<FormLoginValidator> formLoginValidators) {
        if (formLoginValidators == null) {
            this.formLoginValidators = new ArrayList<>();
            return;
        }
        List<FormLoginValidator> validators = new ArrayList<>(formLoginValidators);
        AnnotationAwareOrderComparator.sort(validators);
        this.formLoginValidators = validators;
    }

    /**
     * 构造表单认证处理过滤器
     * <p>
     * 创建一个新的表单认证处理过滤器实例，指定处理认证请求的URL路径。
     * 只有匹配该路径的请求才会被此过滤器处理。
     * </p>
     *
     * <h3>URL配置示例：</h3>
     * <ul>
     *   <li>/login - 标准登录路径</li>
     *   <li>/api/auth/login - API登录路径</li>
     *   <li>/admin/login - 管理员登录路径</li>
     * </ul>
     *
     * @param defaultFilterProcessesUrl 默认的过滤器处理URL路径
     */
    public FormAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    /**
     * 设置用户详情服务
     * <p>
     * 配置用户详情服务实现，该服务负责根据用户名加载用户信息。
     * 这是表单认证过滤器正常工作的必要依赖。
     * </p>
     *
     * <h3>实现要求：</h3>
     * <ul>
     *   <li>实现UserDetailService接口</li>
     *   <li>支持根据用户名查询用户</li>
     *   <li>返回完整的用户详情信息</li>
     *   <li>处理用户不存在的情况</li>
     * </ul>
     *
     * @param userDetailService 用户详情服务实现，不能为null
     * @throws IllegalArgumentException 如果userDetailService为null
     * @see UserDetailService
     */
    public void setUserDetailService(UserDetailService userDetailService) {
        Assert.notNull(userDetailService, "userDetailService is null, please impl userDetailService");
        this.userDetailService = userDetailService;
    }

    /**
     * 尝试执行表单认证
     *
     * <p>这是表单认证的核心方法，实现了完整的用户名密码认证流程，包括参数提取、
     * 验证、账户检查、密码验证等步骤。</p>
     *
     * <h4>认证步骤</h4>
     * <ol>
     *   <li>验证HTTP方法（仅支持POST）</li>
     *   <li>提取认证参数（支持表单参数和JSON）</li>
     *   <li>参数有效性检查</li>
     *   <li>账户锁定状态检查</li>
     *   <li>登录类型验证</li>
     *   <li>用户信息加载</li>
     *   <li>账户状态检查</li>
     *   <li>密码验证</li>
     *   <li>更新登录状态</li>
     * </ol>
     *
     * @param request  HTTP请求，包含认证参数
     * @param response HTTP响应
     * @return 认证成功的用户信息
     * @throws AuthenticationException 认证失败时抛出
     */
    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationException("Authentication method not supported: " + request.getMethod());
        }
        FormLoginContext context = resolveLoginContext(request, response);

        for (FormLoginValidator validator : this.formLoginValidators) {
            if (validator.support(context)) {
                validator.validate(context);
            }
        }

        String username = context.username();
        String password = context.password();
        String device = context.device();
        String loginType = context.loginType();

        if (StringUtils.isBlank(username)) {
            throw new UsernameNotFoundException("账号不能为空！");
        }
        if (StringUtils.isBlank(password)) {
            throw new UsernameNotFoundException("密码不能为空！");
        }
        if (formLockingStrategy.checkFailureTimes(username)) {
            throw new AccountLockedException("账号" + username + "已经被锁定:" + formLockingStrategy.getDuration()
                    + formLockingStrategy.getTimeUnit().name());
        }

        if (StrUtil.isEmpty(loginType)) {
            loginType = StpUtil.getLoginType();
        }

        boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);

        if (!containsLoginType) {
            throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
        }

        request.setAttribute(Constants.LOGIN_TYPE, loginType);

        if (StrUtil.isEmpty(device)) {
            device = "default";
        }
        request.setAttribute(Constants.LOGIN_DEVICE, device);

        LoginUser loginUser = userDetailService.loginByUsername(username, loginType);
        if (loginUser == null) {
            throw new UsernameNotFoundException("用户不存在！");
        }

        if (loginUser.getAccountExpired() == null || Boolean.TRUE.equals(loginUser.getAccountExpired())) {
            throw new AccountExpireException("账号已过期");
        }

        if (loginUser.getAccountLocked() == null || Boolean.TRUE.equals(loginUser.getAccountLocked())) {
            throw new AccountLockedException("账号已锁定");
        }

        String credentials = loginUser.getCredentials();
        boolean matches = passwordEncoder.matches(password, credentials);
        if (!matches) {
            formLockingStrategy.loginFailure(username);
            throw new BadCredentialsException("用户名或密码错误！");
        }
        formLockingStrategy.loginSuccess(username);
        return loginUser;
    }

    private FormLoginContext resolveLoginContext(HttpServletRequest request, HttpServletResponse response) {
        String username = StringUtils.defaultString(obtainUsername(request)).trim();
        String password = StringUtils.defaultString(obtainPassword(request));
        String device = StringUtils.defaultString(obtainDevice(request));
        String loginType = StringUtils.defaultString(obtainLoginType(request));
        Map<String, Object> requestBody = null;

        if (StringUtils.isBlank(username) && StringUtils.isBlank(password)) {
            Map<String, Object> user = getUserLoginForRequestBody(request);
            if (MapUtils.isNotEmpty(user)) {
                requestBody = user;
                username = StringUtils.defaultString((String) user.getOrDefault(this.getUsernameParameter(), ""))
                        .trim();
                password = StringUtils.defaultString((String) user.getOrDefault(this.getPasswordParameter(), ""));
                if (StringUtils.isBlank(loginType)) {
                    loginType = StringUtils.defaultString(
                            (String) user.getOrDefault(Constants.LOGIN_TYPE, StpUtil.getLoginType()));
                }
                if (StringUtils.isBlank(device)) {
                    device = StringUtils.defaultString((String) user.getOrDefault(Constants.LOGIN_DEVICE, "default"));
                }
            }
        }

        return new FormLoginContext(request, response, requestBody, username, password, device, loginType);
    }

    /**
     * 从请求中获取设备类型
     * <p>
     * 提取用户登录时使用的设备类型信息，用于多端登录管理和统计分析。
     * 设备类型有助于实现不同设备的登录策略和会话管理。
     * </p>
     *
     * <h3>常见设备类型：</h3>
     * <ul>
     *   <li><strong>web</strong> - 网页浏览器</li>
     *   <li><strong>mobile</strong> - 手机移动端</li>
     *   <li><strong>app</strong> - 移动应用</li>
     *   <li><strong>desktop</strong> - 桌面应用</li>
     *   <li><strong>tablet</strong> - 平板设备</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 设备类型字符串，可能为null
     */
    private String obtainDevice(HttpServletRequest request) {
        return request.getParameter(Constants.LOGIN_DEVICE);
    }

    /**
     * 从请求中获取登录类型
     *
     * <p>登录类型用于区分不同的用户角色或系统模块，如"admin"、"user"、"merchant"等。</p>
     *
     * @param request HTTP请求
     * @return 登录类型，可能为null
     */
    @Nullable
    protected String obtainLoginType(HttpServletRequest request) {
        return request.getParameter(Constants.LOGIN_TYPE);
    }

    /**
     * 从请求中获取密码
     *
     * <p>根据配置的密码参数名从请求中提取密码。默认参数名为"password"，
     * 可通过setPasswordParameter方法自定义。</p>
     *
     * @param request HTTP请求
     * @return 密码，可能为null
     */
    @Nullable
    protected String obtainPassword(HttpServletRequest request) {
        return request.getParameter(this.passwordParameter);
    }

    /**
     * 从请求中获取用户名
     *
     * <p>根据配置的用户名参数名从请求中提取用户名。默认参数名为"username"，
     * 可通过setUsernameParameter方法自定义。</p>
     *
     * @param request HTTP请求
     * @return 用户名，可能为null
     */
    @Nullable
    protected String obtainUsername(HttpServletRequest request) {
        return request.getParameter(this.usernameParameter);
    }
}
