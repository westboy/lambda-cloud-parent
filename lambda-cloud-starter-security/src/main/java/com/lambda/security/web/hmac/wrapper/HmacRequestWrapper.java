package com.lambda.security.web.hmac.wrapper;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import lombok.Getter;
import org.springframework.util.StreamUtils;

/**
 * HMAC请求包装器
 * <p>
 * 该类继承自HttpServletRequestWrapper，专门用于HMAC认证场景下的HTTP请求包装。
 * 主要功能是缓存请求体内容以支持多次读取，并提供动态添加请求头的能力，
 * 这对于HMAC签名验证过程中需要重复访问请求体和动态设置认证信息非常重要。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>请求体缓存</strong>：解决ServletInputStream只能读取一次的限制</li>
 *   <li><strong>动态请求头</strong>：支持在处理过程中动态添加请求头信息</li>
 *   <li><strong>HMAC支持</strong>：为HMAC签名验证提供必要的请求数据访问</li>
 *   <li><strong>透明包装</strong>：对上层应用透明，不影响正常的请求处理流程</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>请求体缓存：将请求体内容读取并缓存到内存中</li>
 *   <li>多次读取：支持对请求体进行多次读取操作</li>
 *   <li>请求头管理：支持动态添加和获取请求头</li>
 *   <li>编码处理：统一使用UTF-8编码处理请求内容</li>
 * </ul>
 *
 * <h3>使用场景：</h3>
 * <ul>
 *   <li>HMAC签名验证：需要多次读取请求体计算签名</li>
 *   <li>请求日志记录：需要记录完整的请求内容</li>
 *   <li>请求重放：需要保存请求内容用于重放</li>
 *   <li>动态认证：需要在处理过程中添加认证信息</li>
 * </ul>
 *
 * <h3>技术实现：</h3>
 * <ul>
 *   <li><strong>请求体缓存</strong>：使用ByteArrayOutputStream缓存请求体</li>
 *   <li><strong>UTF-8编码</strong>：统一使用UTF-8编码处理文本内容</li>
 *   <li><strong>内存管理</strong>：将请求体完全加载到内存中</li>
 *   <li><strong>流包装</strong>：提供自定义的ServletInputStream实现</li>
 * </ul>
 *
 * <h3>注意事项：</h3>
 * <ul>
 *   <li>会将整个请求体加载到内存中，不适用于大文件上传</li>
 *   <li>仅适用于文本内容，二进制内容可能出现编码问题</li>
 *   <li>增加了内存消耗，需要考虑并发请求的内存使用</li>
 * </ul>
 *
 * @author jpjoo
 * @see HttpServletRequestWrapper
 * @see ServletInputStream
 * @see com.lambda.security.web.hmac.HmacAuthenticationProcessingFilter
 */
@Getter
public final class HmacRequestWrapper extends HttpServletRequestWrapper {

    /**
     * 缓存的请求体内容
     * <p>
     * 将原始请求的InputStream内容读取并转换为字符串缓存。
     * 使用UTF-8编码确保中文等多字节字符的正确处理。
     * </p>
     */
    private final String body;

    /**
     * HMAC相关的请求头映射
     * <p>
     * 存储在HMAC认证过程中动态添加的请求头信息。
     * 这些请求头会与原始请求头合并，优先返回动态添加的值。
     * </p>
     *
     * <h3>常见的HMAC请求头：</h3>
     * <ul>
     *   <li>Authorization - HMAC认证信息</li>
     *   <li>X-Timestamp - 请求时间戳</li>
     *   <li>X-Nonce - 随机数</li>
     *   <li>X-App-Id - 应用标识</li>
     * </ul>
     */
    @SuppressFBWarnings("EI_EXPOSE_REP")
    private final Map<String, String> hmacHeaders = new HashMap<>();

    /**
     * 构造HMAC请求包装器
     * <p>
     * 创建HMAC请求包装器实例，读取并缓存原始请求的输入流内容。
     * 这个过程会消耗原始请求的InputStream，因此后续对请求体的访问
     * 都将通过缓存的内容提供。
     * </p>
     *
     * <h3>初始化过程：</h3>
     * <ol>
     *   <li>调用父类构造函数包装原始请求</li>
     *   <li>创建ByteArrayOutputStream用于缓存</li>
     *   <li>读取原始请求的InputStream</li>
     *   <li>将内容复制到输出流中</li>
     *   <li>使用UTF-8编码转换为字符串</li>
     * </ol>
     *
     * @param request 原始的HTTP请求对象
     * @throws IOException 当读取请求输入流失败时抛出
     *
     * @see StreamUtils#copy(InputStream, OutputStream)
     */
    public HmacRequestWrapper(HttpServletRequest request) throws IOException {
        super(request);
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        ServletInputStream input = request.getInputStream();
        StreamUtils.copy(input, output);
        this.body = output.toString(StandardCharsets.UTF_8);
    }

    /**
     * 动态添加请求头
     * <p>
     * 在HMAC认证处理过程中动态添加请求头信息。
     * 这些请求头会覆盖原始请求中的同名请求头。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>添加解析后的认证信息</li>
     *   <li>设置用户身份标识</li>
     *   <li>添加处理过程中的元数据</li>
     *   <li>传递认证结果给下游处理器</li>
     * </ul>
     *
     * @param name 请求头名称
     * @param value 请求头值
     */
    public void addHeader(String name, String value) {
        hmacHeaders.put(name, value);
    }

    /**
     * 获取请求头值
     * <p>
     * 重写父类方法，优先返回动态添加的请求头值，
     * 如果不存在则返回原始请求中的请求头值。
     * </p>
     *
     * <h3>查找顺序：</h3>
     * <ol>
     *   <li>首先查找动态添加的HMAC请求头</li>
     *   <li>如果未找到，则查找原始请求头</li>
     *   <li>都未找到则返回null</li>
     * </ol>
     *
     * @param name 请求头名称
     * @return 请求头值，如果不存在则返回null
     */
    @Override
    public String getHeader(String name) {
        String headerValue = hmacHeaders.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return super.getHeader(name);
    }

    /**
     * 获取所有请求头名称
     * <p>
     * 重写父类方法，返回包含动态添加的请求头和原始请求头的
     * 所有请求头名称集合。使用Set确保名称不重复。
     * </p>
     *
     * <h3>合并逻辑：</h3>
     * <ol>
     *   <li>创建Set集合存储所有请求头名称</li>
     *   <li>添加所有动态添加的请求头名称</li>
     *   <li>添加所有原始请求头名称</li>
     *   <li>Set自动去重，确保名称唯一</li>
     * </ol>
     *
     * @return 包含所有请求头名称的枚举
     */
    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> headerNames = new HashSet<>(hmacHeaders.keySet());
        Enumeration<String> originalHeaders = super.getHeaderNames();
        while (originalHeaders.hasMoreElements()) {
            headerNames.add(originalHeaders.nextElement());
        }
        return Collections.enumeration(headerNames);
    }

    /**
     * 获取指定名称的所有请求头值
     * <p>
     * 重写父类方法，返回指定名称的所有请求头值，
     * 包括动态添加的和原始请求中的值。
     * </p>
     *
     * <h3>合并策略：</h3>
     * <ol>
     *   <li>如果存在动态添加的请求头，优先添加</li>
     *   <li>然后添加原始请求中的同名请求头</li>
     *   <li>保持添加顺序，动态请求头在前</li>
     * </ol>
     *
     * @param name 请求头名称
     * @return 包含所有同名请求头值的枚举
     */
    @Override
    public Enumeration<String> getHeaders(String name) {
        List<String> headers = new ArrayList<>();
        if (hmacHeaders.containsKey(name)) {
            headers.add(hmacHeaders.get(name));
        }
        Enumeration<String> originalHeaders = super.getHeaders(name);
        while (originalHeaders.hasMoreElements()) {
            headers.add(originalHeaders.nextElement());
        }
        return Collections.enumeration(headers);
    }

    /**
     * 获取请求体输入流
     * <p>
     * 重写父类方法，返回基于缓存请求体内容的输入流。
     * 这允许多次读取请求体内容，解决了原始InputStream只能读取一次的限制。
     * </p>
     *
     * <h3>实现原理：</h3>
     * <ol>
     *   <li>将缓存的请求体字符串转换为字节数组</li>
     *   <li>使用ByteArrayInputStream包装字节数组</li>
     *   <li>创建自定义ServletInputStream实现</li>
     *   <li>委托给ByteArrayInputStream处理读取操作</li>
     * </ol>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>isFinished()和isReady()方法返回固定值</li>
     *   <li>不支持异步读取监听器</li>
     *   <li>适用于同步读取场景</li>
     * </ul>
     *
     * @return 基于缓存内容的ServletInputStream
     */
    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream =
                new ByteArrayInputStream(body.getBytes((StandardCharsets.UTF_8)));
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener readListener) {
                throw new UnsupportedOperationException();
            }
        };
    }

    /**
     * 获取请求体读取器
     * <p>
     * 重写父类方法，返回基于缓存请求体内容的BufferedReader。
     * 使用UTF-8编码确保正确处理中文等多字节字符。
     * </p>
     *
     * <h3>实现方式：</h3>
     * <ol>
     *   <li>调用getInputStream()获取输入流</li>
     *   <li>使用InputStreamReader包装，指定UTF-8编码</li>
     *   <li>使用BufferedReader包装，提供缓冲读取功能</li>
     * </ol>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>按行读取请求体内容</li>
     *   <li>处理文本格式的请求数据</li>
     *   <li>解析JSON、XML等文本协议</li>
     * </ul>
     *
     * @return 基于缓存内容的BufferedReader
     * @see #getInputStream()
     */
    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream(), StandardCharsets.UTF_8));
    }
}
