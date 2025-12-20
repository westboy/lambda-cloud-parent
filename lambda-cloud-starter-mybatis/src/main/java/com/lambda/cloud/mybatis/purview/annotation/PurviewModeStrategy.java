package com.lambda.cloud.mybatis.purview.annotation;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.PurviewContext;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeInnerStrategy;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeQueryStrategy;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeStatisticsStrategy;
import com.lambda.cloud.mybatis.purview.support.PurviewProfile;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * @author Jin
 */
public interface PurviewModeStrategy {

    /**
     * 根据策略生成不同的语句
     */
    void update(PlainSelect body, PurviewContext purview, LoginUser operator, Set<String> permissions)
            throws JSQLParserException;

    /**
     * 替换数据权限语句
     */
    String replace(String source, PurviewContext purview, LoginUser operator, Set<String> permissions)
            throws JSQLParserException;

    /**
     * 增强SQL语句
     */
    String improve(String source, PurviewProfile purviewProfile);

    /**
     * 根据模式选择不同的策略
     */
    static PurviewModeStrategy getInstance(Purview.Mode mode) {
        return switch (mode) {
            case INNER -> new PurviewModeInnerStrategy();
            case STATISTICS -> new PurviewModeStatisticsStrategy();
            default -> new PurviewModeQueryStrategy();
        };
    }
}
