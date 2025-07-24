package com.lambda.security.service;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;

/**
 * HMAC客户端服务接口
 * <p>
 * 该接口定义了基于HMAC（Hash-based Message Authentication Code）认证的客户端服务，
 * 主要用于API接口的安全认证，特别是服务间调用的身份验证和授权。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>API安全：</strong>为API接口提供基于HMAC的安全认证机制</li>
 *   <li><strong>服务间认证：</strong>支持微服务之间的安全通信</li>
 *   <li><strong>客户端管理：</strong>管理API客户端的身份和权限</li>
 *   <li><strong>签名验证：</strong>验证请求的完整性和真实性</li>
 * </ul>
 *
 * <h3>HMAC认证原理：</h3>
 * <ol>
 *   <li><strong>密钥分发：</strong>为每个客户端分配唯一的AppID和SecretKey</li>
 *   <li><strong>签名生成：</strong>客户端使用SecretKey对请求参数进行HMAC签名</li>
 *   <li><strong>请求发送：</strong>将AppID和签名随请求一起发送到服务端</li>
 *   <li><strong>签名验证：</strong>服务端使用相同算法验证签名的有效性</li>
 *   <li><strong>权限检查：</strong>验证客户端是否有权限访问请求的资源</li>
 * </ol>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>客户端加载：</strong>根据AppID加载客户端信息</li>
 *   <li><strong>用户认证：</strong>支持基于用户名的认证</li>
 *   <li><strong>权限管理：</strong>管理客户端的API访问权限</li>
 *   <li><strong>签名验证：</strong>验证HMAC签名的有效性</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li><strong>开放API：</strong>为第三方开发者提供安全的API访问</li>
 *   <li><strong>微服务通信：</strong>保障微服务间调用的安全性</li>
 *   <li><strong>移动应用：</strong>移动应用与后端服务的安全通信</li>
 *   <li><strong>IoT设备：</strong>物联网设备的身份认证和数据传输</li>
 * </ul>
 *
 * <h3>实现示例：</h3>
 * <pre>{@code
 * @Service
 * public class HmacClientServiceImpl implements HmacClientService {
 *
 *     @Autowired
 *     private ClientRepository clientRepository;
 *
 *     @Override
 *     public LoginUser loadClientByAppid(String appid) {
 *         // 1. 根据AppID查找客户端信息
 *         Client client = clientRepository.findByAppid(appid);
 *         if (client == null) {
 *             throw new AuthenticationException("客户端不存在");
 *         }
 *
 *         // 2. 检查客户端状态
 *         if (!client.isActive()) {
 *             throw new AuthenticationException("客户端已被禁用");
 *         }
 *
 *         // 3. 转换为登录用户对象
 *         return convertToLoginUser(client);
 *     }
 *
 *     @Override
 *     public LoginUser loginByUsername(String username, String loginType) {
 *         // 基于用户名的认证逻辑
 *         User user = userService.findByUsername(username);
 *         if (user == null) {
 *             throw new AuthenticationException("用户不存在");
 *         }
 *         return convertToLoginUser(user);
 *     }
 * }
 * }</pre>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>密钥安全：</strong>SecretKey严格保密，不在网络中传输</li>
 *   <li><strong>时间戳验证：</strong>防止重放攻击，验证请求的时效性</li>
 *   <li><strong>签名算法：</strong>使用强加密算法（如HMAC-SHA256）</li>
 *   <li><strong>权限控制：</strong>细粒度的API访问权限控制</li>
 * </ul>
 *
 * <h3>配置示例：</h3>
 * <pre>{@code
 * # HMAC认证配置
 * lambda:
 *   security:
 *     hmac:
 *       enabled: true
 *       algorithm: HmacSHA256
 *       timestamp-tolerance: 300  # 时间戳容忍度（秒）
 *       header-names:
 *         appid: X-API-APPID
 *         signature: X-API-SIGNATURE
 *         timestamp: X-API-TIMESTAMP
 * }</pre>
 *
 * @author jpjoo
 * @see LoginUser
 * @see AuthenticationException
 * @see com.lambda.security.web.hmac.HmacAuthenticationFilter
 */
public interface HmacClientService {

    /**
     * 根据AppID加载客户端信息
     * <p>
     * 该方法根据客户端的AppID加载对应的客户端信息，用于HMAC认证过程中的
     * 客户端身份验证。AppID是客户端的唯一标识符。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>AppID验证：</strong>验证AppID的格式和有效性</li>
     *   <li><strong>客户端查找：</strong>在系统中查找对应的客户端记录</li>
     *   <li><strong>状态检查：</strong>检查客户端是否处于活跃状态</li>
     *   <li><strong>权限加载：</strong>加载客户端的API访问权限</li>
     *   <li><strong>对象转换：</strong>将客户端信息转换为LoginUser对象</li>
     * </ol>
     *
     * <h3>验证内容：</h3>
     * <ul>
     *   <li>客户端是否存在</li>
     *   <li>客户端是否已启用</li>
     *   <li>客户端是否已过期</li>
     *   <li>客户端访问权限</li>
     * </ul>
     *
     * <h3>返回信息：</h3>
     * <p>
     * 返回的LoginUser对象应包含客户端的基本信息、SecretKey（用于签名验证）、
     * API访问权限等信息。SecretKey通常不直接暴露，而是在内部使用。
     * </p>
     *
     * @param appid 客户端应用ID，用于唯一标识API客户端
     * @return 客户端对应的登录用户对象，包含客户端信息和权限
     * @throws AuthenticationException 当客户端不存在或状态异常时抛出
     */
    LoginUser loadClientByAppid(String appid);

    /**
     * 根据用户名进行登录认证
     * <p>
     * 该方法提供基于用户名的登录认证功能，通常用于HMAC认证的补充场景，
     * 或者在某些特殊情况下需要用户级别的认证时使用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li><strong>混合认证：</strong>同时支持客户端认证和用户认证</li>
     *   <li><strong>代理认证：</strong>客户端代表特定用户进行API调用</li>
     *   <li><strong>管理接口：</strong>管理员通过用户名访问管理接口</li>
     *   <li><strong>调试模式：</strong>开发和测试阶段的用户认证</li>
     * </ul>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>用户名验证：</strong>验证用户名的格式和有效性</li>
     *   <li><strong>用户查找：</strong>在系统中查找对应的用户记录</li>
     *   <li><strong>状态检查：</strong>检查用户账户状态</li>
     *   <li><strong>权限加载：</strong>加载用户的API访问权限</li>
     *   <li><strong>对象转换：</strong>将用户信息转换为LoginUser对象</li>
     * </ol>
     *
     * <h3>安全考虑：</h3>
     * <ul>
     *   <li>此方法通常不涉及密码验证，密码验证在调用前完成</li>
     *   <li>需要确保调用此方法前已经过适当的身份验证</li>
     *   <li>返回的用户信息应包含适当的API访问权限</li>
     * </ul>
     *
     * @param username 用户名，用于查找用户的唯一标识
     * @param loginType 登录类型，用于区分不同的认证场景
     * @return 认证成功的登录用户对象，包含用户信息和API权限
     * @throws AuthenticationException 当用户认证失败时抛出
     */
    LoginUser loginByUsername(String username, String loginType) throws AuthenticationException;
}
