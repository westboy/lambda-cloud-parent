package com.lambda.security.inteceptor;

import cn.dev33.satoken.stp.StpLogic;
import com.lambda.cloud.core.principal.LoginUser;

/**
 * 安全拦截器接口
 * <p>
 * 该接口定义了自定义安全拦截器的标准规范，用于在Sa-Token框架的基础上
 * 实现更加灵活和业务相关的安全检查逻辑。
 * </p>
 *
 * <h3>设计目的：</h3>
 * <ul>
 *   <li><strong>业务安全扩展：</strong>在Sa-Token基础上扩展业务相关的安全检查</li>
 *   <li><strong>权限细化控制：</strong>实现更加细粒度的权限控制逻辑</li>
 *   <li><strong>自定义拦截逻辑：</strong>支持自定义的安全拦截和验证规则</li>
 *   <li><strong>框架解耦：</strong>将业务安全逻辑与Sa-Token框架解耦</li>
 * </ul>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li><strong>请求拦截：</strong>对HTTP请求进行安全拦截处理</li>
 *   <li><strong>权限验证：</strong>验证用户是否具有访问特定资源的权限</li>
 *   <li><strong>安全检查：</strong>执行自定义的安全检查逻辑</li>
 *   <li><strong>上下文处理：</strong>处理安全相关的上下文信息</li>
 * </ul>
 *
 * <h3>应用场景：</h3>
 * <ul>
 *   <li><strong>角色权限控制：</strong>基于用户角色进行权限控制</li>
 *   <li><strong>资源访问控制：</strong>控制对特定资源的访问权限</li>
 *   <li><strong>API安全检查：</strong>对API接口进行安全验证</li>
 *   <li><strong>业务规则验证：</strong>验证业务相关的安全规则</li>
 *   <li><strong>审计日志记录：</strong>记录安全相关的操作日志</li>
 * </ul>
 *
 * <h3>实现示例：</h3>
 * <pre>{@code
 * @Component
 * public class CustomSecureInterceptor implements SecureInterceptor {
 *
 *     @Override
 *     public void handle(Object handler, StpLogic stpLogic, LoginUser operator) {
 *         if (handler instanceof HandlerMethod handlerMethod) {
 *             // 检查方法级别的权限注解
 *             RequiresPermissions permission = handlerMethod.getMethodAnnotation(RequiresPermissions.class);
 *             if (permission != null) {
 *                 // 验证用户权限
 *                 checkPermissions(operator, permission.value());
 *             }
 *
 *             // 检查角色权限
 *             RequiresRoles roles = handlerMethod.getMethodAnnotation(RequiresRoles.class);
 *             if (roles != null) {
 *                 // 验证用户角色
 *                 checkRoles(operator, roles.value());
 *             }
 *         }
 *     }
 *
 *     private void checkPermissions(LoginUser user, String[] permissions) {
 *         // 实现权限检查逻辑
 *     }
 *
 *     private void checkRoles(LoginUser user, String[] roles) {
 *         // 实现角色检查逻辑
 *     }
 * }
 * }</pre>
 *
 * <h3>集成方式：</h3>
 * <ul>
 *   <li>通过{@link SaTokenInterceptor}与Sa-Token框架集成</li>
 *   <li>在Spring容器中注册实现类</li>
 *   <li>配置到Sa-Token的拦截器链中</li>
 * </ul>
 *
 * @author jpjoo
 * @see SaTokenInterceptor
 * @see StpLogic
 * @see LoginUser
 */
public interface SecureInterceptor {

    /**
     * 处理安全拦截逻辑
     * <p>
     * 该方法是安全拦截器的核心处理方法，负责执行具体的安全检查和权限验证逻辑。
     * 会接收请求处理器、Sa-Token逻辑实例和当前登录用户信息作为参数。
     * </p>
     *
     * <h3>常见实现模式：</h3>
     * <pre>{@code
     * public void handle(Object handler, StpLogic stpLogic, LoginUser operator) {
     *     // 1. 类型检查
     *     if (!(handler instanceof HandlerMethod handlerMethod)) {
     *         return;
     *     }
     *
     *     // 2. 注解解析
     *     Method method = handlerMethod.getMethod();
     *     Class<?> clazz = handlerMethod.getBeanType();
     *
     *     // 3. 权限检查
     *     checkPermissions(method, clazz, operator);
     *
     *     // 4. 角色检查
     *     checkRoles(method, clazz, operator);
     * }
     * }</pre>
     *
     * @param handler 请求处理器对象，通常为HandlerMethod实例
     * @param stpLogic Sa-Token的逻辑处理实例，提供权限检查功能
     * @param operator 当前登录用户对象，包含用户身份和权限信息
     * @throws cn.dev33.satoken.exception.NotPermissionException 当用户权限不足时抛出
     * @throws cn.dev33.satoken.exception.NotRoleException 当用户角色不匹配时抛出
     * @throws cn.dev33.satoken.exception.NotLoginException 当用户未登录时抛出
     */
    void handle(Object handler, StpLogic stpLogic, LoginUser operator);
}
