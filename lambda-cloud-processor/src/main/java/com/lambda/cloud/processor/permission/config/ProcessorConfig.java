package com.lambda.cloud.processor.permission.config;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

/**
 * 权限处理器配置
 *
 * <p>从编译参数中读取配置，控制权限提取器的行为。
 *
 * @author Jin
 */
@Data
@SuppressFBWarnings("EI_EXPOSE_REP")
public class ProcessorConfig {

    /** 是否启用权限提取，默认 true */
    private boolean enabled = true;

    /** 输出路径，默认 META-INF/permissions/api-permissions.json */
    private String outputPath = "META-INF/permissions/api-permissions.json";

    /** 输出格式，默认 json */
    private String outputFormat = "json";

    /** 扫描的基础包路径 */
    private String basePackage;

    /** 包含的路径模式（逗号分隔） */
    private List<String> includePatterns = new ArrayList<>();

    /** 排除的路径模式（逗号分隔） */
    private List<String> excludePatterns = new ArrayList<>();

    /** 自定义注解的全限定名列表 */
    private List<String> customAnnotations = new ArrayList<>();

    /** 模块名称（从 pom.xml 或配置中获取） */
    private String moduleName;
}
