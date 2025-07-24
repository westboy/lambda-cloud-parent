package com.lambda.security.web.xss;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Set;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.util.AntPathMatcher;

/**
 * XSS防护过滤器
 * <p>
 * 该过滤器用于防止跨站脚本攻击(XSS)，通过包装HTTP请求对象来过滤和清理用户输入中的恶意脚本。
 * 过滤器会对所有请求参数、请求头和请求体进行XSS检查和清理，但可以配置信任的URL路径跳过检查。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>XSS防护</strong>：防止恶意脚本注入和执行</li>
 *   <li><strong>全面覆盖</strong>：对请求参数、请求头、请求体进行全面检查</li>
 *   <li><strong>灵活配置</strong>：支持配置信任的URL路径跳过检查</li>
 *   <li><strong>高性能</strong>：使用高优先级确保在其他过滤器之前执行</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>请求包装：使用XSSRequestWrapper包装原始请求</li>
 *   <li>路径匹配：支持Ant风格的路径模式匹配</li>
 *   <li>信任路径：配置的信任路径跳过XSS检查</li>
 *   <li>脚本清理：自动清理和转义恶意脚本内容</li>
 * </ul>
 *
 * <h3>工作原理：</h3>
 * <ol>
 *   <li>检查请求URI是否匹配信任路径列表</li>
 *   <li>如果匹配信任路径，直接放行原始请求</li>
 *   <li>否则使用XSSRequestWrapper包装请求进行XSS过滤</li>
 *   <li>继续执行过滤器链</li>
 * </ol>
 *
 * <h3>配置示例：</h3>
 * <pre>{@code
 * @Bean
 * public XSSDefendFilter xssDefendFilter() {
 *     Set<String> trustedPaths = Set.of(
 *         "/api/upload/**",
 *         "/static/**",
 *         "/actuator/**"
 *     );
 *     return new XSSDefendFilter(trustedPaths);
 * }
 * }</pre>
 *
 * <h3>信任路径配置：</h3>
 * <ul>
 *   <li>支持Ant风格路径模式：/api/**, /static/*, /upload/file.txt</li>
 *   <li>文件上传接口通常需要加入信任列表</li>
 *   <li>静态资源路径可以加入信任列表提高性能</li>
 *   <li>监控端点等系统接口可以加入信任列表</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>Web应用的XSS攻击防护</li>
 *   <li>用户输入内容的安全过滤</li>
 *   <li>API接口的安全防护</li>
 *   <li>富文本编辑器内容的安全处理</li>
 * </ul>
 *
 * @author Jin
 * @see XSSRequestWrapper
 * @see Filter
 * @see AntPathMatcher
 */
@Order(Ordered.HIGHEST_PRECEDENCE)
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class XSSDefendFilter implements Filter {
    /**
     * Ant路径匹配器
     * <p>
     * 用于匹配请求URI与信任路径模式。支持Ant风格的通配符模式，
     * 如 /api/**, /static/*, /upload/file.txt 等。
     * </p>
     *
     * @see AntPathMatcher
     */
    private static final AntPathMatcher ANT_PATH_MATCHER = new AntPathMatcher();

    /**
     * 信任的URL路径集合
     * <p>
     * 包含不需要进行XSS检查的URL路径模式。匹配这些模式的请求
     * 将跳过XSS过滤，直接使用原始请求对象。
     * </p>
     *
     * <h3>常见的信任路径：</h3>
     * <ul>
     *   <li>/api/upload/** - 文件上传接口</li>
     *   <li>/static/** - 静态资源</li>
     *   <li>/actuator/** - 监控端点</li>
     *   <li>/swagger-ui/** - API文档</li>
     * </ul>
     */
    private final Set<String> trusted;

    /**
     * 构造XSS防护过滤器
     * <p>
     * 初始化XSS防护过滤器，设置信任的URL路径集合。同时配置OWASP ESAPI
     * 的日志设置，禁用特殊字符的日志记录以提高性能。
     * </p>
     *
     * <h3>初始化操作：</h3>
     * <ul>
     *   <li>设置OWASP ESAPI日志配置</li>
     *   <li>保存信任路径集合</li>
     *   <li>准备路径匹配器</li>
     * </ul>
     *
     * @param trusted 信任的URL路径集合，支持Ant风格的路径模式
     *                如果为null或空集合，则所有请求都会进行XSS检查
     *
     * @see AntPathMatcher
     */
    public XSSDefendFilter(Set<String> trusted) {
        System.setProperty("org.owasp.esapi.logSpecial.discard", "true");
        this.trusted = trusted;
    }

    /**
     * 执行XSS防护过滤逻辑
     * <p>
     * 该方法是过滤器的核心逻辑，负责判断请求是否需要进行XSS检查，
     * 并根据判断结果选择使用原始请求或包装后的请求继续处理。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>将ServletRequest转换为HttpServletRequest</li>
     *   <li>获取请求的URI路径</li>
     *   <li>检查URI是否匹配任何信任路径模式</li>
     *   <li>如果匹配信任路径，使用原始请求继续过滤器链</li>
     *   <li>否则使用XSSRequestWrapper包装请求后继续过滤器链</li>
     * </ol>
     *
     * <h3>路径匹配逻辑：</h3>
     * <ul>
     *   <li>使用Ant风格路径匹配器进行模式匹配</li>
     *   <li>支持通配符：* 匹配单层路径，** 匹配多层路径</li>
     *   <li>匹配成功的请求跳过XSS检查</li>
     * </ul>
     *
     * <h3>XSS包装处理：</h3>
     * <ul>
     *   <li>使用XSSRequestWrapper包装原始请求</li>
     *   <li>自动过滤请求参数中的恶意脚本</li>
     *   <li>清理请求头和请求体中的XSS内容</li>
     * </ul>
     *
     * @param request 原始请求对象
     * @param response 响应对象
     * @param chain 过滤器链
     * @throws IOException 当I/O操作失败时抛出
     * @throws ServletException 当Servlet处理失败时抛出
     *
     * @see XSSRequestWrapper
     * @see AntPathMatcher#match(String, String)
     */
    @Override
    @SuppressWarnings("squid:S1874")
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (trusted.stream().anyMatch(item -> ANT_PATH_MATCHER.match(item, httpServletRequest.getRequestURI()))) {
            chain.doFilter(request, response);
            return;
        }
        chain.doFilter(new XSSRequestWrapper(httpServletRequest), response);
    }
}
