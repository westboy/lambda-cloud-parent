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
        while (item instanceof Select) {
            PlainSelect body1 = ((Select) item).getPlainSelect();
            if (body1 != null) {
                body = body1;
                item = body1.getFromItem();
            } else {
                break;
            }
        }
        if (item instanceof Table) {
            return body;
        }
        throw new RuntimeException("not supported");
    }
}
