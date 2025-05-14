package com.lambda.cloud.liquibase.comparator;

import cn.hutool.core.io.FileUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.ArrayUtils;

import java.util.Comparator;

/**
 * @author wb
 */
@Slf4j
public class DefaultLiquibaseComparator implements Comparator<String> {
    /**
     * 需要强制要求优先级顺序的配置
     */
    private static final String[] FORCED_SORT = {
            "lambda-datasource-changelog.xml"
    };
    public static final String ADDITIONAL = "lambda-additional-changelog.xml";

    @Override
    public int compare(String o1, String o2) {
        String name1 = FileUtil.getName(o1);
        String name2 = FileUtil.getName(o2);
        if (name1.equalsIgnoreCase(name2)) {
            return 0;
        }
        int index1 = ArrayUtils.indexOf(FORCED_SORT, name1);
        int index2 = ArrayUtils.indexOf(FORCED_SORT, name2);
        if (index1 > -1 && index2 > -1) {
            return index1 - index2 > 0 ? 1 : -1;
        } else if (index1 > -1) {
            return -1;
        } else if (index2 > -1) {
            return 1;
        } else if (name1.equalsIgnoreCase(ADDITIONAL) || name2.equalsIgnoreCase(ADDITIONAL)) {
            return 1;
        } else {
            return name1.compareTo(name2);
        }
    }
}
