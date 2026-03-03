package com.lambda.cloud.processor.permission.cache;

import com.lambda.cloud.processor.permission.model.ApiPermissionMetadata;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 缓存条目
 * 
 * <p>用于增量编译，缓存每个 Controller 类的权限信息和签名。
 * 
 * @author Lambda Cloud
 */
@Data
public class CacheEntry {
    
    /** 类签名（用于检测变更，基于类名、注解等计算的哈希值） */
    private String classSignature;
    
    /** 最后修改时间戳 */
    private long lastModified;
    
    /** 该类的权限信息列表 */
    private List<ApiPermissionMetadata> permissions = new ArrayList<>();
    
    /**
     * 创建缓存条目
     * 
     * @param classSignature 类签名
     * @param permissions 权限信息列表
     * @return 缓存条目
     */
    public static CacheEntry of(String classSignature, List<ApiPermissionMetadata> permissions) {
        CacheEntry entry = new CacheEntry();
        entry.setClassSignature(classSignature);
        entry.setLastModified(System.currentTimeMillis());
        entry.setPermissions(permissions);
        return entry;
    }
}
