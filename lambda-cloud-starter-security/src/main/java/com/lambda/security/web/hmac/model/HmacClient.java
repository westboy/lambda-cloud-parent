package com.lambda.security.web.hmac.model;

import cn.hutool.core.util.StrUtil;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.core.utils.HmacGenerator;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;

/**
 * HMAC客户端认证信息模型类
 *
 * <p>该类实现了LoginUser接口，用于存储和管理HMAC认证方式下的客户端信息。
 * 主要用于API签名认证场景，提供基于应用ID和密钥的安全认证机制。</p>
 *
 * <h3>设计目标</h3>
 * <ul>
 *   <li><strong>安全性</strong>：基于密钥的签名认证，防止API被恶意调用</li>
 *   <li><strong>灵活性</strong>：支持多客户端管理和白名单控制</li>
 *   <li><strong>可控性</strong>：支持客户端启用/禁用和过期时间控制</li>
 *   <li><strong>兼容性</strong>：实现LoginUser接口，与现有认证体系兼容</li>
 * </ul>
 *
 * <h3>主要功能</h3>
 * <ul>
 *   <li><strong>身份标识</strong>：通过appid唯一标识客户端</li>
 *   <li><strong>密钥管理</strong>：存储用于签名验证的密钥</li>
 *   <li><strong>访问控制</strong>：支持主机白名单和启用状态控制</li>
 *   <li><strong>生命周期</strong>：支持客户端过期时间管理</li>
 *   <li><strong>多租户</strong>：支持租户隔离</li>
 * </ul>
 *
 * <h3>认证流程</h3>
 * <ol>
 *   <li>客户端使用appid和secret生成请求签名</li>
 *   <li>服务端根据appid查找对应的HmacClient</li>
 *   <li>验证客户端状态（启用、未过期）</li>
 *   <li>检查请求来源是否在白名单中</li>
 *   <li>使用secret验证请求签名</li>
 *   <li>认证通过后允许访问</li>
 * </ol>
 *
 * <h3>安全特性</h3>
 * <ul>
 *   <li><strong>密钥保护</strong>：secret字段使用@JsonIgnore防止泄露</li>
 *   <li><strong>白名单控制</strong>：限制允许访问的主机地址</li>
 *   <li><strong>状态控制</strong>：支持启用/禁用客户端</li>
 *   <li><strong>时效控制</strong>：支持设置客户端过期时间</li>
 * </ul>
 *
 * <h3>使用示例</h3>
 * <pre>{@code
 * // 创建HMAC客户端
 * HmacClient client = new HmacClient("app123", "secret456");
 * client.setNickname("测试应用");
 * client.setHosts("192.168.1.100,192.168.1.101");
 * client.setTenantId("tenant001");
 *
 * // 检查白名单
 * Set<String> whitelist = client.getWhitelist();
 * boolean allowed = whitelist.contains("192.168.1.100");
 * }</pre>
 *
 * <h3>配置要求</h3>
 * <ul>
 *   <li>appid：全局唯一的应用标识</li>
 *   <li>secret：足够复杂的密钥，建议使用随机生成</li>
 *   <li>hosts：可选的IP白名单，多个IP用逗号分隔</li>
 *   <li>expired：过期时间，默认设置为9999年</li>
 * </ul>
 *
 * <h3>线程安全</h3>
 * <p>该类是线程安全的，所有字段都是基本类型或不可变对象。</p>
 *
 * @author jpjoo
 * @see LoginUser
 * @see HmacGenerator
 * @since 1.0.0
 */
@SuppressFBWarnings(
        value = {"EI_EXPOSE_REP"},
        justification = "springboot properties")
@Getter
@Setter
public class HmacClient implements LoginUser {

    /**
     * 应用标识符
     * <p>
     * 用于唯一标识HMAC客户端的应用ID。
     * 在整个系统中应保证唯一性，通常由系统自动生成或管理员分配。
     * 用作HMAC签名认证的身份标识，客户端在请求时需要提供此ID。
     * </p>
     *
     * <h4>特性</h4>
     * <ul>
     *   <li><strong>唯一性</strong>：在系统中必须唯一</li>
     *   <li><strong>不可变</strong>：一旦分配通常不允许修改</li>
     *   <li><strong>可读性</strong>：建议使用有意义的命名规则</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>API请求签名时的身份标识</li>
     *   <li>客户端配置管理的主键</li>
     *   <li>日志记录和审计追踪</li>
     * </ul>
     */
    private String appid;

    /**
     * 应用密钥
     * <p>
     * 用于HMAC签名验证的密钥。
     * 该字段包含敏感信息，在JSON序列化时会被忽略以防止泄露。
     * 客户端使用此密钥生成请求签名，服务端使用相同密钥验证签名。
     * </p>
     *
     * <h4>安全要求</h4>
     * <ul>
     *   <li><strong>复杂性</strong>：应使用足够复杂的随机字符串</li>
     *   <li><strong>保密性</strong>：严格保密，不得泄露给第三方</li>
     *   <li><strong>定期更换</strong>：建议定期更换以提高安全性</li>
     * </ul>
     *
     * <h4>存储建议</h4>
     * <ul>
     *   <li>数据库中应加密存储</li>
     *   <li>传输过程中使用HTTPS</li>
     *   <li>日志中不应记录明文密钥</li>
     * </ul>
     */
    private String secret;

    /**
     * 客户端昵称
     * <p>
     * 用于标识客户端的友好名称，便于管理和识别。
     * 通常用于管理界面显示，帮助管理员快速识别不同的客户端应用。
     * </p>
     *
     * <h4>特性</h4>
     * <ul>
     *   <li><strong>可读性</strong>：使用有意义的名称</li>
     *   <li><strong>可修改</strong>：可以根据需要修改</li>
     *   <li><strong>非唯一</strong>：允许重复，仅用于显示</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>管理界面的客户端列表显示</li>
     *   <li>日志记录中的友好标识</li>
     *   <li>报表和统计中的分组标识</li>
     * </ul>
     */
    private String nickname;

    /**
     * 主机白名单
     * <p>
     * 允许访问的主机地址列表，多个地址用逗号分隔。
     * 用于限制客户端的访问来源，提高API安全性。
     * 空值表示不限制访问来源。
     * </p>
     *
     * <h4>格式要求</h4>
     * <ul>
     *   <li><strong>IP地址</strong>：支持IPv4和IPv6格式</li>
     *   <li><strong>域名</strong>：支持完整域名</li>
     *   <li><strong>分隔符</strong>：多个地址用英文逗号分隔</li>
     * </ul>
     *
     * <h4>示例格式</h4>
     * <pre>
     * "192.168.1.100,192.168.1.101"
     * "api.example.com,backup.example.com"
     * "192.168.1.0/24"
     * </pre>
     *
     * <h4>安全考虑</h4>
     * <ul>
     *   <li>建议使用具体IP而非通配符</li>
     *   <li>定期审查和更新白名单</li>
     *   <li>记录访问来源用于安全审计</li>
     * </ul>
     */
    private String hosts;

    /**
     * 过期时间
     * <p>
     * 客户端的过期时间，超过此时间后客户端将无法进行认证。
     * 用于控制客户端的生命周期，提高系统安全性。
     * 默认设置为9999年12月31日，表示长期有效。
     * </p>
     *
     * <h4>时间管理</h4>
     * <ul>
     *   <li><strong>时区处理</strong>：使用系统默认时区</li>
     *   <li><strong>精度</strong>：精确到日期级别</li>
     *   <li><strong>检查频率</strong>：每次认证时检查</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>临时客户端的访问控制</li>
     *   <li>测试环境的时间限制</li>
     *   <li>合同到期的自动失效</li>
     * </ul>
     *
     * <h4>最佳实践</h4>
     * <ul>
     *   <li>生产环境设置合理的过期时间</li>
     *   <li>提前通知即将过期的客户端</li>
     *   <li>建立过期客户端的清理机制</li>
     * </ul>
     */
    private Date expired;

    /**
     * 启用状态
     * <p>
     * 标识客户端是否启用。
     * true表示客户端已启用，可以进行认证；
     * false表示客户端已禁用，无法进行认证。
     * </p>
     *
     * <h4>状态控制</h4>
     * <ul>
     *   <li><strong>即时生效</strong>：状态变更立即生效</li>
     *   <li><strong>可逆操作</strong>：可以随时启用或禁用</li>
     *   <li><strong>安全机制</strong>：紧急情况下快速禁用客户端</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>客户端维护期间的临时禁用</li>
     *   <li>安全事件发生时的紧急禁用</li>
     *   <li>新客户端的逐步启用</li>
     * </ul>
     */
    private boolean enabled;

    /**
     * 租户标识符
     * <p>
     * 用于多租户系统中的数据隔离。
     * 标识客户端所属的租户，确保不同租户的数据和资源相互隔离。
     * 空值表示系统级客户端，不属于特定租户。
     * </p>
     *
     * <h4>多租户特性</h4>
     * <ul>
     *   <li><strong>数据隔离</strong>：确保租户间数据安全</li>
     *   <li><strong>资源隔离</strong>：限制跨租户访问</li>
     *   <li><strong>权限控制</strong>：基于租户的权限管理</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>SaaS平台的租户隔离</li>
     *   <li>企业内部的部门隔离</li>
     *   <li>合作伙伴的数据隔离</li>
     * </ul>
     */
    private String tenantId;

    /**
     * 默认构造函数
     * <p>
     * 创建一个空的HMAC客户端实例。
     * 所有字段将使用默认值，需要后续通过setter方法设置具体值。
     * </p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>Spring框架的Bean实例化</li>
     *   <li>JSON反序列化</li>
     *   <li>需要逐步设置属性的场景</li>
     * </ul>
     */
    public HmacClient() {}

    /**
     * 带参数的构造函数
     * <p>
     * 使用指定的应用ID和密钥创建HMAC客户端实例。
     * 自动设置客户端为启用状态，过期时间设置为9999年12月31日。
     * </p>
     *
     * @param appid 应用标识符，不能为空
     * @param secret 应用密钥，不能为空
     *
     * <h4>初始化设置</h4>
     * <ul>
     *   <li><strong>enabled</strong>：自动设置为true（启用状态）</li>
     *   <li><strong>expired</strong>：设置为9999-12-31（长期有效）</li>
     *   <li><strong>其他字段</strong>：保持默认值null</li>
     * </ul>
     *
     * <h4>使用示例</h4>
     * <pre>{@code
     * HmacClient client = new HmacClient("app123", "secretKey456");
     * // client.enabled = true
     * // client.expired = 9999-12-31
     * }</pre>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>appid和secret不应为空或空字符串</li>
     *   <li>secret应使用足够复杂的随机字符串</li>
     *   <li>创建后可通过setter方法修改其他属性</li>
     * </ul>
     */
    public HmacClient(String appid, String secret) {
        this.appid = appid;
        this.secret = secret;
        this.enabled = true;
        this.expired = Date.from(
                LocalDate.of(9999, 12, 31).atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * 获取认证凭据
     * <p>
     * 实现LoginUser接口的方法，返回应用密钥作为认证凭据。
     * 使用@JsonIgnore注解防止在JSON序列化时泄露密钥信息。
     * </p>
     *
     * @return 应用密钥作为认证凭据
     *
     * <h4>安全特性</h4>
     * <ul>
     *   <li><strong>JSON忽略</strong>：防止序列化时泄露密钥</li>
     *   <li><strong>接口实现</strong>：与LoginUser接口兼容</li>
     *   <li><strong>敏感信息</strong>：返回的是敏感的认证信息</li>
     * </ul>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>HMAC签名验证过程</li>
     *   <li>认证框架的凭据检查</li>
     *   <li>密钥比对和验证</li>
     * </ul>
     */
    @JsonIgnore
    @Override
    public String getCredentials() {
        return this.secret;
    }

    /**
     * 获取组织ID
     * <p>
     * 实现LoginUser接口的方法。
     * 在HMAC客户端实现中，返回空字符串表示不使用组织概念。
     * </p>
     *
     * @return 空字符串，表示不使用组织概念
     *
     * <h4>设计说明</h4>
     * <p>HMAC客户端认证主要用于应用间的API调用，
     * 通常不涉及复杂的组织结构管理，因此返回空字符串。</p>
     *
     * <h4>扩展建议</h4>
     * <p>如果需要支持组织概念，可以：</p>
     * <ul>
     *   <li>添加orgId字段</li>
     *   <li>修改此方法返回实际的组织ID</li>
     *   <li>在认证逻辑中加入组织验证</li>
     * </ul>
     */
    @Override
    public String getOrgId() {
        return "";
    }

    /**
     * 获取账户锁定状态
     * <p>
     * 实现LoginUser接口的方法。
     * 在HMAC客户端实现中，始终返回false，表示账户不会被锁定。
     * 使用@JsonIgnore注解防止在JSON序列化时包含此信息。
     * </p>
     *
     * @return false，表示账户不会被锁定
     *
     * <h4>设计理念</h4>
     * <p>HMAC客户端认证基于密钥验证，不采用传统的账户锁定机制。
     * 安全控制主要通过以下方式实现：</p>
     * <ul>
     *   <li><strong>启用状态</strong>：通过enabled字段控制</li>
     *   <li><strong>过期时间</strong>：通过expired字段控制</li>
     *   <li><strong>白名单</strong>：通过hosts字段控制</li>
     * </ul>
     *
     * <h4>扩展建议</h4>
     * <p>如果需要支持锁定机制，可以：</p>
     * <ul>
     *   <li>添加locked字段</li>
     *   <li>修改此方法返回实际的锁定状态</li>
     *   <li>在认证逻辑中加入锁定检查</li>
     * </ul>
     */
    @JsonIgnore
    @Override
    public Boolean getAccountLocked() {
        return false;
    }

    /**
     * 获取账户过期状态
     * <p>
     * 实现LoginUser接口的方法。
     * 在HMAC客户端实现中，始终返回false，表示不使用此过期检查机制。
     * 使用@JsonIgnore注解防止在JSON序列化时包含此信息。
     * </p>
     *
     * @return false，表示不使用此过期检查机制
     *
     * <h4>过期控制说明</h4>
     * <p>HMAC客户端的过期控制通过expired字段实现，
     * 而不是通过LoginUser接口的getAccountExpired()方法。
     * 这样设计的原因：</p>
     * <ul>
     *   <li><strong>明确性</strong>：expired字段更直观</li>
     *   <li><strong>灵活性</strong>：可以自定义过期逻辑</li>
     *   <li><strong>一致性</strong>：与其他字段保持一致的命名风格</li>
     * </ul>
     *
     * <h4>实际过期检查</h4>
     * <p>实际的过期检查应该在认证逻辑中进行：</p>
     * <pre>{@code
     * if (client.getExpired() != null &&
     *     client.getExpired().before(new Date())) {
     *     // 客户端已过期
     * }
     * }</pre>
     */
    @JsonIgnore
    @Override
    public Boolean getAccountExpired() {
        return false;
    }

    /**
     * 获取主机白名单集合
     * <p>
     * 将hosts字符串解析为主机地址集合。
     * 如果hosts为空或空白，返回空集合表示不限制访问来源。
     * 使用@JsonIgnore注解防止在JSON序列化时包含此计算结果。
     * </p>
     *
     * @return 主机地址集合，空集合表示不限制
     *
     * <h4>解析逻辑</h4>
     * <ol>
     *   <li>检查hosts字段是否为空或空白</li>
     *   <li>如果为空，返回空的HashSet</li>
     *   <li>如果不为空，按逗号分割并转换为Set</li>
     *   <li>使用HashSet确保地址唯一性</li>
     * </ol>
     *
     * <h4>使用示例</h4>
     * <pre>{@code
     * HmacClient client = new HmacClient();
     * client.setHosts("192.168.1.100,192.168.1.101");
     * Set<String> whitelist = client.getWhitelist();
     * // whitelist包含两个IP地址
     *
     * boolean allowed = whitelist.contains("192.168.1.100");
     * }</pre>
     *
     * <h4>注意事项</h4>
     * <ul>
     *   <li>返回的是新创建的Set，修改不会影响原始数据</li>
     *   <li>地址格式验证需要在其他地方进行</li>
     *   <li>空白字符会被自动处理</li>
     * </ul>
     *
     * <h4>性能考虑</h4>
     * <p>每次调用都会重新解析字符串，如果频繁调用建议缓存结果。</p>
     */
    @JsonIgnore
    public Set<String> getWhitelist() {
        if (StrUtil.isBlank(hosts)) {
            return new HashSet<>();
        }
        return new HashSet<>(Arrays.asList(hosts.split(",")));
    }

    /**
     * 获取名称标识
     * <p>
     * 实现LoginUser接口的方法，返回应用ID作为名称标识。
     * 在HMAC认证体系中，应用ID即为客户端的唯一标识。
     * </p>
     *
     * @return 应用ID作为名称标识
     *
     * <h4>接口实现</h4>
     * <p>该方法实现了LoginUser接口的getName()方法，
     * 与getUsername()方法返回相同的值，保持一致性。</p>
     *
     * <h4>使用场景</h4>
     * <ul>
     *   <li>日志记录中的客户端标识</li>
     *   <li>审计追踪中的操作主体</li>
     *   <li>权限管理中的主体识别</li>
     * </ul>
     *
     * <h4>设计说明</h4>
     * <p>在HMAC客户端中，name和username概念相同，
     * 都指向应用ID，这样设计简化了接口实现。</p>
     */
    @Override
    public String getName() {
        return appid;
    }
}
