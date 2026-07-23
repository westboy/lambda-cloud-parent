package com.lambda.security.web.xss;

import cn.hutool.core.util.ArrayUtil;
import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.web.LambdaServletInputStream;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.errors.IntrusionException;

/**
 * XSS请求包装器
 * <p>
 * 该类继承自HttpServletRequestWrapper，用于包装原始的HTTP请求对象，
 * 对请求中的参数、请求头和请求体进行XSS过滤和清理，防止恶意脚本注入攻击。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>全面防护</strong>：对请求参数、请求头、请求体进行全面的XSS过滤</li>
 *   <li><strong>透明处理</strong>：对应用代码透明，无需修改现有业务逻辑</li>
 *   <li><strong>安全清理</strong>：使用OWASP ESAPI和Jsoup进行安全的内容清理</li>
 *   <li><strong>性能优化</strong>：只对非空内容进行处理，提高处理效率</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>参数过滤：过滤请求参数中的恶意脚本</li>
 *   <li>请求头过滤：清理请求头中的XSS内容</li>
 *   <li>请求体过滤：处理请求体中的恶意代码</li>
 *   <li>编码规范化：使用ESAPI进行内容规范化</li>
 *   <li>HTML清理：使用Jsoup移除所有HTML标签</li>
 * </ul>
 *
 * <h3>过滤策略：</h3>
 * <ol>
 *   <li>使用ESAPI进行内容规范化，处理各种编码形式</li>
 *   <li>移除空字符(\0)防止截断攻击</li>
 *   <li>使用Jsoup的Safelist.none()移除所有HTML标签</li>
 *   <li>检测到入侵尝试时返回空字符串并记录日志</li>
 * </ol>
 *
 * <h3>技术实现：</h3>
 * <ul>
 *   <li><strong>OWASP ESAPI</strong>：用于内容规范化和入侵检测</li>
 *   <li><strong>Jsoup</strong>：用于HTML内容清理和标签移除</li>
 *   <li><strong>LambdaServletInputStream</strong>：用于包装处理后的请求体</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>Web表单提交的XSS防护</li>
 *   <li>API接口参数的安全过滤</li>
 *   <li>文件上传时的元数据清理</li>
 *   <li>富文本内容的安全处理</li>
 * </ul>
 *
 * @author Jin
 * @see HttpServletRequestWrapper
 * @see org.owasp.esapi.ESAPI
 * @see org.jsoup.Jsoup
 * @see com.lambda.cloud.web.LambdaServletInputStream
 */
@Slf4j
public class XSSRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 构造XSS请求包装器
     * <p>
     * 使用原始的HttpServletRequest对象创建XSS防护的请求包装器。
     * 包装器会拦截所有对请求参数、请求头和请求体的访问，
     * 并对内容进行XSS过滤处理。
     * </p>
     *
     * @param request 原始的HTTP请求对象
     */
    public XSSRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    /**
     * 获取经过XSS过滤的请求参数值数组
     * <p>
     * 重写父类方法，对请求参数的所有值进行XSS过滤处理。
     * 当参数有多个值时（如复选框），会对每个值都进行安全编码。
     * </p>
     *
     * @param parameter 参数名称
     * @return 经过XSS过滤的参数值数组，如果参数不存在则返回null
     *
     * @see #encode(String)
     */
    @Override
    public String[] getParameterValues(String parameter) {
        String[] values = super.getParameterValues(parameter);
        if (ArrayUtil.isNotEmpty(values)) {
            return Arrays.stream(values).map(this::encode).toArray(String[]::new);
        }
        return values;
    }

    /**
     * 获取经过XSS过滤的请求参数值
     * <p>
     * 重写父类方法，对单个请求参数值进行XSS过滤处理。
     * 这是最常用的参数获取方法，会自动清理参数中的恶意脚本。
     * </p>
     *
     * @param parameter 参数名称
     * @return 经过XSS过滤的参数值，如果参数不存在则返回null
     *
     * @see #encode(String)
     */
    @Override
    public String getParameter(String parameter) {
        String value = super.getParameter(parameter);
        return encode(value);
    }

    /**
     * 获取经过XSS过滤的请求头值集合
     * <p>
     * 重写父类方法，对指定名称的所有请求头值进行XSS过滤处理。
     * 支持处理包含多个值的请求头（用逗号分隔），每个值都会被单独过滤。
     * </p>
     *
     * <h3>处理逻辑：</h3>
     * <ol>
     *   <li>获取指定名称的所有请求头</li>
     *   <li>按逗号分割每个请求头值</li>
     *   <li>对每个分割后的值进行XSS编码</li>
     *   <li>返回过滤后的值集合</li>
     * </ol>
     *
     * @param name 请求头名称
     * @return 经过XSS过滤的请求头值枚举
     *
     * @see #encode(String)
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> result = new ArrayList<>();
        Enumeration<String> headers = super.getHeaders(name);
        while (headers.hasMoreElements()) {
            String header = headers.nextElement();
            String[] values = header.split(",");
            for (String value : values) {
                result.add(encode(value));
            }
        }
        return Collections.enumeration(result);
    }

    /**
     * 获取经过XSS过滤的请求体读取器
     * <p>
     * 重写父类方法，读取请求体内容并进行XSS过滤处理。
     * 适用于处理文本格式的请求体，如JSON、XML等。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>读取原始请求体的全部内容</li>
     *   <li>对内容进行XSS编码处理</li>
     *   <li>将处理后的内容包装为BufferedReader返回</li>
     * </ol>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>该方法会读取整个请求体到内存中</li>
     *   <li>适用于文本内容，不适用于二进制文件</li>
     *   <li>与getInputStream()方法互斥，只能调用其中一个</li>
     * </ul>
     *
     * @return 包含过滤后内容的BufferedReader
     * @throws IOException 当读取请求体失败时抛出
     *
     * @see #encode(String)
     */
    @Override
    public BufferedReader getReader() throws IOException {
        String body = IOUtils.toString(super.getReader());
        return new BufferedReader(new StringReader(encode(body)));
    }

    /**
     * 获取经过XSS过滤的请求体输入流
     * <p>
     * 重写父类方法，读取请求体内容并进行XSS过滤处理后返回输入流。
     * 使用UTF-8编码读取内容，适用于大多数文本格式的请求体。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li>使用UTF-8编码读取原始请求体</li>
     *   <li>对读取的内容进行XSS编码处理</li>
     *   <li>将处理后的内容包装为LambdaServletInputStream返回</li>
     * </ol>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>该方法会读取整个请求体到内存中</li>
     *   <li>使用UTF-8编码，适用于文本内容</li>
     *   <li>与getReader()方法互斥，只能调用其中一个</li>
     *   <li>返回的是自定义的LambdaServletInputStream实现</li>
     * </ul>
     *
     * @return 包含过滤后内容的ServletInputStream
     * @throws IOException 当读取请求体失败时抛出
     *
     * @see #encode(String)
     * @see com.lambda.cloud.web.LambdaServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() throws IOException {
        String body = IOUtils.toString(super.getInputStream(), StandardCharsets.UTF_8);
        return new LambdaServletInputStream(encode(body));
    }

    /**
     * 对字符串进行XSS编码和清理
     * <p>
     * 该方法是XSS防护的核心实现，使用OWASP ESAPI和Jsoup对输入内容进行
     * 规范化处理和HTML标签清理，有效防止XSS攻击。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li><strong>空值检查</strong>：如果输入为空或空白，直接返回原值</li>
     *   <li><strong>内容规范化</strong>：使用ESAPI进行字符编码规范化</li>
     *   <li><strong>空字符移除</strong>：移除空字符(\0)防止截断攻击</li>
     *   <li><strong>HTML清理</strong>：使用Jsoup移除所有HTML标签和属性</li>
     *   <li><strong>入侵检测</strong>：捕获入侵异常并记录日志</li>
     * </ol>
     *
     * <h3>安全策略：</h3>
     * <ul>
     *   <li><strong>ESAPI规范化</strong>：处理各种编码形式的恶意输入</li>
     *   <li><strong>Safelist.none()</strong>：移除所有HTML标签，最严格的清理策略</li>
     *   <li><strong>入侵检测</strong>：检测到明显的攻击尝试时返回空字符串</li>
     *   <li><strong>日志记录</strong>：记录可疑请求的URI，便于排查和配置</li>
     * </ul>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>捕获IntrusionException入侵异常</li>
     *   <li>记录包含配置建议的日志信息</li>
     *   <li>返回空字符串确保安全</li>
     * </ul>
     *
     * <h3>配置建议：</h3>
     * <p>当检测到入侵尝试时，如果确认请求是可信的，可以将对应的URI
     * 添加到XSS防护的信任路径列表中：</p>
     * <pre>{@code
     * spring:
     *   security:
     *     xss-protected:
     *       trusted:
     *         - "/api/upload/**"
     *         - "/trusted/path/**"
     * }</pre>
     *
     * @param value 需要进行XSS编码的字符串
     * @return 经过XSS过滤和清理的安全字符串
     *         如果输入为空则返回原值，如果检测到入侵则返回空字符串
     *
     * @see org.owasp.esapi.ESAPI
     * @see org.jsoup.Jsoup#clean(String, Safelist)
     * @see org.jsoup.safety.Safelist#none()
     * @see org.owasp.esapi.errors.IntrusionException
     */
    public String encode(String value) {
        if (StrUtil.isBlank(value)) {
            return value;
        }
        try {
            value = ESAPI.encoder().canonicalize(value).replace("\0", StrUtil.EMPTY);
            return Jsoup.clean(value, Safelist.none());
        } catch (IntrusionException e) {
            log.info(
                    "If you are sure to trust the request, add the following:\nspring:\n  security:\n    xss-protected:\n      trusted: {}",
                    this.getRequestURI());
            return StrUtil.EMPTY;
        }
    }
}
