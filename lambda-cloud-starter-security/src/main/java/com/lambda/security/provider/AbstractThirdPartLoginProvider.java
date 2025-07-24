package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.service.ThirdPartyLoginService;
import lombok.RequiredArgsConstructor;

/**
 * 第三方登录提供者抽象基类
 *
 * <p>设计目标：
 * <ul>
 *   <li>统一接口：为所有第三方登录提供者提供统一的抽象基类</li>
 *   <li>模板方法：定义通用的认证流程，子类只需实现特定的参数获取逻辑</li>
 *   <li>扩展性：支持多种第三方登录平台（微信、QQ、微博等）</li>
 *   <li>解耦合：将认证逻辑与具体的第三方平台实现分离</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>认证流程：统一处理第三方登录的认证流程</li>
 *   <li>参数转换：将第三方平台的认证参数转换为系统内部格式</li>
 *   <li>用户加载：通过第三方登录结果加载用户信息</li>
 *   <li>授权URL：可选的授权URL构建功能</li>
 * </ul>
 *
 * <p>认证流程：
 * <ol>
 *   <li>接收第三方平台返回的token/code</li>
 *   <li>调用子类实现的getThirdLoginParam方法获取登录参数</li>
 *   <li>通过ThirdPartyLoginService加载用户信息</li>
 *   <li>返回登录用户对象</li>
 * </ol>
 *
 * <p>使用示例：
 * <pre>{@code
 * @Component
 * public class WeChatLoginProvider extends AbstractThirdPartLoginProvider {
 *
 *     public WeChatLoginProvider(ThirdPartyLoginService service) {
 *         super(service);
 *     }
 *
 *     @Override
 *     public ThirdPartLoginResult getThirdLoginParam(String code) {
 *         // 实现微信登录参数获取逻辑
 *         WeChatUserInfo userInfo = weChatApi.getUserInfo(code);
 *         return new ThirdPartLoginResult("wechat", userInfo);
 *     }
 * }
 * }</pre>
 *
 * <p>支持的第三方平台：
 * <ul>
 *   <li>微信登录：通过微信开放平台API</li>
 *   <li>QQ登录：通过QQ互联API</li>
 *   <li>微博登录：通过微博开放平台API</li>
 *   <li>其他：可扩展支持更多第三方平台</li>
 * </ul>
 *
 * @author Jin
 * @see ThirdPartLoginProvider
 * @see ThirdPartLoginResult
 * @see ThirdPartyLoginService
 */
@RequiredArgsConstructor
public abstract class AbstractThirdPartLoginProvider implements ThirdPartLoginProvider {

    /**
     * 第三方登录服务
     *
     * <p>功能说明：
     * <ul>
     *   <li>用户加载：根据第三方登录结果加载系统用户</li>
     *   <li>账号绑定：处理第三方账号与系统账号的绑定关系</li>
     *   <li>信息同步：同步第三方平台的用户信息</li>
     *   <li>权限设置：为第三方登录用户设置相应权限</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>新用户注册：第一次使用第三方登录时自动创建账号</li>
     *   <li>老用户登录：已绑定第三方账号的用户直接登录</li>
     *   <li>账号绑定：将第三方账号绑定到现有系统账号</li>
     * </ul>
     */
    private final ThirdPartyLoginService thirdPartyLoginService;

    /**
     * 执行第三方登录认证
     *
     * <p>认证流程：
     * <ol>
     *   <li>调用子类实现的getThirdLoginParam方法获取第三方登录参数</li>
     *   <li>将登录参数传递给ThirdPartyLoginService进行用户加载</li>
     *   <li>返回认证成功的用户对象</li>
     * </ol>
     *
     * <p>实现说明：
     * <ul>
     *   <li>模板方法：定义了通用的认证流程模板</li>
     *   <li>参数转换：将token转换为ThirdPartLoginResult对象</li>
     *   <li>异常处理：认证失败时会抛出相应的异常</li>
     * </ul>
     *
     * @param token 第三方平台返回的认证token或code
     * @param loginType 登录类型，用于区分不同的登录场景
     * @return 认证成功的登录用户对象
     * @throws RuntimeException 当认证失败或用户不存在时抛出
     */
    @Override
    public LoginUser authenticate(String token, String loginType) {
        ThirdPartLoginResult thirdPartLoginResult = getThirdLoginParam(token);
        return thirdPartyLoginService.loadByThirdLoginResult(thirdPartLoginResult, loginType);
    }

    /**
     * 获取第三方登录参数（抽象方法）
     *
     * <p>实现要求：
     * <ul>
     *   <li>参数获取：根据code/token从第三方平台获取用户信息</li>
     *   <li>数据转换：将第三方平台的用户数据转换为标准格式</li>
     *   <li>异常处理：处理第三方API调用可能出现的异常</li>
     *   <li>数据验证：验证获取到的用户数据的完整性</li>
     * </ul>
     *
     * <p>实现示例：
     * <pre>{@code
     * @Override
     * public ThirdPartLoginResult getThirdLoginParam(String code) {
     *     try {
     *         // 1. 通过code获取access_token
     *         String accessToken = getAccessToken(code);
     *
     *         // 2. 通过access_token获取用户信息
     *         UserInfo userInfo = getUserInfo(accessToken);
     *
     *         // 3. 构造登录结果
     *         return new ThirdPartLoginResult("wechat", userInfo);
     *     } catch (Exception e) {
     *         throw new ThirdPartLoginException("获取微信用户信息失败", e);
     *     }
     * }
     * }</pre>
     *
     * @param code 第三方平台返回的授权码或访问令牌
     * @return 包含第三方登录信息的结果对象
     * @throws RuntimeException 当获取第三方登录参数失败时抛出
     */
    public abstract ThirdPartLoginResult getThirdLoginParam(String code);

    /**
     * 构建授权URL（可选实现）
     *
     * <p>功能说明：
     * <ul>
     *   <li>URL构建：构建第三方平台的授权登录URL</li>
     *   <li>参数设置：设置state、scope、redirectUri等参数</li>
     *   <li>安全性：确保授权URL的安全性和有效性</li>
     * </ul>
     *
     * <p>默认实现：
     * <ul>
     *   <li>抛出UnsupportedOperationException异常</li>
     *   <li>子类可以根据需要重写此方法</li>
     *   <li>适用于不需要动态构建授权URL的场景</li>
     * </ul>
     *
     * <p>重写示例：
     * <pre>{@code
     * @Override
     * public String buildAuthorizationUrl(String state, String scope, String redirectUri) {
     *     return String.format(
     *         "https://open.weixin.qq.com/connect/oauth2/authorize?appid=%s&redirect_uri=%s&response_type=code&scope=%s&state=%s",
     *         appId, URLEncoder.encode(redirectUri, "UTF-8"), scope, state
     *     );
     * }
     * }</pre>
     *
     * @param state 状态参数，用于防止CSRF攻击
     * @param scope 授权范围，定义需要获取的用户信息权限
     * @param redirectUri 授权成功后的回调地址
     * @return 构建好的授权URL
     * @throws UnsupportedOperationException 默认实现抛出此异常
     */
    public String buildAuthorizationUrl(String state, String scope, String redirectUri) {
        throw new UnsupportedOperationException("Not support buildAuthorizationUrl");
    }
}
