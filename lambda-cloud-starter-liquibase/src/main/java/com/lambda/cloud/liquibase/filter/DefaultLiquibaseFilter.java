package com.lambda.cloud.liquibase.filter;

import cn.hutool.core.io.FileUtil;
import java.util.regex.Pattern;
import liquibase.changelog.IncludeAllFilter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Liquibase变更日志文件过滤器，用于筛选符合命名规范的变更日志文件
 * <p>
 * 该类实现了{@link IncludeAllFilter}接口，用于在Liquibase扫描变更日志文件时
 * 过滤出符合特定命名模式的文件。只有匹配{@code lambda-*-changelog.xml}模式的
 * 文件才会被包含在变更日志执行列表中。
 * 
 * <p>支持的文件命名模式：
 * <ul>
 *   <li>{@code lambda-datasource-changelog.xml} - 数据源相关变更</li>
 *   <li>{@code lambda-user-changelog.xml} - 用户模块变更</li>
 *   <li>{@code lambda-order-changelog.xml} - 订单模块变更</li>
 *   <li>{@code lambda-additional-changelog.xml} - 附加变更</li>
 *   <li>其他符合{@code lambda-{模块名}-changelog.xml}模式的文件</li>
 * </ul>
 * 
 * <p>该过滤器具有以下特性：
 * <ul>
 *   <li>使用预编译的正则表达式提高性能</li>
 *   <li>提供详细的调试日志</li>
 *   <li>安全的异常处理机制</li>
 *   <li>空值和边界条件检查</li>
 * </ul>
 * 
 * @author westboy
 * @version 1.0.0
 * @since 2024-01-01
 * @see IncludeAllFilter
 * @see DefaultLiquibaseComparator
 */
@Slf4j
public class DefaultLiquibaseFilter implements IncludeAllFilter {
    
    /**
     * 变更日志文件名匹配模式：lambda-{模块名}-changelog.xml
     */
    private static final String PATTERN = "lambda-\\w*-changelog\\.xml";
    
    /**
     * 编译后的正则表达式模式，提高性能
     */
    private static final Pattern COMPILED_PATTERN = Pattern.compile(PATTERN);

    /**
     * 判断文件是否应该被包含在变更日志中
     *
     * @param file 文件路径
     * @return 如果文件符合命名规范则返回true，否则返回false
     */
    @Override
    public boolean include(String file) {
        if (!StringUtils.hasText(file)) {
            log.debug("File path is null or empty, excluding from changelog");
            return false;
        }
        
        try {
            String name = FileUtil.getName(file);
            if (!StringUtils.hasText(name)) {
                log.debug("File name is null or empty for path: {}, excluding from changelog", file);
                return false;
            }
            
            boolean matches = COMPILED_PATTERN.matcher(name).matches();
            log.debug("File {} {} the pattern {}", name, matches ? "matches" : "does not match", PATTERN);
            return matches;
        } catch (Exception e) {
            log.warn("Error processing file path: {}, excluding from changelog", file, e);
            return false;
        }
    }
}
