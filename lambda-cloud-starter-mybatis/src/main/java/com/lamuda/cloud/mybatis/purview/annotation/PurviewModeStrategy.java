package com.lambda.cloud.mybatis.purview.annotation;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeInnerStrategy;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeQueryStrategy;
import com.lambda.cloud.mybatis.purview.strategy.PurviewModeStatisticsStrategy;
import com.lambda.cloud.mybatis.purview.support.DynamicPurview;
import com.lambda.cloud.mybatis.purview.support.Parameters;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;

import java.util.Set;

/**
 * @author Jin
 */
public interface PurviewModeStrategy {

    /**
     * 根据策略生成不同的语句
     *
     * @param body
     * @param purview
     * @param operator
     * @param permissions
     * @return net.sf.jsqlparser.expression.Expression
     * @throws JSQLParserException
     */
    void update(PlainSelect body, DynamicPurview purview, LoginUser operator, Set<String> permissions) throws JSQLParserException;

    /**
     * 替换数据权限语句
     *
     * @param source
     * @param purview
     * @param operator
     * @param permissions
     * @return java.lang.String
     * @throws JSQLParserException
     */
    String replace(String source, DynamicPurview purview, LoginUser operator, Set<String> permissions) throws JSQLParserException;


    /**
     * 增强SQL语句
     *
     * @param source
     * @param parameters
     * @return org.apache.ibatis.mapping.MappedStatement
     * @throws JSQLParserException
     */
    String improve(String source, Parameters parameters);

    /**
     * 根据模式选择不同的策略
     *
     * @param mode
     */
    static PurviewModeStrategy getInstance(Purview.Mode mode) {
        return switch (mode) {
            case INNER -> new PurviewModeInnerStrategy();
            case STATISTICS -> new PurviewModeStatisticsStrategy();
            default -> new PurviewModeQueryStrategy();
        };
    }
}
