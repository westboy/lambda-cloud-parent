package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.provider.ThirdPartLoginResult;

/**
 * 第三方登录服务接口
 * <p>
 * 该接口定义了第三方登录的核心业务逻辑，负责将第三方登录的结果转换为系统内部的
 * 登录用户对象。这是第三方登录流程中的关键环节，连接了第三方认证和系统用户管理。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>业务抽象：</strong>抽象第三方登录的核心业务逻辑</li>
 *   <li><strong>用户映射：</strong>将第三方用户信息映射为系统用户</li>
 *   <li><strong>统一接口：</strong>为不同第三方平台提供统一的处理接口</li>
 *   <li><strong>扩展支持：</strong>支持业务系统的自定义用户处理逻辑</li>
 * </ul>
 *
 * <h3>主要职责：</h3>
 * <ul>
 *   <li><strong>用户查找：</strong>根据第三方信息查找系统中的对应用户</li>
 *   <li><strong>用户创建：</strong>为新的第三方用户创建系统账户</li>
 *   <li><strong>信息同步：</strong>同步第三方用户信息到系统用户</li>
 *   <li><strong>权限分配：</strong>为第三方登录用户分配适当的权限</li>
 * </ul>
 *
 * <h3>处理流程：</h3>
 * <ol>
 *   <li><strong>结果解析：</strong>解析第三方登录提供者返回的结果</li>
 *   <li><strong>用户识别：</strong>根据第三方唯一标识（如openid）识别用户</li>
 *   <li><strong>用户匹配：</strong>在系统中查找对应的用户记录</li>
 *   <li><strong>用户处理：</strong>创建新用户或更新现有用户信息</li>
 *   <li><strong>权限设置：</strong>设置用户的角色和权限</li>
 *   <li><strong>返回用户：</strong>返回完整的登录用户对象</li>
 * </ol>
 *
 * <h3>实现示例：</h3>
 * <pre>{@code
 * @Service
 * public class ThirdPartyLoginServiceImpl implements ThirdPartyLoginService {
 *
 *     @Autowired
 *     private UserService userService;
 *
 *     @Override
 *     public LoginUser loadByThirdLoginResult(ThirdPartLoginResult result, String loginType) {
 *         // 1. 解析第三方登录结果
 *         String thirdType = result.getThirdType();
 *         Object thirdData = result.getResult();
 *
 *         // 2. 根据第三方类型处理不同的登录逻辑
 *         switch (thirdType) {
 *             case "wxMa":
 *                 return handleWeChatMiniApp(thirdData, loginType);
 *             case "wechat":
 *                 return handleWeChat(thirdData, loginType);
 *             default:
 *                 throw new UnsupportedOperationException("不支持的第三方类型: " + thirdType);
 *         }
 *     }
 *
 *     private LoginUser handleWeChatMiniApp(Object thirdData, String loginType) {
 *         String openid = (String) thirdData;
 *
 *         // 查找或创建用户
 *         User user = userService.findByWeChatOpenid(openid);
 *         if (user == null) {
 *             user = userService.createWeChatUser(openid, loginType);
 *         }
 *
 *         // 转换为登录用户对象
 *         return convertToLoginUser(user);
 *     }
 * }
 * }</pre>
 *
 * <h3>支持的第三方平台：</h3>
 * <ul>
 *   <li><strong>微信小程序：</strong>处理微信小程序的openid登录</li>
 *   <li><strong>微信公众号：</strong>处理微信公众号的用户登录</li>
 *   <li><strong>支付宝：</strong>处理支付宝小程序和开放平台登录</li>
 *   <li><strong>QQ登录：</strong>处理QQ互联登录</li>
 *   <li><strong>其他平台：</strong>可扩展支持其他OAuth2.0平台</li>
 * </ul>
 *
 * <h3>安全考虑：</h3>
 * <ul>
 *   <li><strong>数据验证：</strong>验证第三方返回数据的完整性和有效性</li>
 *   <li><strong>用户权限：</strong>为第三方登录用户分配适当的权限级别</li>
 *   <li><strong>信息脱敏：</strong>对敏感用户信息进行适当的脱敏处理</li>
 *   <li><strong>异常处理：</strong>妥善处理各种异常情况</li>
 * </ul>
 *
 * @see ThirdPartLoginResult
 * @see LoginUser
 * @see com.lambda.security.provider.ThirdPartLoginProvider
 */
public interface ThirdPartyLoginService {

    /**
     * 根据第三方登录结果加载系统用户
     * <p>
     * 该方法是第三方登录的核心业务方法，负责将第三方登录提供者返回的结果
     * 转换为系统内部的登录用户对象。这个过程包括用户查找、创建、信息同步等操作。
     * </p>
     *
     * <h3>处理策略：</h3>
     * <ul>
     *   <li><strong>用户存在：</strong>如果系统中已存在对应的用户，则更新用户信息并返回</li>
     *   <li><strong>用户不存在：</strong>如果系统中不存在对应用户，则创建新用户</li>
     *   <li><strong>信息同步：</strong>同步第三方平台的最新用户信息到系统</li>
     *   <li><strong>权限分配：</strong>为用户分配适当的角色和权限</li>
     * </ul>
     *
     * <h3>典型处理流程：</h3>
     * <ol>
     *   <li><strong>结果解析：</strong>解析ThirdPartLoginResult中的第三方数据</li>
     *   <li><strong>平台识别：</strong>根据thirdType识别第三方平台类型</li>
     *   <li><strong>用户标识提取：</strong>从第三方数据中提取用户唯一标识</li>
     *   <li><strong>用户查询：</strong>在系统中查找对应的用户记录</li>
     *   <li><strong>用户处理：</strong>创建新用户或更新现有用户</li>
     *   <li><strong>权限设置：</strong>设置用户的角色、权限等信息</li>
     *   <li><strong>返回用户：</strong>返回完整的LoginUser对象</li>
     * </ol>
     *
     * <h3>不同登录类型的处理：</h3>
     * <ul>
     *   <li><strong>普通登录：</strong>标准的第三方登录流程</li>
     *   <li><strong>绑定登录：</strong>将第三方账号绑定到现有系统账号</li>
     *   <li><strong>注册登录：</strong>通过第三方账号注册新的系统账号</li>
     *   <li><strong>临时登录：</strong>创建临时用户进行体验</li>
     * </ul>
     *
     * @param thirdLoginResult 第三方登录结果，包含第三方平台类型和用户数据
     * @param loginType 登录类型，用于区分不同的登录场景（如：login、bind、register等）
     * @return 系统内部的登录用户对象，包含用户基本信息、权限等
     * @throws com.lambda.security.exception.AuthenticationException 当用户认证失败时抛出
     * @throws IllegalArgumentException 当参数无效时抛出
     */
    LoginUser loadByThirdLoginResult(ThirdPartLoginResult thirdLoginResult, String loginType);
}
