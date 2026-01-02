package com.lambda.security.provider.wx;

import cn.binarywang.wx.miniapp.api.WxMaService;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.provider.AbstractThirdPartLoginProvider;
import com.lambda.security.provider.ThirdPartLoginResult;
import com.lambda.security.service.ThirdPartyLoginService;

/**
 * 微信小程序登录提供者
 * <p>
 * 该类实现了微信小程序的第三方登录功能，继承自{@link AbstractThirdPartLoginProvider}，
 * 专门处理微信小程序的登录认证流程，包括code换取session_key、用户信息解密等操作。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>微信小程序集成：</strong>专门为微信小程序登录场景设计</li>
 *   <li><strong>安全认证：</strong>处理微信小程序的安全登录流程</li>
 *   <li><strong>用户信息解密：</strong>支持微信用户敏感信息的解密</li>
 *   <li><strong>会话管理：</strong>管理微信小程序的会话状态</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>Code登录：</strong>使用微信小程序的code进行登录认证</li>
 *   <li><strong>Session管理：</strong>管理微信小程序的session_key</li>
 *   <li><strong>用户信息解密：</strong>解密微信返回的加密用户信息</li>
 *   <li><strong>手机号解密：</strong>解密微信返回的加密手机号信息</li>
 * </ul>
 *
 * <h3>认证流程：</h3>
 * <ol>
 *   <li><strong>获取Code：</strong>小程序端调用wx.login()获取code</li>
 *   <li><strong>Code换取Session：</strong>使用code向微信服务器换取session_key</li>
 *   <li><strong>用户信息处理：</strong>根据需要解密用户敏感信息</li>
 *   <li><strong>本地用户匹配：</strong>根据微信用户信息匹配本地用户</li>
 *   <li><strong>登录状态建立：</strong>建立系统内部的登录状态</li>
 * </ol>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 配置微信小程序登录提供者
 * @Bean
 * public WxMaLoginProvider wxMaLoginProvider(
 *         ThirdPartyLoginService thirdPartyLoginService,
 *         WxMaService wxMaService,
 *         WxMaLoginHandler wxMaLoginHandler) {
 *     return new WxMaLoginProvider(thirdPartyLoginService, wxMaService, wxMaLoginHandler);
 * }
 *
 * // 小程序端登录流程
 * wx.login({
 *   success: function(res) {
 *     if (res.code) {
 *       // 发送code到后端进行登录
 *       wx.request({
 *         url: '/api/auth/third-login',
 *         data: {
 *           thirdType: 'wxMa',
 *           loginParam: res.code
 *         }
 *       });
 *     }
 *   }
 * });
 * }</pre>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>Code有效期：</strong>微信code具有5分钟有效期，防止重放攻击</li>
 *   <li><strong>Session加密：</strong>session_key用于解密用户敏感信息</li>
 *   <li><strong>数据校验：</strong>对微信返回的数据进行完整性校验</li>
 *   <li><strong>异常处理：</strong>妥善处理微信API调用异常</li>
 * </ul>
 *
 * <h3>依赖组件：</h3>
 * <ul>
 *   <li>{@link WxMaService} - 微信小程序API服务</li>
 *   <li>{@link WxMaLoginHandler} - 微信小程序登录处理器</li>
 *   <li>{@link ThirdPartyLoginService} - 第三方登录服务</li>
 * </ul>
 *
 * @param <T> 微信小程序登录处理器类型，继承自{@link WxMaLoginHandler}
 * @author Jin
 * @see AbstractThirdPartLoginProvider
 * @see WxMaLoginHandler
 * @see ThirdPartLoginResult
 * @see WxMaService
 */
public class WxMaLoginProvider<T extends WxMaLoginHandler> extends AbstractThirdPartLoginProvider {

    /**
     * 微信小程序登录处理器
     * <p>
     * 负责处理具体的微信小程序登录逻辑，包括用户信息的获取、
     * 解密、验证等操作。支持泛型，可以扩展不同类型的处理器。
     * </p>
     */
    protected final T WxMaLoginHandler;

    /**
     * 构造微信小程序登录提供者
     * <p>
     * 初始化微信小程序登录提供者，注入必要的依赖服务。
     * </p>
     *
     * @param thirdPartService 第三方登录服务，用于处理通用的第三方登录逻辑
     * @param wxMaLoginHandler 微信小程序登录处理器，处理具体的登录业务逻辑
     */
    public WxMaLoginProvider(ThirdPartyLoginService thirdPartService, T wxMaLoginHandler) {
        super(thirdPartService);
        this.WxMaLoginHandler = wxMaLoginHandler;
    }

    /**
     * 获取微信小程序第三方登录参数
     * <p>
     * 处理微信小程序的登录参数（通常是code），通过微信小程序登录处理器
     * 进行具体的业务处理，并返回标准化的第三方登录结果。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>参数验证：</strong>验证传入的登录参数（code）有效性</li>
     *   <li><strong>委托处理：</strong>将参数委托给微信小程序登录处理器处理</li>
     *   <li><strong>结果封装：</strong>将处理结果封装为标准的第三方登录结果</li>
     *   <li><strong>异常转换：</strong>将微信API异常转换为系统认证异常</li>
     * </ol>
     *
     * <h3>典型处理内容：</h3>
     * <ul>
     *   <li>使用code换取session_key和openid</li>
     *   <li>解密用户敏感信息（如手机号）</li>
     *   <li>获取用户基本信息</li>
     *   <li>验证数据完整性</li>
     * </ul>
     *
     * @param loginParam 微信小程序登录参数，通常是通过wx.login()获取的code
     * @return 包含微信小程序登录结果的第三方登录结果对象
     * @throws AuthenticationException 当微信API调用失败或认证失败时抛出
     */
    @Override
    public ThirdPartLoginResult getThirdLoginParam(String loginParam) {
        try {
            Object result = WxMaLoginHandler.handle(loginParam);
            return new ThirdPartLoginResult(getThirdType(), result);
        } catch (Exception e) {
            throw new AuthenticationException(e.getMessage());
        }
    }

    /**
     * 检查是否支持指定的第三方平台类型
     * <p>
     * 判断当前提供者是否支持指定的第三方平台标识。
     * 对于微信小程序登录提供者，只支持"wxMa"类型。
     * </p>
     *
     * @param thirdId 第三方平台类型标识
     * @return 如果是"wxMa"类型返回true，否则返回false
     */
    @Override
    public boolean support(String thirdId) {
        return getThirdType().equals(thirdId);
    }

    /**
     * 获取第三方平台类型标识
     * <p>
     * 返回微信小程序登录提供者的唯一标识符"wxMa"。
     * 该标识符用于区分微信小程序登录与其他第三方登录方式。
     * </p>
     *
     * <h3>标识符说明：</h3>
     * <ul>
     *   <li><strong>wxMa：</strong>WeChat Mini App的缩写</li>
     *   <li><strong>唯一性：</strong>在系统中唯一标识微信小程序登录</li>
     *   <li><strong>一致性：</strong>与前端调用时使用的标识保持一致</li>
     * </ul>
     *
     * @return 微信小程序平台类型标识"wxMa"
     */
    @Override
    public String getThirdType() {
        return "wxMa";
    }
}
