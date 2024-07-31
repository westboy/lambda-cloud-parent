package com.jingfang.cloud.liquibase.filter;

import cn.hutool.core.io.FileUtil;
import liquibase.changelog.IncludeAllFilter;

import java.util.regex.Pattern;

/**
 * @author westboy
 */
public class DefaultLiquibaseFilter implements IncludeAllFilter {
    private static final String PATTERN = "jingfang-\\w*-changelog.xml";

    @Override
    public boolean include(String file) {
        String name = FileUtil.getName(file);
        return Pattern.matches(PATTERN, name);
    }
}
