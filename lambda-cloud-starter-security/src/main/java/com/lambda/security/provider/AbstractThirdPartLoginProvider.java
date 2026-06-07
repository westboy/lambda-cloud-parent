package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.ThirdPartyLoginService;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

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
@Setter
@RequiredArgsConstructor
public abstract class AbstractThirdPartLoginProvider<T extends ThirdPartLoginHandler>
        implements ThirdPartLoginProvider {

    private final ThirdPartyLoginService thirdPartyLoginService;

    protected T thirdPartLoginHandler;

    @Override
    public boolean support(String thirdId) {
        return getThirdType().equals(thirdId);
    }

    @Override
    public LoginUser authenticate(String token, String loginType) {
        ThirdPartLoginResult thirdPartLoginResult = getThirdLoginParam(token);
        return thirdPartyLoginService.loadByThirdLoginResult(thirdPartLoginResult, loginType);
    }

    public ThirdPartLoginResult getThirdLoginParam(String loginParam) {
        try {
            Object result = thirdPartLoginHandler.handle(loginParam);
            return new ThirdPartLoginResult(getThirdType(), result);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    public String buildAuthorizationUrl(String state, String scope, String redirectUri) {
        throw new UnsupportedOperationException("Not support buildAuthorizationUrl");
    }
}
