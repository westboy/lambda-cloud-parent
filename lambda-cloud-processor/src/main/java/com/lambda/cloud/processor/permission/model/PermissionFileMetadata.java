package com.lambda.cloud.processor.permission.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限文件元数据
 *
 * <p>表示整个权限文件的元数据信息，包含版本、生成时间、模块信息和 API 列表。
 *
 * @author Jin
 */
@Data
@SuppressFBWarnings("EI_EXPOSE_REP")
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

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(String generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getModule() {
        return module;
    }

    public void setModule(String module) {
        this.module = module;
    }

    public String getBasePackage() {
        return basePackage;
    }

    public void setBasePackage(String basePackage) {
        this.basePackage = basePackage;
    }

    public int getTotalApis() {
        return totalApis;
    }

    public void setTotalApis(int totalApis) {
        this.totalApis = totalApis;
    }

    public List<ApiPermissionMetadata> getApis() {
        return apis;
    }

    public void setApis(List<ApiPermissionMetadata> apis) {
        this.apis = apis;
    }
}
