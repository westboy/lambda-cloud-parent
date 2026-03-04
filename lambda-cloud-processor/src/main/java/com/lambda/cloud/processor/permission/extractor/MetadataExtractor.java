package com.lambda.cloud.processor.permission.extractor;

import cn.hutool.core.collection.CollUtil;
import com.lambda.cloud.processor.permission.model.ApiPermissionMetadata;
import com.lambda.cloud.processor.permission.scanner.AnnotationScanner;
import java.util.*;
import javax.lang.model.element.*;

/**
 * 元数据提取器
 *
 * <p>从注解中提取权限信息，构建 ApiPermissionMetadata 对象。
 *
 * @author Jin
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
        boolean hasPermission = scanner.hasPermission(controller) || scanner.hasPermission(method);

        if (!hasPermission) {
            return null;
        }

        ApiPermissionMetadata metadata = new ApiPermissionMetadata();

        // 提取路径
        String classPath = extractClassPath(controller);
        String methodPath = extractMethodPath(method);
        metadata.setPath(combinePath(classPath, methodPath));

        // 提取 HTTP 方法
        metadata.setMethod(extractHttpMethod(method));

        // 提取描述信息
        metadata.setDescription(extractDescription(method));

        // 提取分组信息
        metadata.setGroup(extractGroup(controller));

        // 提取权限信息
        metadata.setPermissions(extractPermissions(controller, method));

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
            return paths.getFirst();
        }

        // 再检查 @RequestMapping
        paths = scanner.scanRequestMapping(method);
        return paths.isEmpty() ? "" : paths.getFirst();
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
        // 提取类级别的权限
        List<String> permissions = scanner.scanSaCheckPermission(controller);
        if (CollUtil.isNotEmpty(permissions)) {
            return permissions;
        }
        // 提取方法别的权限
        return scanner.scanSaCheckPermission(method);
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
