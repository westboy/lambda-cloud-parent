package com.lamuda.cloud.mybatis.purview.strategy;

import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.select.FromItem;
import net.sf.jsqlparser.statement.select.PlainSelect;
import net.sf.jsqlparser.statement.select.Select;

import javax.annotation.Nonnull;

/**
 * 统计模式
 *
 * @author Jin
 */
public class PurviewModeStatisticsStrategy extends PurviewModeQueryStrategy {

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
