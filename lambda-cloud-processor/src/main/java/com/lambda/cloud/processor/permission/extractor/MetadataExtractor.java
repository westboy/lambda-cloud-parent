package com.lambda.cloud.processor.permission.extractor;

import com.lambda.cloud.processor.permission.model.ApiPermissionMetadata;
import com.lambda.cloud.processor.permission.scanner.AnnotationScanner;
import java.util.*;
import javax.lang.model.element.*;

/**
 * 元数据提取器
 * 
 * <p>从注解中提取权限信息，构建 ApiPermissionMetadata 对象。
 * 
 * @author Lambda Cloud
 */
public class MetadataExtractor {
    
    private final AnnotationScanner scanner;
    
    public MetadataExtractor() {
        this.scanner = new AnnotationScanner();
    }
    
    /**
     * 提取完整的 API 权限元数据
     */
    public ApiPermissionMetadata extract(TypeElement controller, ExecutableElement method) {
        ApiPermissionMetadata metadata = new ApiPermissionMetadata();

        // 提取路径
        String classPath = extractClassPath(controller);
        String methodPath = extractMethodPath(method);
        metadata.setPath(combinePath(classPath, methodPath));

        // 提取 HTTP 方法
        metadata.setMethod(extractHttpMethod(method));

        // 提取权限信息
        metadata.setPermissions(extractPermissions(controller, method));
        metadata.setPermissionLogic(extractPermissionLogic(controller, method));

        // 提取角色信息
        metadata.setRoles(extractRoles(controller, method));

        // 提取认证要求
        metadata.setRequiresAuth(extractRequiresAuth(controller, method));

        // 提取描述信息
        metadata.setDescription(extractDescription(method));

        // 提取分组信息
        metadata.setGroup(extractGroup(controller));

        // 提取 Controller 和方法名
        metadata.setController(controller.getQualifiedName().toString());
        metadata.setMethodName(method.getSimpleName().toString());

        // 检查是否废弃
        metadata.setDeprecated(scanner.isDeprecated(method));
        
        return metadata;
    }
    
    /**
     * 提取类级别的路径
     */
    public String extractClassPath(TypeElement controller) {
        List<String> paths = scanner.scanRequestMapping(controller);
        return paths.isEmpty() ? "" : paths.getFirst();
    }
    
    /**
     * 提取方法级别的路径
     */
    public String extractMethodPath(ExecutableElement method) {
        // 先检查 HTTP 方法映射注解的路径
        List<String> paths = scanner.scanHttpMethodMappingPath(method);
        if (!paths.isEmpty()) {
            return paths.get(0);
        }
        
        // 再检查 @RequestMapping
        paths = scanner.scanRequestMapping(method);
        return paths.isEmpty() ? "" : paths.get(0);
    }
    
    /**
     * 提取 HTTP 方法
     */
    public String extractHttpMethod(ExecutableElement method) {
        String httpMethod = scanner.scanHttpMethodMapping(method);
        return httpMethod != null ? httpMethod : "GET";
    }
    
    /**
     * 提取权限标识列表
     */
    public List<String> extractPermissions(TypeElement controller, ExecutableElement method) {
        Set<String> permissions = new LinkedHashSet<>();
        
        // 提取类级别的权限
        permissions.addAll(scanner.scanSaCheckPermission(controller));
        
        // 提取方法级别的权限
        permissions.addAll(scanner.scanSaCheckPermission(method));
        
        // TODO: 支持自定义 @RequiresPermission 注解
        
        return new ArrayList<>(permissions);
    }
    
    /**
     * 提取权限逻辑（AND/OR）
     */
    public String extractPermissionLogic(TypeElement controller, ExecutableElement method) {
        // 检查方法级别的 @SaCheckRole mode
        String mode = scanner.scanSaCheckRoleMode(method);
        if (mode != null) {
            return mode;
        }
        
        // 检查类级别的 @SaCheckRole mode
        mode = scanner.scanSaCheckRoleMode(controller);
        if (mode != null) {
            return mode;
        }
        
        return "AND";
    }
    
    /**
     * 提取角色标识列表
     */
    public List<String> extractRoles(TypeElement controller, ExecutableElement method) {
        Set<String> roles = new LinkedHashSet<>();
        
        // 提取类级别的角色
        roles.addAll(scanner.scanSaCheckRole(controller));
        
        // 提取方法级别的角色
        roles.addAll(scanner.scanSaCheckRole(method));
        
        return new ArrayList<>(roles);
    }
    
    /**
     * 提取认证要求
     */
    public boolean extractRequiresAuth(TypeElement controller, ExecutableElement method) {
        // 如果方法或类上有 @SaCheckLogin，则需要认证
        if (scanner.requiresAuth(method) || scanner.requiresAuth(controller)) {
            return true;
        }
        
        // 如果有权限或角色要求，默认需要认证
        List<String> permissions = extractPermissions(controller, method);
        List<String> roles = extractRoles(controller, method);
        
        return !permissions.isEmpty() || !roles.isEmpty();
    }
    
    /**
     * 提取接口描述
     */
    public String extractDescription(ExecutableElement method) {
        return scanner.scanOperationSummary(method);
    }
    
    /**
     * 提取分组信息
     */
    public String extractGroup(TypeElement controller) {
        return scanner.scanTagName(controller);
    }
    
    /**
     * 合并路径
     */
    private String combinePath(String classPath, String methodPath) {
        if (classPath == null || classPath.isEmpty()) {
            return normalizePath(methodPath);
        }
        if (methodPath == null || methodPath.isEmpty()) {
            return normalizePath(classPath);
        }
        
        // 移除尾部斜杠
        classPath = classPath.replaceAll("/$", "");
        // 移除开头斜杠
        methodPath = methodPath.replaceAll("^/", "");
        
        return normalizePath(classPath + "/" + methodPath);
    }
    
    /**
     * 规范化路径
     */
    private String normalizePath(String path) {
        if (path == null || path.isEmpty()) {
            return "/";
        }
        // 确保以 / 开头
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        // 移除重复的斜杠
        path = path.replaceAll("/+", "/");
        // 移除引号
        path = path.replaceAll("^\"|\"$", "");
        return path;
    }
}
