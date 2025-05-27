package com.lambda.cloud.mybatis.purview.strategy;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.annotation.PurviewModeStrategy;
import com.lambda.cloud.mybatis.purview.support.DynamicPurview;
import com.lambda.cloud.mybatis.purview.support.Parameters;
import com.lambda.cloud.mybatis.purview.utils.PurviewUtils;
import com.lambda.cloud.mybatis.utils.SQLUtils;
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
public abstract class AbstractStrategy implements PurviewModeStrategy {

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
        return (PlainSelect) select.getSelectBody();
    }

    /**
     * 增强SQL语句
     *
     * @param source
     * @param parameters
     * @return org.apache.ibatis.mapping.MappedStatement
     * @throws JSQLParserException
     */
    @Override
    public String improve(String source, Parameters parameters) {
        LoginUser operator = parameters.getOperator();
        DynamicPurview purview = parameters.getPurview();
        Set<String> permissions = parameters.getPermissions();
        try {
            Select select = getSelect(source);
            PlainSelect body = getBody(select);
            if (purview.isReplace()) {
                return replace(source, purview, operator, permissions);
            } else {
                update(body, purview, operator, permissions);
                return select.getPlainSelect().toString();
            }
        } catch (Exception e) {
            return PurviewUtils.getSql(source, permissions);
        }
    }

    protected Select getSelect(String sql) throws JSQLParserException {
        return SQLUtils.parse(new StringReader(sql));
    }
}
