package com.lambda.security.web.verify;

import com.lambda.security.handler.AuthenticationFailureHandler;
import com.lambda.security.web.verify.service.VerifyCodeService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.util.Assert;
import org.springframework.web.filter.GenericFilterBean;

/**
 * 验证码过滤器
 * <p>
 * 该过滤器负责在用户认证过程中进行验证码的验证处理。通过策略模式支持多种验证码类型，
 * 如图形验证码、短信验证码、邮箱验证码等。过滤器会根据请求特征自动选择合适的验证码服务进行处理。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>多类型支持</strong>：支持图形、短信、邮箱等多种验证码类型</li>
 *   <li><strong>策略模式</strong>：通过VerifyCodeService接口实现不同验证码的处理逻辑</li>
 *   <li><strong>自动匹配</strong>：根据请求特征自动选择合适的验证码服务</li>
 *   <li><strong>异常处理</strong>：统一的验证失败处理机制</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>验证码服务管理：维护多个验证码服务实例</li>
 *   <li>请求匹配：根据请求特征选择合适的验证码服务</li>
 *   <li>验证执行：委托给具体的验证码服务进行验证</li>
 *   <li>异常处理：统一处理验证失败的情况</li>
 * </ul>
 *
 * <h3>工作流程：</h3>
 * <ol>
 *   <li>接收HTTP请求</li>
 *   <li>遍历所有注册的验证码服务</li>
 *   <li>找到支持当前请求的验证码服务</li>
 *   <li>执行验证码验证逻辑</li>
 *   <li>验证成功则继续过滤器链，失败则调用失败处理器</li>
 * </ol>
 *
 * <h3>配置示例：</h3>
 * <pre>{@code
 * @Bean
 * public VerifyCodeFilter verifyCodeFilter() {
 *     List<VerifyCodeService> services = Arrays.asList(
 *         new ImageVerifyCodeService(),
 *         new SmsVerifyCodeService(),
 *         new EmailVerifyCodeService()
 *     );
 *     VerifyCodeFilter filter = new VerifyCodeFilter(services);
 *     filter.setAuthenticationFailureHandler(authenticationFailureHandler());
 *     return filter;
 * }
 * }</pre>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>用户登录时的图形验证码验证</li>
 *   <li>短信登录时的验证码验证</li>
 *   <li>邮箱验证码登录验证</li>
 *   <li>敏感操作的二次验证</li>
 * </ul>
 *
 * @author Jin
 * @see VerifyCodeService
 * @see AuthenticationFailureHandler
 * @see GenericFilterBean
 */
public class VerifyCodeFilter extends GenericFilterBean implements InitializingBean {

    /**
     * 验证码服务列表
     * <p>
     * 存储所有注册的验证码服务实例。过滤器会遍历这个列表，
     * 找到支持当前请求的验证码服务进行处理。
     * </p>
     *
     * @see VerifyCodeService
     */
    private final List<VerifyCodeService> verifyCodeServices;

    /**
     * 认证失败处理器
     * <p>
     * 当验证码验证失败时，会调用此处理器来处理失败情况，
     * 通常会返回错误响应给客户端。
     * </p>
     *
     * @see AuthenticationFailureHandler
     */
    private AuthenticationFailureHandler failureHandler;

    /**
     * 构造验证码过滤器
     * <p>
     * 初始化验证码过滤器，设置验证码服务列表。如果传入的服务列表为空，
     * 则创建一个空的列表，确保过滤器能够正常工作。
     * </p>
     *
     * @param verifyCodeServices 验证码服务列表，可以为空
     */
    public VerifyCodeFilter(List<VerifyCodeService> verifyCodeServices) {
        if (CollectionUtils.isEmpty(verifyCodeServices)) {
            this.verifyCodeServices = new ArrayList<>();
        } else {
            List<VerifyCodeService> services = new ArrayList<>(verifyCodeServices);
            AnnotationAwareOrderComparator.sort(services);
            this.verifyCodeServices = services;
        }
    }

    /**
     * 设置认证失败处理器
     * <p>
     * 设置当验证码验证失败时的处理器。该处理器负责处理验证失败的情况，
     * 通常会向客户端返回相应的错误信息。
     * </p>
     *
     * @param failureHandler 认证失败处理器，不能为null
     * @throws IllegalArgumentException 当failureHandler为null时抛出
     *
     * @see AuthenticationFailureHandler
     */
    public void setAuthenticationFailureHandler(AuthenticationFailureHandler failureHandler) {
        Assert.notNull(failureHandler, "failureHandler cannot be null");
        this.failureHandler = failureHandler;
    }

    @Override
    public void afterPropertiesSet() {
        Assert.notNull(this.failureHandler, "failureHandler cannot be null");
    }

    /**
     * 执行验证码过滤逻辑
     * <p>
     * 该方法是过滤器的核心逻辑，负责根据请求特征选择合适的验证码服务进行验证。
     * 如果找到支持的服务则执行验证，否则直接放行到下一个过滤器。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>将ServletRequest和ServletResponse转换为HTTP类型</li>
     *   <li>遍历所有注册的验证码服务</li>
     *   <li>调用每个服务的support方法检查是否支持当前请求</li>
     *   <li>找到支持的服务后，执行其验证逻辑</li>
     *   <li>如果验证过程中出现异常，调用失败处理器</li>
     *   <li>如果没有找到支持的服务，直接放行到下一个过滤器</li>
     * </ol>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>捕获验证过程中的所有异常</li>
     *   <li>调用认证失败处理器处理异常</li>
     *   <li>阻止请求继续传递到下一个过滤器</li>
     * </ul>
     *
     * @param req 请求对象
     * @param res 响应对象
     * @param chain 过滤器链
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     *
     * @see VerifyCodeService#support(HttpServletRequest)
     * @see VerifyCodeService#execute(HttpServletRequest, HttpServletResponse, FilterChain)
     * @see AuthenticationFailureHandler#onAuthenticationFailure(HttpServletRequest, HttpServletResponse, Exception)
     */
    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        try {
            for (VerifyCodeService service : this.verifyCodeServices) {
                if (service.support(request)) {
                    service.execute(request, response, chain);
                    return;
                }
            }
        } catch (Exception exception) {
            this.failureHandler.onAuthenticationFailure(request, response, exception);
            return;
        }
        chain.doFilter(request, response);
    }
}
