package com.lambda.cloud.processor.permission.scanner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;

/**
 * 注解扫描器
 *
 * <p>负责识别和解析权限相关注解，包括权限注解、路径注解、文档注解等。
 *
 * @author Jin
 */
public class AnnotationScanner {

    /**
     * 检查元素是否有指定注解
     */
    public boolean hasAnnotation(Element element, String annotationName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取注解的字符串值
     */
    public String getAnnotationValue(Element element, String annotationName, String attributeName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationName)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                        Object value = entry.getValue().getValue();
                        return value != null ? value.toString() : null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * 获取注解的字符串数组值
     */
    @SuppressWarnings("unchecked")
    public List<String> getAnnotationArrayValue(Element element, String annotationName, String attributeName) {
        for (AnnotationMirror mirror : element.getAnnotationMirrors()) {
            if (mirror.getAnnotationType().toString().equals(annotationName)) {
                for (Map.Entry<? extends ExecutableElement, ? extends AnnotationValue> entry :
                        mirror.getElementValues().entrySet()) {
                    if (entry.getKey().getSimpleName().toString().equals(attributeName)) {
                        Object value = entry.getValue().getValue();
                        if (value instanceof List) {
                            List<String> result = new ArrayList<>();
                            for (AnnotationValue av : (List<? extends AnnotationValue>) value) {
                                result.add(av.getValue().toString());
                            }
                            return result;
                        }
                    }
                }
            }
        }
        return Collections.emptyList();
    }

    /**
     * 扫描 @RequestMapping 注解
     */
    public List<String> scanRequestMapping(Element element) {
        List<String> paths = new ArrayList<>();

        // 检查 @RequestMapping
        paths.addAll(
                getAnnotationArrayValue(element, "org.springframework.web.bind.annotation.RequestMapping", "value"));
        paths.addAll(
                getAnnotationArrayValue(element, "org.springframework.web.bind.annotation.RequestMapping", "path"));

        return paths;
    }

    /**
     * 扫描 HTTP 方法映射注解
     */
    public String scanHttpMethodMapping(Element element) {
        if (hasAnnotation(element, "org.springframework.web.bind.annotation.GetMapping")) {
            return "GET";
        }
        if (hasAnnotation(element, "org.springframework.web.bind.annotation.PostMapping")) {
            return "POST";
        }
        if (hasAnnotation(element, "org.springframework.web.bind.annotation.PutMapping")) {
            return "PUT";
        }
        if (hasAnnotation(element, "org.springframework.web.bind.annotation.DeleteMapping")) {
            return "DELETE";
        }
        if (hasAnnotation(element, "org.springframework.web.bind.annotation.PatchMapping")) {
            return "PATCH";
        }

        // 检查 @RequestMapping 的 method 属性
        List<String> methods =
                getAnnotationArrayValue(element, "org.springframework.web.bind.annotation.RequestMapping", "method");
        if (!methods.isEmpty()) {
            String method = methods.getFirst();
            // 提取枚举值，如 RequestMethod.GET -> GET
            if (method.contains(".")) {
                method = method.substring(method.lastIndexOf('.') + 1);
            }
            return method;
        }

        return null;
    }

    /**
     * 扫描 HTTP 方法映射注解的路径
     */
    public List<String> scanHttpMethodMappingPath(Element element) {
        List<String> paths = new ArrayList<>();

        String[] annotations = {
            "org.springframework.web.bind.annotation.GetMapping",
            "org.springframework.web.bind.annotation.PostMapping",
            "org.springframework.web.bind.annotation.PutMapping",
            "org.springframework.web.bind.annotation.DeleteMapping",
            "org.springframework.web.bind.annotation.PatchMapping"
        };

        for (String annotation : annotations) {
            paths.addAll(getAnnotationArrayValue(element, annotation, "value"));
            paths.addAll(getAnnotationArrayValue(element, annotation, "path"));
        }

        return paths;
    }

    /**
     * 扫描 @Operation 注解的 summary
     */
    public String scanOperationSummary(Element element) {
        String summary = getAnnotationValue(element, "io.swagger.v3.oas.annotations.Operation", "summary");
        if (summary != null) {
            // 移除引号
            summary = summary.replaceAll("^\"|\"$", "");
        }
        return summary;
    }

    /**
     * 扫描 @Tag 注解的 name
     */
    public String scanTagName(Element element) {
        String name = getAnnotationValue(element, "io.swagger.v3.oas.annotations.tags.Tag", "name");
        if (name != null) {
            // 移除引号
            name = name.replaceAll("^\"|\"$", "");
        }
        return name;
    }

    /**
     * 检查是否需要认证（@SaCheckLogin）
     */
    public boolean requiresAuth(Element element) {
        return hasAnnotation(element, "cn.dev33.satoken.annotation.SaCheckLogin");
    }

    /**
     * 扫描 @SaCheckPermission 注解
     */
    public List<String> scanSaCheckPermission(Element element) {
        return getAnnotationArrayValue(element, "cn.dev33.satoken.annotation.SaCheckPermission", "value");
    }

    /**
     * 扫描 @SaCheckPermission 注解
     */
    public boolean hasPermission(Element element) {
        return hasAnnotation(element, "cn.dev33.satoken.annotation.SaCheckPermission");
    }

    /**
     * 检查方法是否已废弃
     */
    public boolean isDeprecated(Element element) {
        return hasAnnotation(element, "java.lang.Deprecated")
                || hasAnnotation(element, "io.swagger.v3.oas.annotations.Operation")
                        && "true"
                                .equals(getAnnotationValue(
                                        element, "io.swagger.v3.oas.annotations.Operation", "deprecated"));
    }
}
