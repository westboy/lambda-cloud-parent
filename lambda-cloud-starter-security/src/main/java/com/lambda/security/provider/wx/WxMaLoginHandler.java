package com.lambda.security.provider.wx;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.bean.WxMaJscode2SessionResult;
import me.chanjar.weixin.common.error.WxErrorException;

/**
 * 微信小程序登录处理器接口
 * <p>
 * 该接口定义了微信小程序登录的核心处理逻辑，负责处理微信小程序的code换取session_key
 * 和openid的流程，以及相关的用户信息处理。提供了默认实现，同时支持自定义扩展。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>标准化处理：</strong>为微信小程序登录提供标准化的处理接口</li>
 *   <li><strong>灵活扩展：</strong>支持自定义登录处理逻辑</li>
 *   <li><strong>解耦设计：</strong>将具体的登录处理逻辑与提供者解耦</li>
 *   <li><strong>默认实现：</strong>提供开箱即用的默认处理逻辑</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>Code换取Session：</strong>使用微信小程序code换取session_key和openid</li>
 *   <li><strong>用户信息处理：</strong>处理微信返回的用户基本信息</li>
 *   <li><strong>敏感信息解密：</strong>支持解密用户手机号等敏感信息</li>
 *   <li><strong>自定义扩展：</strong>支持业务系统的自定义处理逻辑</li>
 * </ul>
 *
 * <h3>微信小程序登录流程：</h3>
 * <ol>
 *   <li><strong>前端获取Code：</strong>小程序调用wx.login()获取临时登录凭证code</li>
 *   <li><strong>Code换取Session：</strong>后端使用code调用微信接口换取session_key和openid</li>
 *   <li><strong>用户信息处理：</strong>根据openid查找或创建本地用户</li>
 *   <li><strong>敏感信息解密：</strong>如需要，使用session_key解密用户敏感信息</li>
 *   <li><strong>登录状态建立：</strong>建立系统内部的登录会话</li>
 * </ol>
 *
 * <h3>默认实现说明：</h3>
 * <p>
 * 接口提供了默认的{@link #handle(String, WxMaService)}方法实现，
 * 该实现会调用微信API将code换取为openid，这是最基础的处理逻辑。
 * 业务系统可以根据需要重写此方法以实现更复杂的处理逻辑。
 * </p>
 *
 * <h3>自定义实现示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomWxMaLoginHandler implements WxMaLoginHandler {
 *
 *     @Override
 *     public Object handle(String loginParam, WxMaService wxMaService) throws WxErrorException {
 *         // 1. 获取session信息
 *         WxMaJscode2SessionResult sessionResult = wxMaService.jsCode2SessionInfo(loginParam);
 *
 *         // 2. 构建自定义返回对象
 *         Map<String, Object> result = new HashMap<>();
 *         result.put("openid", sessionResult.getOpenid());
 *         result.put("unionid", sessionResult.getUnionid());
 *         result.put("sessionKey", sessionResult.getSessionKey());
 *
 *         // 3. 可以在这里添加更多业务逻辑
 *         // 如：用户信息预处理、数据校验等
 *
 *         return result;
 *     }
 * }
 * }</pre>
 *
 * <h3>安全注意事项：</h3>
 * <ul>
 *   <li><strong>Session Key保护：</strong>session_key是敏感信息，不应返回给前端</li>
 *   <li><strong>Code有效期：</strong>微信code具有5分钟有效期，且只能使用一次</li>
 *   <li><strong>数据校验：</strong>对微信返回的数据进行必要的校验</li>
 *   <li><strong>异常处理：</strong>妥善处理微信API可能抛出的各种异常</li>
 * </ul>
 *
 * @see WxMaLoginProvider
 * @see WxMaService
 * @see WxMaJscode2SessionResult
 */
public interface WxMaLoginHandler {
    /**
     * 此方法已过期,新方法 {@link WxMaLoginHandler#handle(String)}
     * <p>
     *
     * 处理微信小程序登录逻辑
     * <p>
     * 该方法是微信小程序登录处理的核心方法，负责将微信小程序的登录参数（code）
     * 转换为系统可用的用户标识信息。默认实现会调用微信API获取用户的openid。
     * </p>
     *
     * <h3>默认处理流程：</h3>
     * <ol>
     *   <li><strong>API调用：</strong>调用微信jsCode2SessionInfo接口</li>
     *   <li><strong>Session获取：</strong>获取包含openid、session_key等信息的结果</li>
     *   <li><strong>返回Openid：</strong>提取并返回用户的openid作为唯一标识</li>
     * </ol>
     *
     * <h3>可扩展的处理逻辑：</h3>
     * <ul>
     *   <li><strong>用户信息解密：</strong>使用session_key解密用户敏感信息</li>
     *   <li><strong>Unionid处理：</strong>处理微信开放平台的unionid</li>
     *   <li><strong>自定义返回：</strong>返回包含更多信息的自定义对象</li>
     *   <li><strong>业务逻辑：</strong>添加特定的业务处理逻辑</li>
     * </ul>
     *
     * <h3>返回值说明：</h3>
     * <p>
     * 默认实现返回用户的openid字符串，但实现类可以返回任何类型的对象，
     * 如Map、自定义POJO等，以满足不同的业务需求。
     * </p>
     *
     * @param loginParam 微信小程序登录参数，通常是通过wx.login()获取的code
     * @param wxMaService 微信小程序API服务，用于调用微信相关接口
     * @return 处理后的用户标识信息，默认返回openid，可自定义返回其他类型
     * @throws WxErrorException 当微信API调用失败时抛出，包含具体的错误信息
     */
    @Deprecated(since = "2025.1")
    default Object handle(String loginParam, WxMaService wxMaService) throws WxErrorException {
        WxMaJscode2SessionResult wxMaJscode2SessionResult = wxMaService.jsCode2SessionInfo(loginParam);
        return wxMaJscode2SessionResult.getOpenid();
    }

    /**
     * 处理微信小程序登录逻辑
     * <p>
     * 该方法是微信小程序登录处理的核心方法，负责将微信小程序的登录参数（code）
     * 转换为系统可用的用户标识信息。默认实现会调用微信API获取用户的openid。
     * </p>
     *
     * <h3>默认处理流程：</h3>
     * <ol>
     *   <li><strong>API调用：</strong>调用微信jsCode2SessionInfo接口</li>
     *   <li><strong>Session获取：</strong>获取包含openid、session_key等信息的结果</li>
     *   <li><strong>返回Openid：</strong>提取并返回用户的openid作为唯一标识</li>
     * </ol>
     *
     * <h3>可扩展的处理逻辑：</h3>
     * <ul>
     *   <li><strong>用户信息解密：</strong>使用session_key解密用户敏感信息</li>
     *   <li><strong>Unionid处理：</strong>处理微信开放平台的unionid</li>
     *   <li><strong>自定义返回：</strong>返回包含更多信息的自定义对象</li>
     *   <li><strong>业务逻辑：</strong>添加特定的业务处理逻辑</li>
     * </ul>
     *
     * <h3>返回值说明：</h3>
     * <p>
     * 默认实现返回用户的openid字符串，但实现类可以返回任何类型的对象，
     * 如Map、自定义POJO等，以满足不同的业务需求。
     * </p>
     *
     * @param loginParam 微信小程序登录参数，通常是通过wx.login()获取的code
     * @return 处理后的用户标识信息，默认返回openid，可自定义返回其他类型
     * @throws WxErrorException 当微信API调用失败时抛出，包含具体的错误信息
     */
    default Object handle(String loginParam) throws WxErrorException {
        return null;
    }
}
