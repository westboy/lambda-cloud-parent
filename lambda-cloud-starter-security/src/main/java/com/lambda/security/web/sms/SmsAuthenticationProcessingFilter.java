package com.lambda.security.web.sms;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.StpLogicUtils;
import com.lambda.security.LoginErrorCode;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.exception.BadCredentialsException;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.AbstractAuthenticationProcessingFilter;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.function.Function;
import org.apache.commons.collections4.MapUtils;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * 短信认证处理过滤器
 *
 * <p>专门处理基于短信验证码的用户认证请求。该过滤器继承自AbstractAuthenticationProcessingFilter，
 * 实现了短信登录的完整流程，包括手机号验证、登录类型检查、设备信息处理等功能。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>免密登录</strong> - 提供基于短信验证码的免密码登录方式</li>
 *   <li><strong>多端支持</strong> - 支持不同设备和登录类型的认证</li>
 *   <li><strong>参数灵活</strong> - 支持URL参数和JSON请求体两种参数传递方式</li>
 *   <li><strong>安全可靠</strong> - 通过手机号和验证码确保用户身份</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>手机号验证</strong> - 验证手机号格式和有效性</li>
 *   <li><strong>参数提取</strong> - 从请求参数或JSON体中提取认证信息</li>
 *   <li><strong>登录类型检查</strong> - 验证登录类型是否支持</li>
 *   <li><strong>用户加载</strong> - 根据手机号加载用户信息</li>
 *   <li><strong>上下文设置</strong> - 设置登录相关的请求属性</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li><strong>请求方法检查</strong> - 确保使用POST方法</li>
 *   <li><strong>参数提取</strong> - 从URL参数或请求体中提取手机号、登录类型、设备信息</li>
 *   <li><strong>参数验证</strong> - 验证手机号不为空，登录类型有效</li>
 *   <li><strong>用户查找</strong> - 根据手机号和登录类型查找用户</li>
 *   <li><strong>属性设置</strong> - 将认证信息设置到请求属性中</li>
 *   <li><strong>返回用户</strong> - 返回认证成功的用户信息</li>
 * </ol>
 *
 * <h3>支持的参数</h3>
 * <ul>
 *   <li><strong>mobile</strong> - 手机号（必需）</li>
 *   <li><strong>loginType</strong> - 登录类型（可选，默认使用当前登录类型）</li>
 *   <li><strong>loginDevice</strong> - 登录设备（可选，默认为"default"）</li>
 * </ul>
 *
 * <h3>请求格式</h3>
 * <pre>{@code
 * // URL参数方式
 * POST /sms/login?mobile=13800138000&loginType=user&loginDevice=mobile
 *
 * // JSON请求体方式
 * POST /sms/login
 * Content-Type: application/json
 * {
 *   "mobile": "13800138000",
 *   "loginType": "user",
 *   "loginDevice": "mobile"
 * }
 * }</pre>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 配置过滤器
 * @Bean
 * public SmsAuthenticationProcessingFilter smsFilter() {
 *     SmsAuthenticationProcessingFilter filter =
 *         new SmsAuthenticationProcessingFilter("/sms/login");
 *     filter.setUserDetailService(userDetailService);
 *     return filter;
 * }
 * }</pre>
 *
 * @author Jin
 * @see AbstractAuthenticationProcessingFilter
 * @see UserDetailService
 */
@SuppressWarnings("all")
public class SmsAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {

    /**
     * 手机号参数名
     * <p>
     * 用于从HTTP请求参数或JSON请求体中提取手机号的参数名称。
     * 默认值为"mobile"，可以通过setter方法自定义以适应不同的前端实现。
     * </p>
     *
     * <h3>使用示例：</h3>
     * <ul>
     *   <li>默认：mobile=13800138000</li>
     *   <li>自定义：phone=13800138000</li>
     *   <li>国际化：phoneNumber=+8613800138000</li>
     * </ul>
     */
    private String mobileParameter = "mobile";

    /**
     * 登录类型参数名
     * <p>
     * 用于从HTTP请求参数或JSON请求体中提取登录类型的参数名称。
     * 默认值为"loginType"，用于区分不同的用户体系、业务场景或权限级别。
     * </p>
     *
     * <h3>常见登录类型：</h3>
     * <ul>
     *   <li><strong>user</strong> - 普通用户登录</li>
     *   <li><strong>admin</strong> - 管理员登录</li>
     *   <li><strong>merchant</strong> - 商户登录</li>
     *   <li><strong>customer</strong> - 客户登录</li>
     * </ul>
     */
    private String loginTypeParameter = "loginType";

    /**
     * 设备参数名
     * <p>
     * 用于从HTTP请求参数或JSON请求体中提取登录设备信息的参数名称。
     * 默认值为"loginDevice"，用于标识用户登录时使用的设备类型，
     * 有助于实现多端登录管理和设备安全策略。
     * </p>
     *
     * <h3>常见设备类型：</h3>
     * <ul>
     *   <li><strong>mobile</strong> - 手机移动端</li>
     *   <li><strong>web</strong> - 网页浏览器</li>
     *   <li><strong>app</strong> - 移动应用</li>
     *   <li><strong>tablet</strong> - 平板设备</li>
     *   <li><strong>desktop</strong> - 桌面应用</li>
     * </ul>
     */
    private String deviceParameter = "loginDevice";

    /**
     * 用户详情服务
     * <p>
     * 用于根据手机号查找和加载用户信息的服务接口实现。
     * 该服务是短信认证过滤器的核心依赖，负责实现具体的用户查找逻辑。
     * </p>
     *
     * <h3>主要职责：</h3>
     * <ul>
     *   <li>根据手机号查询用户信息</li>
     *   <li>支持多种登录类型的用户查找</li>
     *   <li>返回完整的用户详情数据</li>
     *   <li>处理用户不存在的情况</li>
     * </ul>
     *
     * @see UserDetailService#loginByMobile(String, String)
     */
    private UserDetailService userDetailService;

    /**
     * 构造短信认证处理过滤器
     * <p>
     * 创建一个短信认证过滤器实例，指定处理短信登录请求的URL模式。
     * 只有匹配该URL模式的请求才会被此过滤器拦截和处理。
     * </p>
     *
     * <h3>URL配置示例：</h3>
     * <ul>
     *   <li>/sms/login - 标准短信登录路径</li>
     *   <li>/api/auth/sms - API短信认证路径</li>
     *   <li>/mobile/login - 移动端短信登录</li>
     *   <li>/user/sms/auth - 用户短信认证</li>
     * </ul>
     *
     * @param defaultFilterProcessesUrl 过滤器处理的URL模式
     */
    public SmsAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        super(defaultFilterProcessesUrl);
    }

    /**
     * 设置用户详情服务
     * <p>
     * 注入用户详情服务实例，用于根据手机号查找用户信息。
     * 该服务是短信认证过滤器正常工作的核心依赖。
     * </p>
     *
     * <h3>服务要求：</h3>
     * <ul>
     *   <li>实现UserDetailService接口</li>
     *   <li>支持根据手机号查询用户</li>
     *   <li>支持多种登录类型</li>
     *   <li>返回完整的用户详情信息</li>
     * </ul>
     *
     * @param userDetailService 用户详情服务实例，不能为null
     * @see UserDetailService
     */
    public void setUserDetailService(UserDetailService userDetailService) {
        this.userDetailService = userDetailService;
    }

    /**
     * 尝试进行短信认证
     *
     * <p>这是短信认证的核心方法，负责完整的短信登录验证流程。
     * 包括请求方法验证、参数提取、用户查找等步骤。</p>
     *
     * <p><strong>认证流程：</strong></p>
     * <ol>
     *   <li><strong>请求方法验证</strong> - 确保使用POST方法</li>
     *   <li><strong>参数提取</strong> - 从URL参数中提取手机号、登录类型、设备信息</li>
     *   <li><strong>JSON体处理</strong> - 如果URL参数为空，从JSON请求体中提取参数</li>
     *   <li><strong>参数验证</strong> - 验证手机号不为空，登录类型有效</li>
     *   <li><strong>属性设置</strong> - 将参数设置到请求属性中</li>
     *   <li><strong>用户查找</strong> - 根据手机号和登录类型查找用户</li>
     *   <li><strong>结果返回</strong> - 返回找到的用户信息</li>
     * </ol>
     *
     * <p><strong>参数获取策略：</strong></p>
     * <ul>
     *   <li>优先从URL参数中获取</li>
     *   <li>如果URL参数为空，则从JSON请求体中获取</li>
     *   <li>支持参数的默认值设置</li>
     * </ul>
     *
     * <p><strong>默认值处理：</strong></p>
     * <ul>
     *   <li>loginType默认使用StpUtil.getLoginType()</li>
     *   <li>device默认使用"default"</li>
     *   <li>mobile必须提供，无默认值</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @param response HTTP响应对象
     * @return 认证成功的用户信息
     * @throws AuthenticationException 如果认证失败或请求方法不支持
     * @throws BadCredentialsException 如果参数验证失败或用户不存在
     * @throws IOException 如果I/O操作失败
     * @throws ServletException 如果Servlet处理失败
     */
    @Override
    public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException {
        if (!RequestMethod.POST.name().equals(request.getMethod())) {
            throw new AuthenticationException("Authentication method not supported: " + request.getMethod());
        }
        String mobile = safeObtain(this::obtainMobile, request);
        String loginType = safeObtain(this::obtainLoginType, request);
        String device = safeObtain(this::obtainDevice, request);

        if (StrUtil.isBlank(mobile)) {
            Map<String, Object> smsLogin = getUserLoginForRequestBody(request);
            if (MapUtils.isEmpty(smsLogin)) {
                throw new BadCredentialsException("Request body is empty");
            }
            mobile = (String) smsLogin.getOrDefault(this.mobileParameter, "");

            if (StrUtil.isBlank(mobile)) {
                throw new BadCredentialsException("Mobile is empty");
            }
            request.setAttribute(mobileParameter, mobile);

            if (StrUtil.isBlank(loginType)) {
                loginType = (String) smsLogin.getOrDefault(this.loginTypeParameter, StpUtil.getLoginType());
                request.setAttribute(loginTypeParameter, loginType);
            }

            if (StrUtil.isBlank(device)) {
                device = (String) smsLogin.getOrDefault(this.deviceParameter, "default");
                request.setAttribute(deviceParameter, device);
            }
        }

        if (StrUtil.isNotBlank(mobile)) {
            request.setAttribute(mobileParameter, mobile);
        }

        if (StrUtil.isNotBlank(loginType)) {
            request.setAttribute(loginTypeParameter, loginType);
        }

        boolean containsLoginType = StpLogicUtils.containsLoginType(loginType);
        if (!containsLoginType) {
            throw new AuthenticationException(LoginErrorCode.CODE_20000, "登录类型错误！");
        }

        if (StrUtil.isNotBlank(device)) {
            request.setAttribute(deviceParameter, device);
        }

        LoginUser loginUser = userDetailService.loginByMobile(mobile, loginType);

        if (loginUser == null) {
            throw new BadCredentialsException("User not found");
        }

        return loginUser;
    }

    /**
     * 获取设备参数
     * <p>
     * 从HTTP请求参数中获取登录设备信息。设备信息用于标识用户登录时
     * 使用的设备类型，有助于实现多端登录管理、设备安全策略和用户行为分析。
     * </p>
     *
     * <h3>设备类型用途：</h3>
     * <ul>
     *   <li>多端登录管理：控制同一用户在不同设备的登录状态</li>
     *   <li>安全策略：针对不同设备实施不同的安全措施</li>
     *   <li>用户体验：为不同设备提供定制化的功能</li>
     *   <li>统计分析：分析用户的设备使用偏好</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 设备参数值，如果不存在则返回null
     */
    private String obtainDevice(HttpServletRequest request) {
        return request.getParameter(this.deviceParameter);
    }

    /**
     * 获取登录类型参数
     * <p>
     * 从HTTP请求参数中获取登录类型信息。登录类型用于区分不同的
     * 用户体系、业务场景或权限级别，实现多租户和多角色的认证管理。
     * </p>
     *
     * <h3>登录类型应用：</h3>
     * <ul>
     *   <li>用户体系区分：普通用户、VIP用户、企业用户</li>
     *   <li>权限级别：管理员、操作员、访客</li>
     *   <li>业务场景：前台用户、后台管理、第三方接入</li>
     *   <li>租户隔离：不同租户使用不同的登录类型</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 登录类型参数值，如果不存在则返回null
     */
    @Nullable
    protected String obtainLoginType(HttpServletRequest request) {
        return request.getParameter(this.loginTypeParameter);
    }

    /**
     * 获取手机号参数
     * <p>
     * 从HTTP请求参数中获取手机号信息。手机号是短信认证的核心标识符，
     * 用于唯一标识用户身份并作为短信验证码的接收方。
     * </p>
     *
     * <h3>手机号要求：</h3>
     * <ul>
     *   <li>格式正确：符合手机号码格式规范</li>
     *   <li>已注册：在系统中已注册的有效手机号</li>
     *   <li>可接收：能够正常接收短信验证码</li>
     *   <li>未被禁用：账户状态正常，未被系统禁用</li>
     * </ul>
     *
     * @param request HTTP请求对象
     * @return 手机号参数值，如果不存在则返回null
     */
    @Nullable
    protected String obtainMobile(HttpServletRequest request) {
        return request.getParameter(this.mobileParameter);
    }

    /**
     * 安全获取参数值
     *
     * <p>使用函数式接口安全地从请求中提取参数值，
     * 如果参数值为null则返回空字符串，避免空指针异常。</p>
     *
     * <p><strong>使用场景：</strong></p>
     * <ul>
     *   <li>统一处理可能为null的参数值</li>
     *   <li>简化参数提取的代码逻辑</li>
     *   <li>提供一致的空值处理策略</li>
     * </ul>
     *
     * @param extractor 参数提取函数
     * @param request HTTP请求对象
     * @return 参数值，如果为null则返回空字符串
     */
    private String safeObtain(Function<HttpServletRequest, String> extractor, HttpServletRequest request) {
        String value = extractor.apply(request);
        return value != null ? value : "";
    }
}
