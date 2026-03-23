package com.lambda.cloud.mybatis.datascope.strategy;

import javax.annotation.Nonnull;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

/**
 * 统计模式
 *
 * @author Jin
 */
public class StatisticsDataScopeStrategy extends SubQueryDataScopeStrategy {

    @Nonnull
    @Override
    public PlainSelect getBody(Select select) {
        PlainSelect body = super.getBody(select);
        FromItem item = body.getFromItem();
        // 仅当下钻的子查询依然是一个简单的 SELECT 时才继续下钻，防止破坏复杂的聚合查询或 UNION 查询
        while (item instanceof Select subSelect) {
            if (subSelect.getPlainSelect() != null) {
                PlainSelect plainSelect = subSelect.getPlainSelect();
                if (plainSelect != null) {
                    body = plainSelect;
                    item = plainSelect.getFromItem();
                } else {
                    break;
                }
            } else {
                break;
            }
        }
        if (item instanceof Table || item instanceof Select) {
            return body;
        }
        throw new UnsupportedOperationException("StatisticsDataScopeStrategy does not support this SQL structure");
    }
}
