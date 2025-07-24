package com.lambda.security.web.hmac.service;

import com.lambda.autoconfig.SecurityProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import com.lambda.security.service.HmacClientService;
import com.lambda.security.service.UserDetailService;
import com.lambda.security.web.hmac.model.HmacClient;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 基于内存的HMAC客户端服务实现
 * <p>
 * 提供基于内存存储的HMAC客户端管理服务，用于存储和查询HMAC认证客户端信息。
 * 该实现将客户端配置加载到内存中，提供快速的客户端查询和用户认证功能。
 * 适用于客户端数量相对固定且不频繁变更的场景。
 * </p>
 *
 * <h3>设计目标：</h3>
 * <ul>
 *   <li><strong>高性能</strong> - 基于内存存储，提供快速的客户端查询</li>
 *   <li><strong>简单易用</strong> - 无需外部存储依赖，配置简单</li>
 *   <li><strong>线程安全</strong> - 支持多线程并发访问</li>
 *   <li><strong>配置驱动</strong> - 基于配置文件初始化客户端信息</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>客户端管理</strong> - 存储和管理HMAC客户端配置</li>
 *   <li><strong>客户端查询</strong> - 根据应用ID查询客户端信息</li>
 *   <li><strong>用户认证</strong> - 委托用户详情服务进行用户认证</li>
 *   <li><strong>配置加载</strong> - 从配置中加载客户端列表</li>
 * </ul>
 *
 * <h3>存储结构：</h3>
 * <ul>
 *   <li><strong>键值存储</strong> - 使用HashMap存储appid到LoginUser的映射</li>
 *   <li><strong>客户端包装</strong> - 将配置转换为HmacClient对象</li>
 *   <li><strong>用户名生成</strong> - 自动生成"hmac_" + appid格式的用户名</li>
 * </ul>
 *
 * <h3>适用场景：</h3>
 * <ul>
 *   <li><strong>小规模应用</strong> - 客户端数量较少的应用</li>
 *   <li><strong>配置固定</strong> - 客户端配置相对稳定的场景</li>
 *   <li><strong>快速启动</strong> - 需要快速部署和启动的环境</li>
 *   <li><strong>开发测试</strong> - 开发和测试环境的简单配置</li>
 * </ul>
 *
 * <h3>性能特点：</h3>
 * <ul>
 *   <li><strong>查询速度</strong> - O(1)时间复杂度的客户端查询</li>
 *   <li><strong>内存占用</strong> - 内存占用与客户端数量成正比</li>
 *   <li><strong>启动速度</strong> - 快速的服务初始化</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 配置客户端列表
 * List<SecurityProperties.Hmac.Client> clients = Arrays.asList(
 *     new SecurityProperties.Hmac.Client("app1", "secret1"),
 *     new SecurityProperties.Hmac.Client("app2", "secret2")
 * );
 *
 * // 创建服务实例
 * MemoryHmacClientService service = new MemoryHmacClientService(
 *     userDetailService, clients);
 *
 * // 查询客户端
 * LoginUser client = service.loadClientByAppid("app1");
 * }</pre>
 *
 * @author jpjoo
 * @see HmacClientService
 * @see HmacClient
 * @see SecurityProperties.Hmac.Client
 * @since 1.0.0
 */
public class MemoryHmacClientService implements HmacClientService {

    /**
     * 客户端用户映射表
     * <p>
     * 存储应用ID到LoginUser对象的映射关系，用于快速查询客户端信息。
     * 键为应用ID（appid），值为对应的HmacClient对象（继承自LoginUser）。
     * </p>
     *
     * <h3>存储特点：</h3>
     * <ul>
     *   <li><strong>快速查询</strong> - HashMap提供O(1)的查询性能</li>
     *   <li><strong>线程安全</strong> - 初始化后为只读，支持并发访问</li>
     *   <li><strong>内存高效</strong> - 直接存储在内存中，无IO开销</li>
     * </ul>
     */
    private final Map<String, LoginUser> users = new HashMap<>();

    /**
     * 用户详情服务
     * <p>
     * 用于处理用户认证相关的业务逻辑，当需要进行用户名密码认证时，
     * 会委托给此服务进行处理。
     * </p>
     */
    private final UserDetailService userDetailService;

    /**
     * 构造函数
     * <p>
     * 初始化内存HMAC客户端服务，加载配置中的客户端列表到内存中。
     * 为每个客户端创建对应的HmacClient对象，并建立appid到客户端的映射关系。
     * </p>
     *
     * <h3>初始化过程：</h3>
     * <ol>
     *   <li>遍历配置中的客户端列表</li>
     *   <li>为每个客户端创建HmacClient对象</li>
     *   <li>生成格式为"hmac_" + appid的用户名</li>
     *   <li>将appid和HmacClient的映射存储到内存中</li>
     *   <li>保存用户详情服务的引用</li>
     * </ol>
     *
     * <h3>客户端对象创建：</h3>
     * <ul>
     *   <li><strong>用户名格式</strong> - "hmac_" + client.getAppid()</li>
     *   <li><strong>密钥设置</strong> - 使用配置中的secret作为凭据</li>
     *   <li><strong>对象类型</strong> - 创建HmacClient实例</li>
     * </ul>
     *
     * @param userDetailService 用户详情服务，用于处理用户认证
     * @param clients 客户端配置列表，包含appid和secret信息
     *
     * @see HmacClient
     * @see SecurityProperties.Hmac.Client
     * @see UserDetailService
     */
    public MemoryHmacClientService(UserDetailService userDetailService, List<SecurityProperties.Hmac.Client> clients) {
        for (SecurityProperties.Hmac.Client client : clients) {
            users.put(client.getAppid(), new HmacClient("hmac_" + client.getAppid(), client.getSecret()));
        }
        this.userDetailService = userDetailService;
    }

    /**
     * 根据应用ID加载客户端信息
     * <p>
     * 从内存中查询指定应用ID对应的客户端信息。
     * 该方法提供快速的客户端查询功能，用于HMAC认证过程中的客户端验证。
     * </p>
     *
     * <h3>查询逻辑：</h3>
     * <ul>
     *   <li><strong>直接查询</strong> - 从HashMap中直接获取客户端信息</li>
     *   <li><strong>快速响应</strong> - O(1)时间复杂度的查询性能</li>
     *   <li><strong>空值处理</strong> - 未找到时返回null</li>
     * </ul>
     *
     * <h3>返回对象：</h3>
     * <ul>
     *   <li><strong>HmacClient</strong> - 包含用户名和密钥的客户端对象</li>
     *   <li><strong>用户名格式</strong> - "hmac_" + appid</li>
     *   <li><strong>凭据信息</strong> - 包含用于HMAC签名验证的密钥</li>
     * </ul>
     *
     * @param appid 应用标识符，用于查询对应的客户端信息
     * @return 客户端信息对象，如果未找到则返回null
     *
     * @see HmacClient
     * @see LoginUser
     */
    @Override
    public LoginUser loadClientByAppid(String appid) {
        return users.get(appid);
    }

    /**
     * 根据用户名进行用户登录认证
     * <p>
     * 委托给用户详情服务进行用户名密码认证。
     * 该方法主要用于处理需要用户凭据验证的认证场景，
     * 与HMAC客户端认证形成互补的认证体系。
     * </p>
     *
     * <h3>委托处理：</h3>
     * <ul>
     *   <li><strong>服务委托</strong> - 将认证请求转发给UserDetailService</li>
     *   <li><strong>类型支持</strong> - 支持不同的登录类型（loginType）</li>
     *   <li><strong>异常传播</strong> - 透明传播认证异常</li>
     * </ul>
     *
     * <h3>认证流程：</h3>
     * <ol>
     *   <li>接收用户名和登录类型参数</li>
     *   <li>调用用户详情服务的认证方法</li>
     *   <li>返回认证结果或抛出认证异常</li>
     * </ol>
     *
     * @param username 用户名，用于用户身份识别
     * @param loginType 登录类型，指定认证方式
     * @return 认证成功的用户信息对象
     * @throws AuthenticationException 当认证失败时抛出
     *
     * @see UserDetailService#loginByUsername(String, String)
     * @see AuthenticationException
     */
    @Override
    public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        return userDetailService.loginByUsername(username, loginType);
    }
}
