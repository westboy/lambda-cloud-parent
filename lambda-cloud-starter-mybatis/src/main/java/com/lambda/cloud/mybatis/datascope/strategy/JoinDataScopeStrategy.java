package com.lambda.cloud.mybatis.datascope.strategy;

import static com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluator.getDataScopeIds;
import static com.lambda.cloud.mybatis.datascope.support.DataScopeEvaluator.getLevel;

import com.lambda.autoconfig.datascope.DataScopeProperties;
import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.datascope.DataScopePropertiesHolder;
import com.lambda.cloud.mybatis.datascope.annotation.DataScope;
import com.lambda.cloud.mybatis.datascope.context.DataScopeContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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

/**
 * 内联查询
 *
 * @author Jin
 */
public class JoinDataScopeStrategy extends AbstractDataScopeStrategy {

    @Override
    public void update(PlainSelect body, DataScopeContext purview, LoginUser user, Set<String> permissions) {
        DataScope.Scheme scheme = purview.getScheme();
        if (DataScope.Scheme.NOT_CASCADE.equals(scheme)) {
            throw new RuntimeException();
        } else if (DataScope.Scheme.ORGANIZATION.equals(scheme)) {
            Expression expression = innerExpressionForOrgan(body, purview, user);
            updateWhere(body, expression);
        } else {
            innerExpressionForCascade(body, purview, user, permissions);
        }
    }

    @Override
    public String replace(String source, DataScopeContext purview, LoginUser operator, Set<String> permissions) {
        throw new RuntimeException();
    }

    /**
     * 组织模式的内联查询表达式
     *
     * @param body
     * @param purview
     * @param user
     */
    private Expression innerExpressionForOrgan(PlainSelect body, DataScopeContext purview, LoginUser user) {
        String orgId = user.getOrgId();
        String idOrg = DataScopePropertiesHolder.getInstance().getOrganizationTableAlias() + "."
                + DataScopePropertiesHolder.getInstance().getOrganizationIdColumn();
        Table table = new Table(DataScopePropertiesHolder.getInstance().getOrganizationTableName());
        table.setAlias(new Alias(DataScopePropertiesHolder.getInstance().getOrganizationTableAlias(), false));
        EqualsTo expression0 = new EqualsTo();
        expression0.setLeftExpression(new Column(idOrg));
        expression0.setRightExpression(new Column(purview.getKey()));
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
     * @param body
     * @param purview
     * @param operator
     * @param permissions
     */
    private void innerExpressionForCascade(
            PlainSelect body, DataScopeContext purview, LoginUser operator, Set<String> permissions) {
        if (purview.isPretreatment()) {
            InExpression expression = new InExpression();
            expression.setLeftExpression(new Column(purview.getKey()));
            List<Expression> expressions = new ArrayList<>();
            for (String item : permissions) {
                expressions.add(new StringValue(item));
            }
            expression.setRightExpression(new ExpressionList<>(expressions));
            updateWhere(body, expression);
        }

        Select selectbody = getDistinctSelect(purview, operator);
        LateralSubSelect select1 = new LateralSubSelect();
        select1.setSelect(selectbody);
        DataScopeProperties properties = DataScopePropertiesHolder.getInstance();
        select1.setAlias(new Alias(properties.getPurviewTableAlias(), false));
        EqualsTo expression0 = new EqualsTo();
        expression0.setLeftExpression(
                new Column(properties.getPurviewTableAlias() + "." + properties.getPurviewIdColumn()));
        expression0.setRightExpression(new Column(purview.getKey()));
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

    private Select getDistinctSelect(DataScopeContext purview, LoginUser operator) {
        DataScopeProperties properties = DataScopePropertiesHolder.getInstance();
        PlainSelect body1 = new PlainSelect();
        body1.addSelectItems(new SelectItem<>(
                new Column("DISTINCT " + properties.getPurviewTableAlias0() + "." + properties.getPurviewIdColumn())));
        Table table = new Table(properties.getPurviewTableName());
        table.setAlias(new Alias(properties.getPurviewTableAlias0(), false));
        body1.setFromItem(table);

        List<Expression> tids =
                getDataScopeIds(operator).stream().map(StringValue::new).collect(Collectors.toList());
        InExpression expression1 = new InExpression();
        expression1.setLeftExpression(
                new Column(properties.getPurviewTableAlias0() + "." + properties.getPurviewTidColumn()));
        expression1.setRightExpression(new ExpressionList<>(tids));

        Expression expression = expression1;
        int[] types = purview.getType();
        if (ArrayUtils.isNotEmpty(types)) {
            if (types.length == 1) {
                EqualsTo expression2 = new EqualsTo();
                expression2.setLeftExpression(
                        new Column(properties.getPurviewTableAlias0() + "." + properties.getPurviewTypeColumn()));
                expression2.setRightExpression(new LongValue(types[0]));
                expression = new AndExpression(expression1, expression2);
            } else {
                InExpression expression2 = new InExpression();
                expression2.setLeftExpression(
                        new Column(properties.getPurviewTableAlias0() + "." + properties.getPurviewTypeColumn()));
                List<Expression> list = new ArrayList<>();
                for (int i : types) {
                    list.add(new LongValue(i));
                }
                expression2.setRightExpression(new ExpressionList<>(list));
                expression = new AndExpression(expression1, expression2);
            }
        }

        int level = getLevel(purview);
        if (level > -1) {
            ComparisonOperator expression3 = purview.getLevelExp().getComparisonOperator();
            expression3.setLeftExpression(
                    new Column(properties.getPurviewTableAlias0() + "." + properties.getPurviewRankColumn()));
            expression3.setRightExpression(new LongValue(level));
            expression = new AndExpression(expression, expression3);
        }
        int checked = purview.getChecked();
        if (checked > 0) {
            EqualsTo expression4 = new EqualsTo();
            expression4.setLeftExpression(
                    new Column(properties.getPurviewTableAlias0() + "." + properties.getPurviewCheckedColumn()));
            expression4.setRightExpression(new LongValue(checked));
            expression = new AndExpression(expression, expression4);
        }
        body1.setWhere(expression);
        return body1;
    }
}
