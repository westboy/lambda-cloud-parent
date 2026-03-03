package com.lambda.cloud.processor.permission.model;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限文件元数据
 * 
 * <p>表示整个权限文件的元数据信息，包含版本、生成时间、模块信息和 API 列表。
 * 
 * @author Lambda Cloud
 */
@Data
public class PermissionFileMetadata {
    
    /** 文件格式版本 */
    private String version = "1.0.0";
    
    /** 生成时间（ISO 8601 格式） */
    private String generatedAt;
    
    /** 模块名称 */
    private String module;
    
    /** 基础包路径 */
    private String basePackage;
    
    /** 接口总数 */
    private int totalApis;
    
    /** API 权限列表 */
    private List<ApiPermissionMetadata> apis = new ArrayList<>();
}
