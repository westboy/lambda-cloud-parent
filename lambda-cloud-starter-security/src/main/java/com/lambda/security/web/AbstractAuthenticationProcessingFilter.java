package com.lambda.security.web;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.handler.AuthenticationFailureHandler;
import com.lambda.security.handler.AuthenticationSuccessHandler;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.log.LogMessage;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 抽象认证处理过滤器
 *
 * <p>这是所有认证处理过滤器的基类，提供了统一的认证处理框架。该过滤器负责拦截特定的认证请求，
 * 执行认证逻辑，并根据认证结果调用相应的成功或失败处理器。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>统一认证框架</strong> - 为不同类型的认证提供统一的处理流程</li>
 *   <li><strong>模板方法模式</strong> - 定义认证处理的骨架，具体认证逻辑由子类实现</li>
 *   <li><strong>可扩展性</strong> - 支持多种认证方式的扩展（表单、短信、第三方等）</li>
 *   <li><strong>处理器模式</strong> - 通过成功/失败处理器实现认证结果的灵活处理</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>请求拦截</strong> - 根据配置的URL模式拦截认证请求</li>
 *   <li><strong>认证处理</strong> - 调用子类实现的具体认证逻辑</li>
 *   <li><strong>异常处理</strong> - 统一处理认证过程中的异常</li>
 *   <li><strong>结果处理</strong> - 根据认证结果调用相应的处理器</li>
 *   <li><strong>请求包装</strong> - 支持对请求进行预处理和包装</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>检查请求是否需要认证（URL匹配）</li>
 *   <li>包装请求对象（可选的预处理）</li>
 *   <li>调用子类的attemptAuthentication方法执行具体认证</li>
 *   <li>认证成功时调用成功处理器</li>
 *   <li>认证失败时调用失败处理器</li>
 * </ol>
 *
 * <h3>子类实现示例</h3>
 * <pre>{@code
 * public class FormAuthenticationProcessingFilter extends AbstractAuthenticationProcessingFilter {
 *     public FormAuthenticationProcessingFilter() {
 *         super("/login");
 *     }
 *
 *     @Override
 *     public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
 *         String username = request.getParameter("username");
 *         String password = request.getParameter("password");
 *         // 执行用户名密码认证逻辑
 *         return userDetailService.loginByUsername(username, password);
 *     }
 * }
 * }</pre>
 *
 * <h3>配置示例</h3>
 * <pre>{@code
 * @Bean
 * public AbstractAuthenticationProcessingFilter authenticationFilter() {
 *     FormAuthenticationProcessingFilter filter = new FormAuthenticationProcessingFilter();
 *     filter.setAuthenticationSuccessHandler(new DefaultAuthenticationSuccessHandler());
 *     filter.setAuthenticationFailureHandler(new DefaultAuthenticationFailureHandler());
 *     return filter;
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see AuthenticationSuccessHandler
 * @see AuthenticationFailureHandler
 * @see LoginUser
 */
@Slf4j
public abstract class AbstractAuthenticationProcessingFilter extends GenericFilterBean {
    /** 登录参数在请求属性中的键名 */
    public static final String LOGIN_PARAMETERS = "loginParameters";

    /** Ant路径匹配器，用于URL模式匹配 */
    protected static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /** 认证成功处理器 */
    private AuthenticationSuccessHandler successHandler;

    /** 认证失败处理器 */
    private AuthenticationFailureHandler failureHandler;

    /** 过滤器处理的URL模式 */
    @Setter
    private String filterProcessesUrl;

    /**
     * 构造函数
     *
     * @param defaultFilterProcessesUrl 默认的过滤器处理URL模式
     */
    protected AbstractAuthenticationProcessingFilter(String defaultFilterProcessesUrl) {
        this.filterProcessesUrl = defaultFilterProcessesUrl;
    }

    /**
     * 过滤器入口方法
     *
     * <p>将ServletRequest和ServletResponse转换为HttpServletRequest和HttpServletResponse，
     * 然后委托给具体的doFilter方法处理。</p>
     *
     * @param request  servlet请求
     * @param response servlet响应
     * @param chain    过滤器链
     * @throws IOException      IO异常
     * @throws ServletException servlet异常
     */
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        this.doFilter((HttpServletRequest) request, (HttpServletResponse) response, chain);
    }

    /**
     * 核心过滤器处理方法
     *
     * <p>这是认证过滤器的核心处理逻辑，包含以下步骤：</p>
     * <ol>
     *   <li>检查请求是否需要认证</li>
     *   <li>如果不需要认证，直接传递给下一个过滤器</li>
     *   <li>如果需要认证，则执行认证流程：
     *       <ul>
     *         <li>包装请求对象</li>
     *         <li>尝试执行认证</li>
     *         <li>处理认证成功或失败的结果</li>
     *       </ul>
     *   </li>
     * </ol>
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param chain    过滤器链
     * @throws IOException      IO异常
     * @throws ServletException servlet异常
     */
    private void doFilter(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        if (this.nonRequiresAuthentication(request)) {
            chain.doFilter(request, response);
        } else {
            try {
                HttpServletRequest wrapRequest = this.wrapRequest(request);
                LoginUser loginUser = this.attemptAuthentication(wrapRequest, response);
                this.successfulAuthentication(wrapRequest, response, chain, loginUser);
            } catch (Exception exception) {
                if (exception instanceof AuthenticationException) {
                    this.unsuccessfulAuthentication(request, response, (AuthenticationException) exception);
                } else {
                    log.error("authentication exception", exception);
                    this.unsuccessfulAuthentication(
                            request, response, new AuthenticationException(exception.getMessage()));
                }
            }
        }
    }

    /**
     * 包装请求对象
     *
     * <p>子类可以重写此方法来对请求进行预处理或包装。默认实现直接返回原始请求。</p>
     *
     * <h4>常见用途</h4>
     * <ul>
     *   <li><strong>请求体缓存</strong> - 缓存请求体内容以支持多次读取</li>
     *   <li><strong>参数解析</strong> - 预解析请求参数或JSON数据</li>
     *   <li><strong>安全处理</strong> - 对请求进行XSS过滤或其他安全处理</li>
     *   <li><strong>编码转换</strong> - 处理字符编码问题</li>
     * </ul>
     *
     * @param request 原始HTTP请求
     * @return 包装后的HTTP请求
     * @throws IOException IO异常
     */
    protected HttpServletRequest wrapRequest(HttpServletRequest request) throws IOException {
        return request;
    }

    /**
     * 判断请求是否不需要认证
     *
     * <p>通过Ant路径匹配器检查当前请求的URI是否匹配配置的过滤器处理URL模式。
     * 如果匹配，则表示需要认证；如果不匹配，则表示不需要认证。</p>
     *
     * <h4>匹配规则</h4>
     * <ul>
     *   <li><strong>精确匹配</strong> - /login 只匹配 /login</li>
     *   <li><strong>通配符匹配</strong> - /api/* 匹配 /api/user、/api/order 等</li>
     *   <li><strong>递归匹配</strong> - /api/** 匹配 /api 下的所有路径</li>
     * </ul>
     *
     * @param request HTTP请求
     * @return true表示不需要认证，false表示需要认证
     */
    protected boolean nonRequiresAuthentication(HttpServletRequest request) {
        if (ANT_PATH_MATCHER.match(this.filterProcessesUrl, request.getRequestURI())) {
            return false;
        } else {
            if (this.logger.isTraceEnabled()) {
                this.logger.trace(LogMessage.format("Did not match request to %s", filterProcessesUrl));
            }
            return true;
        }
    }

    /**
     * 尝试认证（抽象方法）
     *
     * <p>这是模板方法模式的核心，由子类实现具体的认证逻辑。不同的认证方式
     * （如表单认证、短信认证、第三方认证等）需要实现不同的认证策略。</p>
     *
     * <h4>实现要求</h4>
     * <ul>
     *   <li><strong>参数提取</strong> - 从请求中提取认证所需的参数</li>
     *   <li><strong>参数验证</strong> - 验证参数的有效性和完整性</li>
     *   <li><strong>认证处理</strong> - 调用相应的认证服务进行身份验证</li>
     *   <li><strong>用户构建</strong> - 构建并返回LoginUser对象</li>
     *   <li><strong>异常处理</strong> - 适当地抛出AuthenticationException</li>
     * </ul>
     *
     * <h4>常见实现模式</h4>
     * <pre>{@code
     * @Override
     * public LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
     *     // 1. 提取认证参数
     *     String username = request.getParameter("username");
     *     String password = request.getParameter("password");
     *
     *     // 2. 参数验证
     *     if (StrUtil.isBlank(username) || StrUtil.isBlank(password)) {
     *         throw new AuthenticationException("用户名或密码不能为空");
     *     }
     *
     *     // 3. 执行认证
     *     return userDetailService.loginByUsername(username, password);
     * }
     * }</pre>
     *
     * @param request  HTTP请求，包含认证所需的参数
     * @param response HTTP响应，可用于设置认证相关的响应头或Cookie
     * @return 认证成功的用户信息
     * @throws AuthenticationException 认证失败时抛出
     * @throws IOException             IO异常
     * @throws ServletException       Servlet异常
     */
    public abstract LoginUser attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException, IOException, ServletException;

    /**
     * 处理认证成功
     *
     * <p>当认证成功时调用此方法，委托给配置的成功处理器来处理认证成功的后续逻辑。</p>
     *
     * <h4>典型处理逻辑</h4>
     * <ul>
     *   <li><strong>生成Token</strong> - 生成JWT或Session Token</li>
     *   <li><strong>设置响应</strong> - 设置响应头、Cookie或返回JSON数据</li>
     *   <li><strong>记录日志</strong> - 记录登录成功的审计日志</li>
     *   <li><strong>更新状态</strong> - 更新用户最后登录时间等信息</li>
     * </ul>
     *
     * @param request   HTTP请求
     * @param response  HTTP响应
     * @param chain     过滤器链（通常不会继续执行）
     * @param loginUser 认证成功的用户信息
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    protected void successfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, FilterChain chain, LoginUser loginUser)
            throws IOException, ServletException {
        this.successHandler.onAuthenticationSuccess(request, response, loginUser);
    }

    /**
     * 处理认证失败
     *
     * <p>当认证失败时调用此方法，委托给配置的失败处理器来处理认证失败的后续逻辑。</p>
     *
     * <h4>典型处理逻辑</h4>
     * <ul>
     *   <li><strong>错误响应</strong> - 返回适当的错误状态码和错误信息</li>
     *   <li><strong>记录日志</strong> - 记录登录失败的审计日志</li>
     *   <li><strong>安全措施</strong> - 实施登录失败次数限制、账户锁定等安全策略</li>
     *   <li><strong>统计分析</strong> - 收集失败统计信息用于安全分析</li>
     * </ul>
     *
     * @param request  HTTP请求
     * @param response HTTP响应
     * @param failed   认证失败的异常信息
     * @throws IOException      IO异常
     * @throws ServletException Servlet异常
     */
    protected void unsuccessfulAuthentication(
            HttpServletRequest request, HttpServletResponse response, AuthenticationException failed)
            throws IOException, ServletException {
        this.failureHandler.onAuthenticationFailure(request, response, failed);
    }

    /**
     * 设置认证成功处理器
     *
     * @param successHandler 认证成功处理器，不能为null
     * @throws IllegalArgumentException 如果successHandler为null
     */
    public void setAuthenticationSuccessHandler(AuthenticationSuccessHandler successHandler) {
        Assert.notNull(successHandler, "successHandler cannot be null");
        this.successHandler = successHandler;
    }

    /**
     * 设置认证失败处理器
     *
     * @param failureHandler 认证失败处理器，不能为null
     * @throws IllegalArgumentException 如果failureHandler为null
     */
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }

    /**
     * 获取认证成功处理器
     *
     * @return 认证成功处理器
     */
    protected AuthenticationSuccessHandler getSuccessHandler() {
        return this.successHandler;
    }

    /**
     * 获取认证失败处理器
     *
     * @return 认证失败处理器
     */
    protected AuthenticationFailureHandler getFailureHandler() {
        return this.failureHandler;
    }

    /**
     * 从请求体中获取用户登录参数
     *
     * <p>解析HTTP请求体中的JSON数据，提取登录相关的参数，并将其存储在请求属性中
     * 以便后续处理使用。这个方法主要用于处理POST请求中的JSON格式登录数据。</p>
     *
     * <h4>处理流程</h4>
     * <ol>
     *   <li>使用WebHttpUtils工具类解析请求体中的JSON数据</li>
     *   <li>将解析后的参数Map存储到请求属性中</li>
     *   <li>返回参数Map供认证逻辑使用</li>
     * </ol>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li><strong>JSON登录</strong> - 前端通过JSON格式提交登录信息</li>
     *   <li><strong>API认证</strong> - RESTful API的认证参数传递</li>
     *   <li><strong>移动端登录</strong> - 移动应用的登录请求处理</li>
     * </ul>
     *
     * <h4>请求体示例</h4>
     * <pre>{@code
     * {
     *   "username": "admin",
     *   "password": "123456",
     *   "captcha": "abc123"
     * }
     * }</pre>
     *
     * @param request HTTP请求对象
     * @return 解析后的登录参数Map
     * @throws AuthenticationException 当解析请求体失败时抛出
     */
    public Map<String, Object> getUserLoginForRequestBody(HttpServletRequest request) {
        try {
            Map<String, Object> loginParameters = WebHttpUtils.getRequestBody(request);
            request.setAttribute(LOGIN_PARAMETERS, loginParameters);
            return loginParameters;
        } catch (Exception e) {
            throw new AuthenticationException("获取用户数据失败！");
        }
    }
}
