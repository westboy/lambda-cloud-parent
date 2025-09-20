package com.lambda.security.web.hmac.utils;

import static com.lambda.cloud.mvc.WebHttpUtils.AUTHORIZATION;

import com.lambda.cloud.core.utils.HmacGenerator;
import com.lambda.security.web.hmac.model.HmacAuthorization;
import com.lambda.security.web.hmac.wrapper.HmacRequestWrapper;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * HMAC认证工具类
 * <p>
 * 提供HMAC（Hash-based Message Authentication Code）认证相关的工具方法，
 * 用于解析、验证和生成HMAC签名。支持基于时间戳和请求内容的签名验证，
 * 确保API请求的完整性和真实性。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>安全性</strong> - 提供强大的消息认证机制</li>
 *   <li><strong>完整性</strong> - 确保请求内容未被篡改</li>
 *   <li><strong>防重放</strong> - 基于时间戳防止重放攻击</li>
 *   <li><strong>标准化</strong> - 遵循HMAC标准规范</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>授权解析</strong> - 从Authorization头中解析HMAC信息</li>
 *   <li><strong>签名生成</strong> - 生成请求的HMAC签名值</li>
 *   <li><strong>模式匹配</strong> - 验证Authorization头格式</li>
 *   <li><strong>盐值计算</strong> - 计算用于签名的盐值</li>
 * </ul>
 *
 * <h3>Authorization头格式：</h3>
 * <pre>{@code
 * Authorization: HMAC appid:timestamp:digest
 * }</pre>
 *
 * <h3>签名算法：</h3>
 * <ol>
 *   <li>构建签名字符串：METHOD + URI + QUERY + BODY + TIMESTAMP</li>
 *   <li>使用客户端密钥进行HMAC-SHA256签名</li>
 *   <li>将签名结果进行Base64编码</li>
 *   <li>组装Authorization头</li>
 * </ol>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 解析Authorization头
 * HmacAuthorization auth = HmacUtils.getHmacAuthorization(request);
 * if (auth != null) {
 *     String appid = auth.getAppid();
 *     String timestamp = auth.getTimestamp();
 *     String digest = auth.getDigest();
 * }
 *
 * // 生成签名盐值
 * String saltValue = HmacUtils.getHmacSaltValue(request, appid, timestamp);
 * }</pre>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li><strong>时间窗口</strong> - 验证时间戳防止重放攻击</li>
 *   <li><strong>密钥管理</strong> - 客户端密钥需要安全存储</li>
 *   <li><strong>传输安全</strong> - 建议使用HTTPS传输</li>
 *   <li><strong>签名验证</strong> - 服务端必须验证签名的正确性</li>
 * </ul>
 *
 * @author jpjoo
 * @see HmacAuthorization
 * @see HmacRequestWrapper
 * @see HmacGenerator
 * @since 1.0.0
 */
@Slf4j
public final class HmacUtils {

    /**
     * 私有构造方法，防止实例化
     * <p>
     * 该类是工具类，所有方法都是静态的，不需要实例化。
     * </p>
     */
    private HmacUtils() {}

    /**
     * Authorization头的正则表达式模式
     * <p>
     * 用于解析HMAC Authorization头的格式："HMAC appid:timestamp:digest"
     * </p>
     *
     * <h3>模式说明：</h3>
     * <ul>
     *   <li>第一组：认证类型（如"HMAC"）</li>
     *   <li>第二组：应用ID（appid）</li>
     *   <li>第三组：时间戳（timestamp）</li>
     *   <li>第四组：签名摘要（digest）</li>
     * </ul>
     */
    private static final String PATTERN = "^(\\w+) (\\S+):(\\S+):(\\S+)$";

    /**
     * 从HTTP请求中解析HMAC授权信息
     * <p>
     * 从请求的Authorization头中提取HMAC认证所需的信息，包括应用ID、
     * 时间戳和签名摘要。如果Authorization头格式不正确，返回null。
     * </p>
     *
     * <h3>解析流程：</h3>
     * <ol>
     *   <li>获取Authorization头内容</li>
     *   <li>使用正则表达式进行模式匹配</li>
     *   <li>提取各个组件（appid、timestamp、digest）</li>
     *   <li>构建HmacAuthorization对象</li>
     * </ol>
     *
     * <h3>期望的Authorization头格式：</h3>
     * <pre>{@code
     * Authorization: HMAC myapp:1234567890:base64encodeddigest
     * }</pre>
     *
     * <h3>返回值说明：</h3>
     * <ul>
     *   <li>成功：返回包含解析信息的HmacAuthorization对象</li>
     *   <li>失败：返回null（格式不正确或头不存在）</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含Authorization头
     * @return 解析成功的HMAC授权信息，失败时返回null
     * @see HmacAuthorization
     */
    public static HmacAuthorization getHmacAuthorization(HttpServletRequest request) {
        final Matcher matcher = matcher(request);

        if (matcher.matches()) {
            HmacAuthorization authorization = new HmacAuthorization();
            authorization.setAppid(matcher.group(2));
            authorization.setTimestamp(matcher.group(3));
            authorization.setDigest(matcher.group(4));
            return authorization;
        }
        return null;
    }

    /**
     * 获取Authorization头的正则匹配器
     * <p>
     * 从HTTP请求中获取Authorization头，并使用预定义的正则表达式模式
     * 创建匹配器，用于后续的信息提取。
     * </p>
     *
     * <h3>处理步骤：</h3>
     * <ol>
     *   <li>从请求头中获取Authorization值</li>
     *   <li>编译预定义的正则表达式模式</li>
     *   <li>创建并返回匹配器对象</li>
     * </ol>
     *
     * <h3>注意事项：</h3>
     * <ul>
     *   <li>如果Authorization头不存在，匹配器将不会匹配任何内容</li>
     *   <li>调用者需要检查匹配器的matches()方法结果</li>
     *   <li>匹配成功后可以通过group()方法获取各个组件</li>
     * </ul>
     *
     * @param request HTTP请求对象，包含Authorization头
     * @return 正则表达式匹配器，用于提取HMAC信息
     */
    public static Matcher matcher(HttpServletRequest request) {
        final String header = request.getHeader(AUTHORIZATION);
        final Pattern pattern = Pattern.compile(PATTERN);
        return pattern.matcher(header);
    }

    /**
     * 生成HMAC签名的盐值字符串
     * <p>
     * 根据HMAC签名算法的要求，将请求的各个组件（应用ID、时间戳、查询参数、
     * 请求体）组合成用于签名的基础字符串。这个字符串将作为HMAC算法的输入。
     * </p>
     *
     * <h3>盐值组成：</h3>
     * <ul>
     *   <li>应用ID（appid）</li>
     *   <li>时间戳（timestamp）</li>
     *   <li>查询参数（按字典序排列）</li>
     *   <li>请求体内容（如果存在）</li>
     * </ul>
     *
     * <h3>生成规则：</h3>
     * <ol>
     *   <li>提取请求的查询参数Map</li>
     *   <li>检查请求是否包含请求体</li>
     *   <li>如果有请求体，获取请求体内容</li>
     *   <li>调用HmacGenerator生成基础字符串</li>
     * </ol>
     *
     * <h3>请求体处理：</h3>
     * <ul>
     *   <li>只有POST和PUT请求才考虑请求体</li>
     *   <li>请求体内容不能为空</li>
     *   <li>请求体内容会被包含在签名计算中</li>
     * </ul>
     *
     * @param request HMAC请求包装器，提供请求体和参数访问
     * @param appid 应用ID，用于标识客户端
     * @param timestamp 时间戳字符串，用于防重放攻击
     * @return 用于HMAC签名的盐值字符串
     * @see HmacGenerator#baseString(String, long, Map, String)
     */
    public static String getHmacSaltValue(final HmacRequestWrapper request, String appid, String timestamp) {
        Map<String, String[]> queries = request.getParameterMap();
        String body = null;
        if (hasBody(request)) {
            body = request.getBody();
        }
        return HmacGenerator.baseString(appid, Long.parseLong(timestamp), queries, body);
    }

    /**
     * 检查HTTP请求是否包含有效的请求体
     * <p>
     * 判断当前请求是否包含需要参与HMAC签名计算的请求体内容。
     * 只有特定的HTTP方法（POST、PUT）且请求体非空时才返回true。
     * </p>
     *
     * <h3>判断条件：</h3>
     * <ul>
     *   <li>请求体内容不为空（非null且非空白字符串）</li>
     *   <li>HTTP方法为POST或PUT</li>
     * </ul>
     *
     * <h3>设计原理：</h3>
     * <ul>
     *   <li>GET、DELETE等方法通常不包含请求体</li>
     *   <li>POST、PUT方法可能包含需要签名的数据</li>
     *   <li>空的请求体不参与签名计算</li>
     *   <li>确保签名的一致性和安全性</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>API请求签名验证</li>
     *   <li>数据完整性检查</li>
     *   <li>防篡改验证</li>
     * </ul>
     *
     * @param request HMAC请求包装器，提供请求体和方法访问
     * @return true表示请求包含有效的请求体，false表示无请求体或不需要签名
     */
    private static boolean hasBody(HmacRequestWrapper request) {
        String body = request.getBody();
        RequestMethod method = RequestMethod.valueOf(request.getMethod());
        return StringUtils.isNotBlank(body) && (RequestMethod.POST.equals(method) || RequestMethod.PUT.equals(method));
    }
}
