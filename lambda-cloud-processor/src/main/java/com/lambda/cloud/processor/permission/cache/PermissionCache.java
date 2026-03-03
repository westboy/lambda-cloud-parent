package com.lambda.cloud.processor.permission.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lambda.cloud.processor.permission.model.ApiPermissionMetadata;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.security.MessageDigest;
import java.util.*;
import javax.annotation.processing.Filer;
import javax.lang.model.element.*;
import javax.tools.StandardLocation;

/**
 * 权限缓存管理器
 * 
 * <p>用于增量编译，缓存每个 Controller 类的权限信息和签名，避免重复处理未变更的类。
 * 
 * @author Lambda Cloud
 */
public class PermissionCache {
    
    private static final String CACHE_FILE = "permission-cache.json";
    private final Map<String, CacheEntry> cache = new HashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final File cacheFile;
    
    public PermissionCache(String buildDirectory) {
        this.cacheFile = new File(buildDirectory, CACHE_FILE);
        loadCache();
    }
    
    /**
     * 检查类是否已变更
     * 
     * @param classElement 类元素
     * @return true 如果类已变更或不存在缓存
     */
    public boolean isClassChanged(TypeElement classElement) {
        String className = classElement.getQualifiedName().toString();
        String currentSignature = calculateSignature(classElement);
        
        CacheEntry cached = cache.get(className);
        if (cached == null) {
            return true; // 新类，需要处理
        }
        
        return !currentSignature.equals(cached.getClassSignature());
    }
    
    /**
     * 获取缓存的权限信息
     * 
     * @param className 类名
     * @return 缓存的权限信息列表，如果不存在返回空列表
     */
    public List<ApiPermissionMetadata> getCachedPermissions(String className) {
        CacheEntry entry = cache.get(className);
        return entry != null ? entry.getPermissions() : Collections.emptyList();
    }
    
    /**
     * 更新缓存
     * 
     * @param classElement 类元素
     * @param permissions 权限信息列表
     */
    public void updateCache(TypeElement classElement, List<ApiPermissionMetadata> permissions) {
        String className = classElement.getQualifiedName().toString();
        String signature = calculateSignature(classElement);
        
        CacheEntry entry = CacheEntry.of(signature, permissions);
        cache.put(className, entry);
    }
    
    /**
     * 移除缓存条目
     * 
     * @param className 类名
     */
    public void removeCache(String className) {
        cache.remove(className);
    }
    
    /**
     * 保存缓存到文件
     */
    public void saveCache() {
        try {
            // 确保目录存在
            File parentDir = cacheFile.getParentFile();
            if (parentDir != null && !parentDir.exists()) {
                parentDir.mkdirs();
            }
            
            // 写入缓存文件
            objectMapper.writerWithDefaultPrettyPrinter()
                .writeValue(cacheFile, cache);
        } catch (IOException e) {
            // 缓存保存失败不影响编译，只记录警告
            System.err.println("[PermissionCache] Failed to save cache: " + e.getMessage());
        }
    }
    
    /**
     * 从文件加载缓存
     */
    @SuppressWarnings("unchecked")
    private void loadCache() {
        if (!cacheFile.exists()) {
            return;
        }
        
        try {
            Map<String, CacheEntry> loaded = objectMapper.readValue(
                cacheFile,
                objectMapper.getTypeFactory().constructMapType(
                    HashMap.class, String.class, CacheEntry.class
                )
            );
            cache.putAll(loaded);
        } catch (IOException e) {
            // 缓存加载失败不影响编译，只记录警告
            System.err.println("[PermissionCache] Failed to load cache: " + e.getMessage());
        }
    }
    
    /**
     * 计算类签名
     * 
     * <p>基于类名、类级别注解、方法签名和方法注解计算哈希值。
     * 
     * @param classElement 类元素
     * @return 签名字符串（MD5 哈希）
     */
    public String calculateSignature(TypeElement classElement) {
        StringBuilder sb = new StringBuilder();
        
        // 1. 类名
        sb.append(classElement.getQualifiedName());
        
        // 2. 类级别注解
        List<String> classAnnotations = new ArrayList<>();
        for (AnnotationMirror am : classElement.getAnnotationMirrors()) {
            classAnnotations.add(am.toString());
        }
        Collections.sort(classAnnotations);
        classAnnotations.forEach(sb::append);
        
        // 3. 方法签名和注解
        List<String> methodSignatures = new ArrayList<>();
        for (Element element : classElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                ExecutableElement method = (ExecutableElement) element;
                
                // 方法名和参数
                StringBuilder methodSig = new StringBuilder();
                methodSig.append(method.getSimpleName());
                methodSig.append("(");
                for (VariableElement param : method.getParameters()) {
                    methodSig.append(param.asType()).append(",");
                }
                methodSig.append(")");
                
                // 方法注解
                List<String> methodAnnotations = new ArrayList<>();
                for (AnnotationMirror am : method.getAnnotationMirrors()) {
                    methodAnnotations.add(am.toString());
                }
                Collections.sort(methodAnnotations);
                methodAnnotations.forEach(methodSig::append);
                
                methodSignatures.add(methodSig.toString());
            }
        }
        Collections.sort(methodSignatures);
        methodSignatures.forEach(sb::append);
        
        // 计算 MD5 哈希
        return md5(sb.toString());
    }
    
    /**
     * 计算 MD5 哈希
     */
    private String md5(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder hexString = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            // 如果哈希计算失败，返回原始字符串的哈希码
            return String.valueOf(input.hashCode());
        }
    }
    
    /**
     * 获取缓存统计信息
     */
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalEntries", cache.size());
        stats.put("cacheFile", cacheFile.getAbsolutePath());
        stats.put("cacheFileExists", cacheFile.exists());
        return stats;
    }
    
    /**
     * 清空缓存
     */
    public void clear() {
        cache.clear();
        if (cacheFile.exists()) {
            cacheFile.delete();
        }
    }
}
