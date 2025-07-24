package com.lambda.security.web.hmac.model;

import lombok.Getter;
import lombok.Setter;

/**
 * HMAC授权信息模型
 * <p>
 * 封装HMAC认证过程中的授权信息，包含应用标识、时间戳和数字摘要。
 * 该模型用于解析和存储从HTTP请求Authorization头中提取的HMAC认证参数，
 * 是HMAC认证流程中的核心数据结构。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>数据封装</strong> - 封装HMAC认证的核心参数</li>
 *   <li><strong>类型安全</strong> - 提供强类型的数据访问</li>
 *   <li><strong>简化解析</strong> - 简化Authorization头的解析过程</li>
 *   <li><strong>标准化</strong> - 统一HMAC认证参数的表示方式</li>
 * </ul>
 *
 * <h3>HMAC认证流程：</h3>
 * <ol>
 *   <li>客户端生成时间戳和应用标识</li>
 *   <li>客户端计算请求的HMAC数字摘要</li>
 *   <li>客户端将认证信息放入Authorization头</li>
 *   <li>服务端解析Authorization头到此模型</li>
 *   <li>服务端使用此模型进行签名验证</li>
 * </ol>
 *
 * <h3>Authorization头格式：</h3>
 * <pre>{@code
 * Authorization: HMAC appid=<应用ID>,timestamp=<时间戳>,digest=<数字摘要>
 * }</pre>
 *
 * <h3>字段说明：</h3>
 * <ul>
 *   <li><strong>appid</strong> - 应用标识符，用于识别客户端应用</li>
 *   <li><strong>timestamp</strong> - 请求时间戳，用于防重放攻击</li>
 *   <li><strong>digest</strong> - HMAC数字摘要，用于验证请求完整性</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>防重放</strong> - 通过时间戳防止重放攻击</li>
 *   <li><strong>完整性</strong> - 通过数字摘要确保请求完整性</li>
 *   <li><strong>身份验证</strong> - 通过应用ID进行身份识别</li>
 *   <li><strong>不可伪造</strong> - 基于共享密钥的HMAC算法</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 解析Authorization头
 * HmacAuthorization auth = HmacUtils.getHmacAuthorization(request);
 * if (auth != null) {
 *     String appId = auth.getAppid();
 *     String timestamp = auth.getTimestamp();
 *     String digest = auth.getDigest();
 *     // 进行HMAC验证...
 * }
 * }</pre>
 *
 * @author jpjoo
 * @see com.lambda.security.web.hmac.utils.HmacUtils
 * @see com.lambda.security.web.hmac.wrapper.HmacRequestWrapper
 * @since 1.0.0
 */
@Setter
@Getter
public class HmacAuthorization {

    /**
     * 应用标识符
     * <p>
     * 用于唯一标识发起HMAC认证请求的客户端应用。
     * 服务端通过此标识符查找对应的应用配置和密钥信息，
     * 是HMAC认证过程中身份识别的关键字段。
     * </p>
     *
     * <h3>特性：</h3>
     * <ul>
     *   <li><strong>唯一性</strong> - 在系统中唯一标识一个应用</li>
     *   <li><strong>可读性</strong> - 通常为有意义的字符串标识</li>
     *   <li><strong>安全性</strong> - 不包含敏感信息，可在日志中记录</li>
     * </ul>
     *
     * <h3>用途：</h3>
     * <ul>
     *   <li>查找应用对应的HMAC密钥</li>
     *   <li>验证应用的访问权限</li>
     *   <li>记录和审计API调用</li>
     *   <li>实现应用级别的访问控制</li>
     * </ul>
     */
    private String appid;

    /**
     * 请求时间戳
     * <p>
     * 表示HMAC认证请求的发起时间，通常为Unix时间戳格式。
     * 用于防止重放攻击，服务端会验证时间戳的有效性，
     * 拒绝过期或未来时间的请求。
     * </p>
     *
     * <h3>格式要求：</h3>
     * <ul>
     *   <li><strong>Unix时间戳</strong> - 自1970年1月1日以来的秒数</li>
     *   <li><strong>字符串格式</strong> - 数字字符串，如"1640995200"</li>
     *   <li><strong>精度</strong> - 通常精确到秒级</li>
     * </ul>
     *
     * <h3>安全作用：</h3>
     * <ul>
     *   <li><strong>防重放攻击</strong> - 防止恶意重复使用旧请求</li>
     *   <li><strong>时效性控制</strong> - 限制请求的有效时间窗口</li>
     *   <li><strong>时序验证</strong> - 确保请求的时间合理性</li>
     * </ul>
     *
     * <h3>验证规则：</h3>
     * <ul>
     *   <li>时间戳不能早于当前时间减去允许的时间偏差</li>
     *   <li>时间戳不能晚于当前时间加上允许的时间偏差</li>
     *   <li>已使用的时间戳在有效期内不能重复使用</li>
     * </ul>
     */
    private String timestamp;

    /**
     * HMAC数字摘要
     * <p>
     * 使用HMAC算法计算的请求签名，用于验证请求的完整性和真实性。
     * 摘要基于请求的关键信息（如URL、参数、请求体等）和应用密钥计算得出，
     * 是HMAC认证的核心安全机制。
     * </p>
     *
     * <h3>计算方式：</h3>
     * <pre>{@code
     * digest = HMAC-SHA256(secret_key, salt_string)
     * salt_string = method + url + query_params + body + timestamp
     * }</pre>
     *
     * <h3>安全特性：</h3>
     * <ul>
     *   <li><strong>完整性保护</strong> - 任何请求内容的修改都会导致摘要不匹配</li>
     *   <li><strong>身份验证</strong> - 只有拥有正确密钥的客户端才能生成有效摘要</li>
     *   <li><strong>不可伪造</strong> - 没有密钥无法伪造有效的摘要</li>
     *   <li><strong>抗篡改</strong> - 请求在传输过程中的任何修改都能被检测</li>
     * </ul>
     *
     * <h3>格式特征：</h3>
     * <ul>
     *   <li><strong>编码格式</strong> - 通常为Base64或十六进制编码</li>
     *   <li><strong>固定长度</strong> - SHA256算法产生固定长度的摘要</li>
     *   <li><strong>大小写敏感</strong> - 摘要值区分大小写</li>
     * </ul>
     */
    private String digest;
}
