package com.lambda.security.service;

import cn.dev33.satoken.stp.StpInterface;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.security.exception.AuthenticationException;
import java.util.List;

/**
 * 用户详情服务接口
 * <p>
 * 该接口继承自Sa-Token的{@link StpInterface}，定义了用户认证和权限管理的核心方法。
 * 它是Lambda Cloud安全框架中用户管理的核心接口，负责用户登录认证、权限获取等功能。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>用户认证：</strong>提供多种用户登录认证方式</li>
 *   <li><strong>权限管理：</strong>集成Sa-Token的权限管理功能</li>
 *   <li><strong>扩展支持：</strong>支持业务系统的自定义用户逻辑</li>
 *   <li><strong>统一接口：</strong>为不同登录方式提供统一的接口规范</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>用户名登录：</strong>支持传统的用户名密码登录</li>
 *   <li><strong>手机号登录：</strong>支持手机号验证码登录</li>
 *   <li><strong>权限获取：</strong>获取用户的权限列表</li>
 *   <li><strong>角色获取：</strong>获取用户的角色列表</li>
 * </ul>
 *
 * <h3>Sa-Token集成：</h3>
 * <p>
 * 通过继承{@link StpInterface}，该接口与Sa-Token框架深度集成，
 * 提供了权限验证、角色检查等功能。Sa-Token会自动调用这些方法来进行权限校验。
 * </p>
 *
 * <h3>实现示例：</h3>
 * <pre>{@code
 * @Service
 * public class UserDetailServiceImpl implements UserDetailService {
 *
 *     @Autowired
 *     private UserService userService;
 *
 *     @Autowired
 *     private PasswordEncoder passwordEncoder;
 *
 *     @Override
 *     public LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
 *         // 1. 查找用户
 *         User user = userService.findByUsername(username);
 *         if (user == null) {
 *             throw new AuthenticationException("用户不存在");
 *         }
 *
 *         // 2. 检查用户状态
 *         if (!user.isEnabled()) {
 *             throw new AuthenticationException("用户已被禁用");
 *         }
 *
 *         // 3. 转换为登录用户对象
 *         return convertToLoginUser(user);
 *     }
 *
 *     @Override
 *     public LoginUser loginByMobile(String mobile, String loginType) throws AuthenticationException {
 *         // 手机号登录逻辑
 *         User user = userService.findByMobile(mobile);
 *         if (user == null) {
 *             throw new AuthenticationException("手机号未注册");
 *         }
 *         return convertToLoginUser(user);
 *     }
 *
 *     @Override
 *     public List<String> getPermissionList(Object loginId, String loginType) {
 *         // 获取用户权限列表
 *         return userService.getUserPermissions(loginId);
 *     }
 *
 *     @Override
 *     public List<String> getRoleList(Object loginId, String loginType) {
 *         // 获取用户角色列表
 *         return userService.getUserRoles(loginId);
 *     }
 * }
 * }</pre>
 *
 * <h3>登录类型支持：</h3>
 * <ul>
 *   <li><strong>用户登录：</strong>普通用户的登录认证</li>
 *   <li><strong>管理员登录：</strong>管理员的登录认证</li>
 *   <li><strong>系统登录：</strong>系统内部服务的登录认证</li>
 *   <li><strong>第三方登录：</strong>配合第三方登录使用</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li><strong>密码加密：</strong>支持多种密码加密算法</li>
 *   <li><strong>账户锁定：</strong>支持账户锁定和解锁机制</li>
 *   <li><strong>权限缓存：</strong>支持权限信息的缓存优化</li>
 *   <li><strong>多端登录：</strong>支持多设备、多端登录管理</li>
 * </ul>
 *
 * <h3>默认实现说明：</h3>
 * <p>
 * 接口提供了默认的方法实现，这些默认实现会抛出异常或返回空列表，
 * 业务系统需要根据实际需求重写相应的方法。
 * </p>
 *
 * @author jin
 * @see StpInterface
 * @see LoginUser
 * @see AuthenticationException
 */
public interface UserDetailService extends StpInterface {

    /**
     * 根据用户名进行登录认证
     * <p>
     * 该方法实现基于用户名的登录认证逻辑。通常与密码验证配合使用，
     * 但具体的密码验证逻辑由调用方处理，此方法主要负责用户查找和状态检查。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>用户查找：</strong>根据用户名查找系统中的用户记录</li>
     *   <li><strong>用户验证：</strong>验证用户是否存在且状态正常</li>
     *   <li><strong>权限加载：</strong>加载用户的角色和权限信息</li>
     *   <li><strong>对象转换：</strong>将用户信息转换为LoginUser对象</li>
     * </ol>
     *
     * <h3>验证内容：</h3>
     * <ul>
     *   <li>用户是否存在</li>
     *   <li>用户是否已启用</li>
     *   <li>用户是否已锁定</li>
     *   <li>用户是否已过期</li>
     * </ul>
     *
     * <h3>默认实现：</h3>
     * <p>
     * 默认实现会抛出{@link AuthenticationException}异常，提示功能未实现。
     * 业务系统需要重写此方法以提供具体的用户名登录逻辑。
     * </p>
     *
     * @param username 用户名，用于查找用户的唯一标识
     * @param loginType 登录类型，用于区分不同的登录场景（如：user、admin、system等）
     * @return 认证成功的登录用户对象，包含用户基本信息和权限
     * @throws AuthenticationException 当用户认证失败时抛出，包含具体的失败原因
     */
    default LoginUser loginByUsername(String username, String loginType) throws AuthenticationException {
        throw new AuthenticationException("用户名登录暂未实现！");
    }

    /**
     * 根据手机号进行登录认证
     * <p>
     * 该方法实现基于手机号的登录认证逻辑。通常与短信验证码配合使用，
     * 提供更便捷的登录方式。此方法主要负责手机号用户的查找和状态检查。
     * </p>
     *
     * <h3>处理流程：</h3>
     * <ol>
     *   <li><strong>手机号验证：</strong>验证手机号格式的有效性</li>
     *   <li><strong>用户查找：</strong>根据手机号查找系统中的用户记录</li>
     *   <li><strong>用户验证：</strong>验证用户是否存在且状态正常</li>
     *   <li><strong>权限加载：</strong>加载用户的角色和权限信息</li>
     *   <li><strong>对象转换：</strong>将用户信息转换为LoginUser对象</li>
     * </ol>
     *
     * <h3>应用场景：</h3>
     * <ul>
     *   <li><strong>短信登录：</strong>配合短信验证码进行登录</li>
     *   <li><strong>快速登录：</strong>为用户提供快速登录方式</li>
     *   <li><strong>找回密码：</strong>通过手机号进行密码重置</li>
     *   <li><strong>新用户注册：</strong>通过手机号注册新用户</li>
     * </ul>
     *
     * <h3>安全考虑：</h3>
     * <ul>
     *   <li>验证码的有效期和使用次数限制</li>
     *   <li>手机号的格式和归属地验证</li>
     *   <li>防止恶意刷验证码攻击</li>
     *   <li>用户隐私信息的保护</li>
     * </ul>
     *
     * <h3>默认实现：</h3>
     * <p>
     * 默认实现会抛出{@link AuthenticationException}异常，提示功能未实现。
     * 业务系统需要重写此方法以提供具体的手机号登录逻辑。
     * </p>
     *
     * @param mobile 手机号，用于查找用户的手机号码
     * @param loginType 登录类型，用于区分不同的登录场景（如：user、admin、system等）
     * @return 认证成功的登录用户对象，包含用户基本信息和权限
     * @throws AuthenticationException 当用户认证失败时抛出，包含具体的失败原因
     */
    default LoginUser loginByMobile(String mobile, String loginType) throws AuthenticationException {
        throw new AuthenticationException("手机号登录暂未实现！");
    }

    /**
     * 获取用户权限列表
     * <p>
     * 该方法是Sa-Token权限验证的核心方法，用于获取指定用户的权限列表。
     * Sa-Token在进行权限校验时会自动调用此方法来获取用户的权限信息。
     * </p>
     *
     * <h3>权限格式：</h3>
     * <ul>
     *   <li><strong>资源权限：</strong>如"user:read"、"user:write"、"order:delete"</li>
     *   <li><strong>功能权限：</strong>如"system:config"、"report:export"</li>
     *   <li><strong>数据权限：</strong>如"dept:1"、"org:admin"</li>
     *   <li><strong>API权限：</strong>如"api:/user/list"、"api:/order/create"</li>
     * </ul>
     *
     * <h3>权限来源：</h3>
     * <ul>
     *   <li><strong>直接权限：</strong>直接分配给用户的权限</li>
     *   <li><strong>角色权限：</strong>通过角色继承的权限</li>
     *   <li><strong>组织权限：</strong>通过组织结构继承的权限</li>
     *   <li><strong>动态权限：</strong>根据业务规则动态计算的权限</li>
     * </ul>
     *
     * <h3>缓存策略：</h3>
     * <p>
     * 建议对权限列表进行缓存，以提高权限校验的性能。
     * 当用户权限发生变化时，需要及时清除相关缓存。
     * </p>
     *
     * <h3>默认实现：</h3>
     * <p>
     * 默认实现返回空列表，表示用户没有任何权限。
     * 业务系统需要重写此方法以提供具体的权限获取逻辑。
     * </p>
     *
     * @param loginId 登录用户ID，通常是用户的唯一标识
     * @param loginType 登录类型，用于区分不同类型的用户（如：user、admin、system等）
     * @return 用户权限列表，每个元素代表一个权限标识
     */
    @Override
    default List<String> getPermissionList(Object loginId, String loginType) {
        return List.of();
    }

    /**
     * 获取用户角色列表
     * <p>
     * 该方法是Sa-Token角色验证的核心方法，用于获取指定用户的角色列表。
     * Sa-Token在进行角色校验时会自动调用此方法来获取用户的角色信息。
     * </p>
     *
     * <h3>角色类型：</h3>
     * <ul>
     *   <li><strong>系统角色：</strong>如"admin"、"user"、"guest"</li>
     *   <li><strong>业务角色：</strong>如"manager"、"employee"、"customer"</li>
     *   <li><strong>功能角色：</strong>如"auditor"、"operator"、"viewer"</li>
     *   <li><strong>组织角色：</strong>如"dept_leader"、"team_member"</li>
     * </ul>
     *
     * <h3>角色层次：</h3>
     * <p>
     * 角色可以具有层次结构，高级角色可以继承低级角色的权限。
     * 例如：admin > manager > employee > user
     * </p>
     *
     * <h3>角色来源：</h3>
     * <ul>
     *   <li><strong>直接分配：</strong>直接分配给用户的角色</li>
     *   <li><strong>组织继承：</strong>通过组织结构继承的角色</li>
     *   <li><strong>临时角色：</strong>临时授予的角色（有时效性）</li>
     *   <li><strong>默认角色：</strong>系统默认分配的角色</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li><strong>权限控制：</strong>基于角色的访问控制（RBAC）</li>
     *   <li><strong>功能限制：</strong>根据角色限制功能访问</li>
     *   <li><strong>数据过滤：</strong>根据角色过滤数据范围</li>
     *   <li><strong>界面展示：</strong>根据角色展示不同的界面元素</li>
     * </ul>
     *
     * <h3>缓存策略：</h3>
     * <p>
     * 建议对角色列表进行缓存，以提高角色校验的性能。
     * 当用户角色发生变化时，需要及时清除相关缓存。
     * </p>
     *
     * <h3>默认实现：</h3>
     * <p>
     * 默认实现返回空列表，表示用户没有任何角色。
     * 业务系统需要重写此方法以提供具体的角色获取逻辑。
     * </p>
     *
     * @param loginId 登录用户ID，通常是用户的唯一标识
     * @param loginType 登录类型，用于区分不同类型的用户（如：user、admin、system等）
     * @return 用户角色列表，每个元素代表一个角色标识
     */
    @Override
    default List<String> getRoleList(Object loginId, String loginType) {
        return List.of();
    }
}
