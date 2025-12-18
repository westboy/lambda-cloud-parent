package com.lambda.security.web.verify.service;

import cn.hutool.json.JSONObject;
import com.lambda.cloud.mvc.WebHttpUtils;
import com.lambda.cloud.web.LambdaHttpServletRequestWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * 验证码服务接口
 *
 * <p>设计目标：
 * <ul>
 *   <li>统一接口：为不同类型的验证码提供统一的处理接口</li>
 *   <li>灵活扩展：支持图形验证码、短信验证码等多种验证方式</li>
 *   <li>链式处理：与过滤器链无缝集成，支持多级验证</li>
 *   <li>请求适配：提供请求包装和参数提取的通用方法</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>支持检测：判断当前请求是否需要验证码验证</li>
 *   <li>验证执行：执行具体的验证码校验逻辑</li>
 *   <li>请求包装：提供请求对象的包装和增强功能</li>
 *   <li>参数提取：统一处理表单和JSON格式的请求参数</li>
 * </ul>
 *
 * <p>验证流程：
 * <ol>
 *   <li>调用support()方法检查是否需要验证</li>
 *   <li>如果需要验证，调用execute()方法执行验证逻辑</li>
 *   <li>验证通过后继续过滤器链的执行</li>
 *   <li>验证失败时返回错误响应</li>
 * </ol>
 *
 * <p>实现示例：
 * <pre>{@code
 * @Component
 * public class CaptchaVerifyCodeService implements VerifyCodeService {
 *
 *     @Override
 *     public boolean support(HttpServletRequest request) {
 *         return "/login".equals(request.getRequestURI());
 *     }
 *
 *     @Override
 *     public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain) {
 *         // 验证图形验证码
 *         LambdaHttpServletRequestWrapper wrapper = getRequestWrapper(request);
 *         JSONObject params = getRequestParam(wrapper);
 *         String captcha = params.getStr("captcha");
 *
 *         if (validateCaptcha(captcha)) {
 *             chain.doFilter(wrapper, response);
 *         } else {
 *             response.setStatus(400);
 *             response.getWriter().write("验证码错误");
 *         }
 *     }
 * }
 * }</pre>
 *
 * <p>使用场景：
 * <ul>
 *   <li>登录验证：在用户登录时验证图形验证码</li>
 *   <li>注册验证：在用户注册时验证短信验证码</li>
 *   <li>敏感操作：在执行敏感操作前进行二次验证</li>
 *   <li>防刷保护：防止恶意请求和机器人攻击</li>
 * </ul>
 *
 * @author jpjoo
 * @see LambdaHttpServletRequestWrapper
 * @see WebHttpUtils
 */
public interface VerifyCodeService {

    /**
     * 检查当前请求是否需要验证码验证
     *
     * <p>支持检测逻辑：
     * <ul>
     *   <li>URL匹配：检查请求路径是否匹配验证规则</li>
     *   <li>方法检查：验证HTTP请求方法（GET、POST等）</li>
     *   <li>参数判断：根据请求参数决定是否需要验证</li>
     *   <li>条件过滤：基于业务条件进行动态判断</li>
     * </ul>
     *
     * <p>常见匹配规则：
     * <ul>
     *   <li>精确匹配："/login"、"/register"</li>
     *   <li>方法限制：仅对POST请求进行验证</li>
     *   <li>参数条件：存在特定参数时才验证</li>
     * </ul>
     *
     * <p>实现示例：
     * <pre>{@code
     * @Override
     * public boolean support(HttpServletRequest request) {
     *     String uri = request.getRequestURI();
     *     String method = request.getMethod();
     *
     *     // 仅对登录和注册的POST请求进行验证
     *     return "POST".equals(method) &&
     *            (uri.endsWith("/login") || uri.endsWith("/register"));
     * }
     * }</pre>
     *
     * <p>性能考虑：
     * <ul>
     *   <li>快速判断：使用简单的字符串比较</li>
     *   <li>缓存结果：对复杂判断逻辑进行缓存</li>
     *   <li>短路求值：优先检查最常见的条件</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含请求路径、方法、参数等信息
     * @return true表示需要验证码验证，false表示跳过验证
     */
    boolean support(HttpServletRequest request);

    /**
     * 执行验证码校验逻辑
     *
     * <p>执行流程：
     * <ol>
     *   <li>获取请求包装器：使用getRequestWrapper()包装原始请求</li>
     *   <li>提取请求参数：使用getRequestParam()获取验证码参数</li>
     *   <li>执行验证逻辑：调用具体的验证码校验方法</li>
     *   <li>处理验证结果：成功时继续链式调用，失败时返回错误</li>
     * </ol>
     *
     * <p>验证处理：
     * <ul>
     *   <li>参数提取：从请求中获取验证码相关参数</li>
     *   <li>格式验证：检查验证码格式是否正确</li>
     *   <li>有效性验证：验证验证码是否有效且未过期</li>
     *   <li>一次性验证：确保验证码只能使用一次</li>
     * </ul>
     *
     * <p>错误处理：
     * <ul>
     *   <li>参数缺失：返回400状态码和错误信息</li>
     *   <li>验证码错误：返回验证失败的响应</li>
     *   <li>验证码过期：提示用户重新获取验证码</li>
     *   <li>系统异常：记录日志并返回服务器错误</li>
     * </ul>
     *
     * <p>实现示例：
     * <pre>{@code
     * @Override
     * public void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
     *         throws IOException, ServletException {
     *     try {
     *         LambdaHttpServletRequestWrapper wrapper = getRequestWrapper(request);
     *         JSONObject params = getRequestParam(wrapper);
     *
     *         String code = params.getStr("verifyCode");
     *         String key = params.getStr("verifyKey");
     *
     *         if (validateCode(key, code)) {
     *             // 验证成功，继续处理
     *             chain.doFilter(wrapper, response);
     *         } else {
     *             // 验证失败，返回错误
     *             WebHttpUtils.writeErrorResponse(response, "验证码错误");
     *         }
     *     } catch (Exception e) {
     *         log.error("验证码校验异常", e);
     *         WebHttpUtils.writeErrorResponse(response, "验证失败");
     *     }
     * }
     * }</pre>
     *
     * <p>安全考虑：
     * <ul>
     *   <li>防重放：验证码使用后立即失效</li>
     *   <li>防暴力破解：限制验证失败次数</li>
     *   <li>时效性：设置合理的过期时间</li>
     *   <li>日志记录：记录验证失败的详细信息</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含验证码相关参数
     * @param response HTTP响应对象，用于返回验证结果
     * @param chain 过滤器链，验证成功后继续执行
     * @throws IOException 处理请求或响应时的IO异常
     * @throws ServletException 过滤器处理过程中的异常
     */
    void execute(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws IOException, ServletException;

    /**
     * 获取增强的请求包装器
     *
     * <p>包装功能：
     * <ul>
     *   <li>请求体缓存：支持多次读取请求体内容</li>
     *   <li>参数增强：提供更便捷的参数获取方法</li>
     *   <li>编码处理：自动处理字符编码问题</li>
     *   <li>格式支持：同时支持表单和JSON格式</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>参数提取：需要从请求体中提取验证码参数</li>
     *   <li>多次读取：过滤器和业务代码都需要读取请求体</li>
     *   <li>格式转换：统一处理不同格式的请求参数</li>
     *   <li>编码统一：确保参数的正确编码</li>
     * </ul>
     *
     * <p>包装优势：
     * <ul>
     *   <li>透明性：对业务代码完全透明</li>
     *   <li>兼容性：完全兼容原始HttpServletRequest接口</li>
     *   <li>性能：缓存机制避免重复解析</li>
     *   <li>安全性：防止请求体被意外修改</li>
     * </ul>
     *
     * <p>实现原理：
     * <ul>
     *   <li>装饰器模式：包装原始请求对象</li>
     *   <li>流缓存：将InputStream内容缓存到内存</li>
     *   <li>延迟解析：按需解析请求参数</li>
     *   <li>编码转换：统一使用UTF-8编码</li>
     * </ul>
     *
     * @param request 原始的HTTP请求对象
     * @return 增强的请求包装器，支持多次读取和参数提取
     * @see LambdaHttpServletRequestWrapper
     */
    default LambdaHttpServletRequestWrapper getRequestWrapper(HttpServletRequest request) {
        return new LambdaHttpServletRequestWrapper(request);
    }

    /**
     * 统一提取请求参数
     *
     * <p>参数来源：
     * <ul>
     *   <li>表单参数：application/x-www-form-urlencoded格式</li>
     *   <li>JSON参数：application/json格式的请求体</li>
     *   <li>URL参数：查询字符串中的参数</li>
     *   <li>混合参数：同时支持多种格式的参数</li>
     * </ul>
     *
     * <p>合并策略：
     * <ul>
     *   <li>优先级：JSON参数 > 表单参数 > URL参数</li>
     *   <li>覆盖规则：同名参数时，高优先级覆盖低优先级</li>
     *   <li>类型保持：保持原始参数的数据类型</li>
     *   <li>空值处理：自动过滤null和空字符串</li>
     * </ul>
     *
     * <p>处理流程：
     * <ol>
     *   <li>提取表单参数：使用WebHttpUtils.getFormRequest()</li>
     *   <li>提取JSON参数：使用WebHttpUtils.getRequestBody()</li>
     *   <li>参数合并：将表单参数合并到JSON参数中</li>
     *   <li>返回结果：转换为JSONObject格式</li>
     * </ol>
     *
     * <p>使用示例：
     * <pre>{@code
     * LambdaHttpServletRequestWrapper wrapper = getRequestWrapper(request);
     * JSONObject params = getRequestParam(wrapper);
     *
     * String verifyCode = params.getStr("verifyCode");
     * String mobile = params.getStr("mobile");
     * Integer type = params.getInt("type");
     * }</pre>
     *
     * <p>支持格式：
     * <ul>
     *   <li>表单提交：verifyCode=1234&mobile=13800138000</li>
     *   <li>JSON提交：{"verifyCode":"1234","mobile":"13800138000"}</li>
     *   <li>混合提交：URL参数 + JSON请求体</li>
     * </ul>
     *
     * <p>注意事项：
     * <ul>
     *   <li>编码处理：自动处理中文和特殊字符</li>
     *   <li>类型转换：支持字符串、数字、布尔值等类型</li>
     *   <li>安全性：防止参数注入和XSS攻击</li>
     *   <li>性能：缓存解析结果，避免重复处理</li>
     * </ul>
     *
     * @param request 增强的请求包装器，支持多次读取请求体
     * @return 包含所有请求参数的JSONObject，支持类型安全的参数获取
     * @see WebHttpUtils#getFormRequest (LambdaHttpServletRequestWrapper)
     * @see WebHttpUtils#getRequestBody (LambdaHttpServletRequestWrapper)
     */
    default JSONObject getRequestParam(LambdaHttpServletRequestWrapper request) {
        Map<String, Object> formRequest = WebHttpUtils.getFormRequest(request);
        Map<String, Object> ajaxRequest = WebHttpUtils.getRequestBody(request);
        ajaxRequest.putAll(formRequest);
        return (JSONObject) ajaxRequest;
    }
}
