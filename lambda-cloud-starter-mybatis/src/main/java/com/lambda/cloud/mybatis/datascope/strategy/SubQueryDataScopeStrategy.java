package com.lambda.cloud.mybatis.datascope.strategy;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.DataScopeEvaluator;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.statement.select.*;

/**
 *  子查询
 *
 * @author Jin
 */
public class SubQueryDataScopeStrategy extends AbstractDataScopeStrategy {

    @Override
    public String replace(String source, DataScopeContext context, LoginUser operator, Set<String> permissions) {
        if (context.isPretreatment()) {
            return DataScopeEvaluator.getSql(source, permissions);
        } else {
            String sql = DataScopeEvaluator.buildStrategyScopeSql(context, operator);
            return DataScopeEvaluator.getSql(source, sql);
        }
    }

    @Override
    public void update(PlainSelect body, DataScopeContext context, LoginUser operator, Set<String> permissions)
            throws JSQLParserException {
        InExpression expression = new InExpression();
        String key = context.getKey();
        expression.setLeftExpression(new Column(key));
        if (context.isPretreatment()) {
            List<Expression> expressions = new ArrayList<>();
            for (String item : permissions) {
                expressions.add(new StringValue(item));
            }
            expression.setRightExpression(new ExpressionList<>(expressions));
        } else {
            String sql = DataScopeEvaluator.buildStrategyScopeSql(context, operator);
            Select select = getSelect(sql);
            ParenthesedSelect subSelect = new ParenthesedSelect();
            subSelect.setSelect(select);
            expression.setRightExpression(subSelect);
        }
        updateWhere(body, expression);
    }

    /*
       强制使用用户在注解中配置的 key
    public static String resolveColumnName(Alias alias, @Nonnull String key) {
        if (key.contains(LEFT_BRACKET) || key.contains(DOT)) {
            return key;
        }
        if (alias != null) {
            return alias.getName() + DOT + key;
        }
        return key;
    }
     */
}
