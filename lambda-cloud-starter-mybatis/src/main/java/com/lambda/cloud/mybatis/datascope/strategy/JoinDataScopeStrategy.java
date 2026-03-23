package com.lambda.cloud.mybatis.datascope.strategy;

import com.lambda.autoconfig.datascope.DataScopeProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.DataScopePropertiesHolder;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.LongValue;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.conditional.AndExpression;
import net.sf.jsqlparser.expression.operators.conditional.OrExpression;
import net.sf.jsqlparser.expression.operators.relational.*;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang.ArrayUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.lambda.cloud.mybatis.datascope.DataScopeEvaluator.getDataScopeIds;
import static com.lambda.cloud.mybatis.datascope.DataScopeEvaluator.getLevel;

/**
 * 内联查询
 *
 * @author Jin
 */
public class JoinDataScopeStrategy extends AbstractDataScopeStrategy {

    @Override
    public void update(PlainSelect body, DataScopeContext context, LoginUser user, Set<String> permissions) {
        DataScope.Scheme scheme = context.getScheme();
        if (DataScope.Scheme.ORGANIZATION.equals(scheme)) {
            Expression expression = innerExpressionForOrgan(body, context, user);
            updateWhere(body, expression);
        } else {
            innerExpressionForCascade(body, context, user, permissions);
        }
    }

    @Override
    public String replace(String source, DataScopeContext context, LoginUser operator, Set<String> permissions) {
        // Replace模式难以通过JOIN实现，降级为子查询或直接返回不支持.
        throw new UnsupportedOperationException("JoinDataScopeStrategy does not support Replace mode");
    }

    /**
     * 组织模式的内联查询表达式
     */
    private Expression innerExpressionForOrgan(PlainSelect body, DataScopeContext context, LoginUser user) {
        String orgId = user.getOrgId();
        String idOrg = DataScopePropertiesHolder.getInstance().getOrganizationTableAlias() + "."
                + DataScopePropertiesHolder.getInstance().getOrganizationIdColumn();
        Table table = new Table(DataScopePropertiesHolder.getInstance().getOrganizationTableName());
        table.setAlias(new Alias(DataScopePropertiesHolder.getInstance().getOrganizationTableAlias(), false));
        EqualsTo expression0 = new EqualsTo();
        expression0.setLeftExpression(new Column(idOrg));
        expression0.setRightExpression(new Column(context.getKey()));
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(table);
        join.addOnExpression(expression0);
        List<Join> joins = body.getJoins();
        if (CollectionUtils.isEmpty(joins)) {
            joins = new ArrayList<>();
        }
        joins.add(join);
        body.setJoins(joins);
        // ~~=====================WHERE==================~~//
        String idParentKeys = DataScopePropertiesHolder.getInstance().getOrganizationTableAlias() + "."
                + DataScopePropertiesHolder.getInstance().getOrganizationParentKeysColumn();
        EqualsTo expression1 = new EqualsTo();
        expression1.setLeftExpression(new Column(idOrg));
        expression1.setRightExpression(new StringValue(orgId));
        LikeExpression expression2 = new LikeExpression();
        expression2.setLeftExpression(new Column(idParentKeys));
        expression2.setRightExpression(new StringValue("%" + orgId + "%"));
        return new ParenthesedExpressionList<>(new OrExpression(expression1, expression2));
    }

    /***
     * 联动模式的内联查询表达式
     */
    private void innerExpressionForCascade(
            PlainSelect body, DataScopeContext context, LoginUser operator, Set<String> permissions) {
        if (context.isPretreatment()) {
            InExpression expression = new InExpression();
            expression.setLeftExpression(new Column(context.getKey()));
            List<Expression> expressions = new ArrayList<>();
            for (String item : permissions) {
                expressions.add(new StringValue(item));
            }
            expression.setRightExpression(new ExpressionList<>(expressions));
            updateWhere(body, expression);
        }

        Select selectable = getDistinctSelect(context, operator);
        LateralSubSelect select1 = new LateralSubSelect();
        select1.setSelect(selectable);
        DataScopeProperties properties = DataScopePropertiesHolder.getInstance();
        select1.setAlias(new Alias(properties.getDataScopeTableAlias(), false));
        EqualsTo expression0 = new EqualsTo();
        expression0.setLeftExpression(
                new Column(properties.getDataScopeTableAlias() + "." + properties.getDataScopeIdColumn()));
        expression0.setRightExpression(new Column(context.getKey()));
        Join join = new Join();
        join.setInner(true);
        join.setRightItem(select1);
        join.addOnExpression(expression0);
        List<Join> joins = body.getJoins();
        if (CollectionUtils.isEmpty(joins)) {
            joins = new ArrayList<>();
        }
        joins.add(join);
        body.setJoins(joins);
    }

    private Select getDistinctSelect(DataScopeContext context, LoginUser operator) {
        DataScopeProperties properties = DataScopePropertiesHolder.getInstance();
        PlainSelect body1 = new PlainSelect();
        body1.addSelectItems(new SelectItem<>(new Column(
                "DISTINCT " + properties.getDataScopeTableAlias0() + "." + properties.getDataScopeIdColumn())));
        Table table = new Table(properties.getDataScopeTableName());
        table.setAlias(new Alias(properties.getDataScopeTableAlias0(), false));
        body1.setFromItem(table);

        List<Expression> expressions =
                getDataScopeIds(operator).stream().map(StringValue::new).collect(Collectors.toList());
        InExpression expression1 = new InExpression();
        expression1.setLeftExpression(
                new Column(properties.getDataScopeTableAlias0() + "." + properties.getDataScopeTidColumn()));
        expression1.setRightExpression(new ExpressionList<>(expressions));

        Expression expression = expression1;
        int[] types = context.getType();
        if (ArrayUtils.isNotEmpty(types)) {
            if (types.length == 1) {
                EqualsTo expression2 = new EqualsTo();
                expression2.setLeftExpression(
                        new Column(properties.getDataScopeTableAlias0() + "." + properties.getDataScopeTypeColumn()));
                expression2.setRightExpression(new LongValue(types[0]));
                expression = new AndExpression(expression1, expression2);
            } else {
                InExpression expression2 = new InExpression();
                expression2.setLeftExpression(
                        new Column(properties.getDataScopeTableAlias0() + "." + properties.getDataScopeTypeColumn()));
                List<Expression> list = new ArrayList<>();
                for (int i : types) {
                    list.add(new LongValue(i));
                }
                expression2.setRightExpression(new ExpressionList<>(list));
                expression = new AndExpression(expression1, expression2);
            }
        }

        int level = getLevel(context);
        if (level > -1) {
            ComparisonOperator expression3 = context.getLevelExp().getComparisonOperator();
            expression3.setLeftExpression(
                    new Column(properties.getDataScopeTableAlias0() + "." + properties.getDataScopeRankColumn()));
            expression3.setRightExpression(new LongValue(level));
            expression = new AndExpression(expression, expression3);
        }
        int checked = context.getChecked();
        if (checked > 0) {
            EqualsTo expression4 = new EqualsTo();
            expression4.setLeftExpression(
                    new Column(properties.getDataScopeTableAlias0() + "." + properties.getDataScopeCheckedColumn()));
            expression4.setRightExpression(new LongValue(checked));
            expression = new AndExpression(expression, expression4);
        }
        body1.setWhere(expression);
        return body1;
    }
}
