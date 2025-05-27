package com.lambda.cloud.mybatis.purview.strategy;

import com.lambda.cloud.core.principal.LoginUser;
import com.lambda.cloud.mybatis.purview.support.DynamicPurview;
import com.lambda.cloud.mybatis.purview.utils.PurviewUtils;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.expression.Alias;
import net.sf.jsqlparser.expression.Expression;
import net.sf.jsqlparser.expression.StringValue;
import net.sf.jsqlparser.expression.operators.relational.ExpressionList;
import net.sf.jsqlparser.expression.operators.relational.InExpression;
import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.*;

/**
 * 内联查询
 *
 * @author Jin
 */
public class PurviewModeQueryStrategy extends AbstractStrategy {

    @Override
    public String replace(String source, DynamicPurview purview, LoginUser operator, Set<String> permissions) {
        if (purview.isPretreatment()) {
            return PurviewUtils.getSql(source, permissions);
        } else {
            String sql = PurviewUtils.buildSQL02(purview, operator);
            return PurviewUtils.getSql(source, sql);
        }
    }

    @Override
    public void update(PlainSelect body, DynamicPurview purview, LoginUser operator, Set<String> permissions)
            throws JSQLParserException {
        InExpression expression = new InExpression();
        FromItem fromItem = body.getFromItem();
        String key = purview.getKey();
        if (fromItem instanceof Table) {
            Alias alias = fromItem.getAlias();
            key = PurviewUtils.getPurviewKey(alias, key);
        }
        expression.setLeftExpression(new Column(key));
        if (purview.isPretreatment()) {
            List<Expression> expressions = new ArrayList<>();
            for (String item : permissions) {
                expressions.add(new StringValue(item));
            }
            expression.setRightExpression(new ExpressionList<>(expressions));
        } else {
            String sql = PurviewUtils.buildSQL02(purview, operator);
            Select select = getSelect(sql);
            PlainSelect selectBody = select.getPlainSelect();
            LateralSubSelect subSelect = new LateralSubSelect();
            subSelect.setSelect(selectBody);
            expression.setRightExpression(subSelect);
        }
        updateWhere(body, expression);
    }
}
