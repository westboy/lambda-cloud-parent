package com.lambda.security.web.verify.service.sms.model;

import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 短信验证码发送响应结果
 *
 * <p>封装短信验证码发送操作的响应信息，包括发送状态、重发限制、有效期等关键信息。
 * 用于向客户端返回短信发送的结果和相关的业务参数。
 *
 * <p>设计目标：
 * <ul>
 *   <li>信息完整：包含客户端所需的所有响应信息</li>
 *   <li>序列化支持：实现Serializable接口，支持网络传输</li>
 *   <li>易于使用：通过Lombok简化getter/setter方法</li>
 *   <li>灵活构造：支持无参和全参构造函数</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>状态反馈：告知客户端短信发送是否成功</li>
 *   <li>限制控制：提供重发时间间隔信息</li>
 *   <li>有效期通知：告知验证码的有效时间</li>
 *   <li>消息传递：提供详细的状态描述信息</li>
 * </ul>
 *
 * <p>响应场景：
 * <ul>
 *   <li>发送成功：返回验证码ID和有效期信息</li>
 *   <li>发送失败：返回错误消息和重试建议</li>
 *   <li>频率限制：返回重发等待时间</li>
 *   <li>参数错误：返回具体的错误描述</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 发送成功的响应
 * SmsVerifyCodeResponse success = new SmsVerifyCodeResponse(
 *     "sms_123456",     // 短信ID
 *     60,               // 60秒后可重发
 *     5,                // 5分钟有效期
 *     "短信发送成功"      // 成功消息
 * );
 *
 * // 发送失败的响应
 * SmsVerifyCodeResponse failure = new SmsVerifyCodeResponse(
 *     null,             // 无短信ID
 *     120,              // 2分钟后可重试
 *     null,             // 无有效期
 *     "手机号格式错误"    // 错误消息
 * );
 *
 * // JSON序列化示例
 * {
 *   "id": "sms_123456",
 *   "resendSeconds": 60,
 *   "validMinutes": 5,
 *   "message": "短信发送成功"
 * }
 * }</pre>
 *
 * <p>字段说明：
 * <ul>
 *   <li>id：短信发送的唯一标识，用于追踪和验证</li>
 *   <li>resendSeconds：距离下次可重发的秒数</li>
 *   <li>validMinutes：验证码的有效期（分钟）</li>
 *   <li>message：人类可读的状态描述信息</li>
 * </ul>
 *
 * <p>最佳实践：
 * <ul>
 *   <li>状态一致性：确保字段值与实际状态保持一致</li>
 *   <li>消息本地化：根据用户语言返回相应的消息</li>
 *   <li>安全考虑：不在响应中暴露敏感的内部信息</li>
 *   <li>客户端友好：提供清晰易懂的状态描述</li>
 * </ul>
 *
 * @author Jin
 * @see SmsVerifyCode
 * @see SmsVerifyCodeStore
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class SmsVerifyCodeResponse implements Serializable {

    /**
     * 短信发送的唯一标识
     *
     * <p>短信服务提供商返回的唯一标识符，用于追踪短信的发送状态和后续操作。
     * 在发送成功时包含有效值，发送失败时可能为null。
     *
     * <p>标识特性：
     * <ul>
     *   <li>唯一性：每条短信都有唯一的标识符</li>
     *   <li>可追踪：可用于查询短信的发送状态</li>
     *   <li>可选性：发送失败时可能为null</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>状态查询：查询短信的发送和到达状态</li>
     *   <li>问题排查：定位短信发送过程中的问题</li>
     *   <li>审计日志：记录短信发送的完整链路</li>
     * </ul>
     */
    private String id;

    /**
     * 距离下次可重发的秒数
     *
     * <p>为了防止短信轰炸和降低成本，系统会限制短信的发送频率。
     * 该字段表示用户需要等待多少秒后才能再次请求发送验证码。
     *
     * <p>限制策略：
     * <ul>
     *   <li>频率控制：防止恶意用户频繁发送短信</li>
     *   <li>成本控制：减少不必要的短信发送费用</li>
     *   <li>用户体验：给用户明确的等待时间预期</li>
     * </ul>
     *
     * <p>典型值：
     * <ul>
     *   <li>60秒：常见的重发间隔</li>
     *   <li>120秒：较严格的限制策略</li>
     *   <li>0：立即可以重发（通常在发送失败时）</li>
     * </ul>
     */
    private Integer resendSeconds;

    /**
     * 验证码有效期（分钟）
     *
     * <p>验证码从发送成功开始计算的有效时间，超过该时间后验证码将自动失效。
     * 客户端可以使用该信息向用户显示倒计时或有效期提醒。
     *
     * <p>有效期设计：
     * <ul>
     *   <li>安全性：较短的有效期可以降低被恶意使用的风险</li>
     *   <li>用户体验：足够的时间让用户完成验证操作</li>
     *   <li>系统性能：自动过期可以减少存储压力</li>
     * </ul>
     *
     * <p>典型值：
     * <ul>
     *   <li>5分钟：常见的验证码有效期</li>
     *   <li>10分钟：较宽松的有效期设置</li>
     *   <li>3分钟：较严格的安全策略</li>
     * </ul>
     */
    private Integer validMinutes;

    /**
     * 响应消息描述
     *
     * <p>人类可读的状态描述信息，用于向用户展示操作结果和相关提示。
     * 消息内容应该清晰、友好，并根据用户的语言偏好进行本地化。
     *
     * <p>消息类型：
     * <ul>
     *   <li>成功消息："短信发送成功，请查收验证码"</li>
     *   <li>失败消息："手机号格式错误，请检查后重试"</li>
     *   <li>限制消息："发送过于频繁，请稍后再试"</li>
     *   <li>系统消息："系统繁忙，请稍后重试"</li>
     * </ul>
     *
     * <p>设计原则：
     * <ul>
     *   <li>用户友好：使用通俗易懂的语言</li>
     *   <li>信息准确：准确反映当前的操作状态</li>
     *   <li>指导性：提供明确的后续操作建议</li>
     *   <li>本地化：支持多语言环境</li>
     * </ul>
     */
    private String message;
}
