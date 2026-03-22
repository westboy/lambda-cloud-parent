package com.lambda.cloud.mybatis.datascope.strategy;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluationContext;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.statement.select.PlainSelect;

/**
 * @author Jin
 */
public interface DataScopeStrategy {

    /**
     * 根据策略生成不同的语句
     */
    void update(PlainSelect body, DataScopeContext purview, LoginUser operator, Set<String> permissions)
            throws JSQLParserException;

    /**
     * 替换数据权限语句
     */
    String replace(String source, DataScopeContext purview, LoginUser operator, Set<String> permissions)
            throws JSQLParserException;

    /**
     * 增强SQL语句
     */
    String improve(String source, DataScopeEvaluationContext dataScopeEvaluationContext);

    /**
     * 根据模式选择不同的策略
     */
    static DataScopeStrategy getInstance(DataScope.Mode mode) {
        return switch (mode) {
            case INNER -> new JoinDataScopeStrategy();
            case STATISTICS -> new StatisticsDataScopeStrategy();
            default -> new SubQueryDataScopeStrategy();
        };
    }
}
