package com.lambda.security.provider;

import com.lambda.cloud.core.principal.LoginUser;

/**
 * 第三方登录提供者接口
 * <p>
 * 该接口定义了第三方登录提供者的标准规范，用于集成各种第三方登录平台
 * （如微信、支付宝、QQ、微博等）的登录认证功能。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>统一接口：</strong>为不同的第三方登录平台提供统一的接口规范</li>
 *   <li><strong>插件化架构：</strong>支持插件化的第三方登录扩展</li>
 *   <li><strong>解耦设计：</strong>将第三方登录逻辑与核心认证逻辑解耦</li>
 *   <li><strong>灵活扩展：</strong>便于添加新的第三方登录平台支持</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>身份认证：</strong>通过第三方平台的令牌进行用户身份认证</li>
 *   <li><strong>用户信息获取：</strong>从第三方平台获取用户基本信息</li>
 *   <li><strong>平台支持检查：</strong>检查是否支持特定的第三方平台</li>
 *   <li><strong>类型标识：</strong>提供第三方平台的唯一标识</li>
 * </ul>
 *
 * <h3>支持的第三方平台：</h3>
 * <ul>
 *   <li><strong>微信：</strong>微信公众号、小程序、开放平台登录</li>
 *   <li><strong>支付宝：</strong>支付宝小程序、开放平台登录</li>
 *   <li><strong>QQ：</strong>QQ互联登录</li>
 *   <li><strong>微博：</strong>新浪微博登录</li>
 *   <li><strong>钉钉：</strong>钉钉企业登录</li>
 *   <li><strong>其他：</strong>可扩展支持其他OAuth2.0平台</li>
 * </ul>
 *
 * <h3>实现示例：</h3>
 * <pre>{@code
 * @Component
 * public class WeChatLoginProvider implements ThirdPartLoginProvider {
 *
 *     @Override
 *     public LoginUser authenticate(String token, String loginType) {
 *         // 1. 使用token从微信API获取用户信息
 *         WeChatUserInfo userInfo = weChatApi.getUserInfo(token);
 *
 *         // 2. 根据微信用户信息查找或创建本地用户
 *         LoginUser loginUser = userService.findOrCreateUser(userInfo);
 *
 *         // 3. 返回登录用户对象
 *         return loginUser;
 *     }
 *
 *     @Override
 *     public boolean support(String thirdType) {
 *         return "wechat".equals(thirdType);
 *     }
 *
 *     @Override
 *     public String getThirdType() {
 *         return "wechat";
 *     }
 * }
 * }</pre>
 *
 * <h3>集成方式：</h3>
 * <ul>
 *   <li>实现ThirdPartLoginProvider接口</li>
 *   <li>在Spring容器中注册为Bean</li>
 *   <li>通过{@link com.lambda.security.service.ThirdPartyLoginService}进行调用</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li><strong>令牌验证：</strong>严格验证第三方平台返回的令牌</li>
 *   <li><strong>用户信息校验：</strong>校验从第三方获取的用户信息</li>
 *   <li><strong>防重放攻击：</strong>防止令牌被重复使用</li>
 *   <li><strong>数据脱敏：</strong>对敏感用户信息进行脱敏处理</li>
 * </ul>
 *
 * @author Jin
 * @see AbstractThirdPartLoginProvider
 * @see ThirdPartLoginResult
 * @see com.lambda.security.service.ThirdPartyLoginService
 */
public interface ThirdPartLoginProvider {

    /**
     * 执行第三方登录认证
     * <p>
     * 使用第三方平台提供的令牌进行用户身份认证，并返回系统内部的登录用户对象。
     * 该方法是第三方登录的核心处理逻辑。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>令牌验证：</strong>验证第三方令牌的有效性</li>
     *   <li><strong>用户信息获取：</strong>通过令牌从第三方平台获取用户信息</li>
     *   <li><strong>用户匹配：</strong>根据第三方用户信息查找本地用户</li>
     *   <li><strong>用户创建：</strong>如果本地用户不存在，则创建新用户</li>
     *   <li><strong>信息同步：</strong>同步第三方用户信息到本地</li>
     *   <li><strong>返回用户：</strong>返回完整的登录用户对象</li>
     * </ol>
     *
     * <h3>异常处理：</h3>
     * <ul>
     *   <li>令牌无效或过期时抛出认证异常</li>
     *   <li>第三方API调用失败时抛出服务异常</li>
     *   <li>用户信息不完整时抛出数据异常</li>
     * </ul>
     *
     * @param token 第三方平台提供的访问令牌或授权码
     * @param loginType 登录类型标识，用于区分不同的登录场景
     * @return 认证成功的登录用户对象，包含用户基本信息和权限
     * @throws com.lambda.security.exception.AuthenticationException 当认证失败时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    LoginUser authenticate(String token, String loginType);

    /**
     * 检查是否支持指定的第三方平台类型
     * <p>
     * 该方法用于判断当前提供者是否能够处理指定类型的第三方登录请求。
     * 系统会根据此方法的返回值来选择合适的登录提供者。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>系统启动时注册可用的登录提供者</li>
     *   <li>处理登录请求时选择对应的提供者</li>
     *   <li>动态检查平台支持情况</li>
     * </ul>
     *
     * @param thirdType 第三方平台类型标识（如："wechat"、"alipay"、"qq"等）
     * @return 如果支持该平台类型返回true，否则返回false
     */
    boolean support(String thirdType);

    /**
     * 获取第三方平台类型标识
     * <p>
     * 返回当前登录提供者所支持的第三方平台的唯一标识符。
     * 该标识符用于区分不同的第三方登录平台。
     * </p>
     *
     * <h3>标识符规范：</h3>
     * <ul>
     *   <li><strong>唯一性：</strong>在系统中必须是唯一的</li>
     *   <li><strong>简洁性：</strong>使用简短、有意义的字符串</li>
     *   <li><strong>一致性：</strong>与第三方平台的官方标识保持一致</li>
     *   <li><strong>小写：</strong>建议使用小写字母和下划线</li>
     * </ul>
     *
     * <h3>常见标识符：</h3>
     * <ul>
     *   <li>"wechat" - 微信登录</li>
     *   <li>"alipay" - 支付宝登录</li>
     *   <li>"qq" - QQ登录</li>
     *   <li>"weibo" - 微博登录</li>
     *   <li>"dingtalk" - 钉钉登录</li>
     * </ul>
     *
     * @return 第三方平台类型的唯一标识符
     */
    String getThirdType();
}
