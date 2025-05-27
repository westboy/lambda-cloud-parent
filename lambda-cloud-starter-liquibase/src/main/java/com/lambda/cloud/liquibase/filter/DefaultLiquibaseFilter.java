package com.lambda.cloud.liquibase.filter;

import cn.hutool.core.io.FileUtil;
import java.util.regex.Pattern;
import liquibase.changelog.IncludeAllFilter;

/**
 * @author westboy
 */
public class DefaultLiquibaseFilter implements IncludeAllFilter {
    private static final String PATTERN = "lambda-\\w*-changelog.xml";

    @Override
    public boolean include(String file) {
        String name = FileUtil.getName(file);
        return Pattern.matches(PATTERN, name);
    }
}
