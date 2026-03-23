package com.lambda.cloud.mybatis.datascope.strategy;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import com.lambda.cloud.mybatis.datascope.support.DataScopeCacheKey;
import com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluationContext;
import com.lambda.cloud.mybatis.datascope.DataScopeEvaluator;
import com.lambda.cloud.mybatis.utils.JSqlParserUtils;
import java.io.StringReader;
import java.util.Set;
import javax.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * @author Jin
 */
@Slf4j
public abstract class AbstractDataScopeStrategy implements DataScopeStrategy {

    private static final Cache<DataScopeCacheKey, String> SQL_CACHE = CacheUtil.newLRUCache(1024);

    /**
     * 更新Where条件
     *
     * @param body
     * @param expression
     */
    void updateWhere(PlainSelect body, Expression expression) {
        Expression where = body.getWhere();
        if (where != null) {
            body.setWhere(new AndExpression(where, expression));
        } else {
            body.setWhere(expression);
        }
    }

    /**
     * <p>获取查询语句体</p>
     *
     * <em>当解析出错时将返回null</em>
     *
     * @param select
     * @return net.sf.jsqlparser.statement.select.PlainSelect
     */
    @Nonnull
    PlainSelect getBody(Select select) {
        return select.getPlainSelect();
    }

    /**
     * 增强SQL语句
     *
     * @param source
     * @param dataScopeEvaluationContext
     * @return org.apache.ibatis.mapping.MappedStatement
     * @throws JSQLParserException
     */
    @Override
    public String improve(String source, DataScopeEvaluationContext dataScopeEvaluationContext) {
        LoginUser operator = dataScopeEvaluationContext.getOperator();
        DataScopeContext context = dataScopeEvaluationContext.getContext();
        Set<String> permissions = dataScopeEvaluationContext.getPermissions();

        // 显式处理 Replace 模式
        if (context.isReplace()) {
            try {
                return replace(source, context, operator, permissions);
            } catch (Exception e) {
                // Replace 模式下的特定回退逻辑（如果是基于正则替换的实现，通常不会抛出 JSQLParserException）
                // 但为了保险起见，这里可以保留一个最小化的回退或者直接抛出异常
                log.warn("Replace mode failed, falling back to regex replacement. Error: {}", e.getMessage());
                return DataScopeEvaluator.getSql(source, permissions);
            }
        }

        try {
            Select select = getSelect(source);
            PlainSelect body = getBody(select);
            update(body, context, operator, permissions);
            return select.getPlainSelect().toString();
        } catch (JSQLParserException e) {
            log.error("Failed to parse SQL: {}. Error: {}", source, e.getMessage());
            // 解析失败时，不再隐式降级为正则替换，而是抛出异常，暴露问题
            throw new RuntimeException("Failed to parse SQL for data permission filtering", e);
        } catch (Exception e) {
            log.error("Unexpected error during SQL improvement: {}. Error: {}", source, e.getMessage());
            throw new RuntimeException("Unexpected error during data permission processing", e);
        }
    }

    protected Select getSelect(String sql) throws JSQLParserException {
        return JSqlParserUtils.parse(new StringReader(sql));
    }
}
