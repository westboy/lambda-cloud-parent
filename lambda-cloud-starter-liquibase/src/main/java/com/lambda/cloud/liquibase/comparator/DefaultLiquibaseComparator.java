package com.lambda.cloud.liquibase.comparator;

import cn.hutool.core.io.FileUtil;
import java.util.Arrays;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

/**
 * Liquibase变更日志文件比较器，用于确定文件执行顺序
 * <p>
 * 该类实现了{@link Comparator}接口，为Liquibase变更日志文件提供自定义的排序逻辑。
 * 确保关键的基础设施变更文件优先执行，附加变更文件最后执行。
 * 
 * <p>排序规则（按优先级从高到低）：
 * <ol>
 *   <li><strong>强制优先文件</strong>：{@code lambda-datasource-changelog.xml} 等基础设施文件</li>
 *   <li><strong>普通模块文件</strong>：按文件名字典序排序</li>
 *   <li><strong>附加变更文件</strong>：{@code lambda-additional-changelog.xml} 最后执行</li>
 * </ol>
 * 
 * <p>设计原理：
 * <ul>
 *   <li>数据源配置必须最先执行，为后续变更提供基础</li>
 *   <li>业务模块变更按字典序执行，保证可预测性</li>
 *   <li>附加变更通常包含数据修复或补丁，应最后执行</li>
 *   <li>提供完整的空值检查和异常处理</li>
 * </ul>
 * 
 * <p>使用场景：
 * 在{@code lambda-master.xml}中通过{@code comparator}属性指定此比较器，
 * 确保变更日志文件按正确顺序执行。
 * 
 * @author westboy
 * @version 1.0.0
 * @since 2024-01-01
 * @see Comparator
 * @see DefaultLiquibaseFilter
 */
@Slf4j
public class DefaultLiquibaseComparator implements Comparator<String> {
    
    /**
     * 需要强制要求优先级顺序的配置文件
     */
    private static final String[] FORCED_SORT = {"lambda-datasource-changelog.xml"};

    /**
     * 附加变更日志文件名，通常最后执行
     */
    public static final String ADDITIONAL = "lambda-additional-changelog.xml";

    @Override
    public int compare(String o1, String o2) {
        // 空值检查
        if (o1 == null && o2 == null) {
            return 0;
        }
        if (o1 == null) {
            return -1;
        }
        if (o2 == null) {
            return 1;
        }
        
        // 提取文件名
        String name1 = FileUtil.getName(o1);
        String name2 = FileUtil.getName(o2);
        
        // 空值检查
        if (!StringUtils.hasText(name1) && !StringUtils.hasText(name2)) {
            return 0;
        }
        if (!StringUtils.hasText(name1)) {
            return -1;
        }
        if (!StringUtils.hasText(name2)) {
            return 1;
        }
        
        // 相同文件名
        if (name1.equalsIgnoreCase(name2)) {
            return 0;
        }
        
        // 检查是否在强制排序列表中
        int index1 = Arrays.asList(FORCED_SORT).indexOf(name1);
        int index2 = Arrays.asList(FORCED_SORT).indexOf(name2);
        
        if (index1 == 0 && index2 == 0) {
            // 两个都在强制排序列表中，按索引顺序排序
            return 0;
        } else if (index1 == 0) {
            // 只有第一个在强制排序列表中，优先执行
            return -1;
        } else if (index2 == 0) {
            // 只有第二个在强制排序列表中，优先执行
            return 1;
        }
        
        // 处理附加变更日志文件
        boolean isAdditional1 = ADDITIONAL.equalsIgnoreCase(name1);
        boolean isAdditional2 = ADDITIONAL.equalsIgnoreCase(name2);
        
        if (isAdditional1 && isAdditional2) {
            return 0;
        } else if (isAdditional1) {
            // 附加文件最后执行
            return 1;
        } else if (isAdditional2) {
            // 附加文件最后执行
            return -1;
        }
        
        // 默认按文件名字典序排序
        return name1.compareTo(name2);
    }
}
