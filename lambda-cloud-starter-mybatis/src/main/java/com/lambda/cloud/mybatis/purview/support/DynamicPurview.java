package com.lambda.cloud.mybatis.purview.support;

import com.baomidou.mybatisplus.core.toolkit.Constants;
import com.lambda.cloud.mybatis.purview.annotation.Purview;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.io.Serializable;
import lombok.Data;

/**
 * 动态数据权限
 *
 * @author Jin
 */
@Data
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2", "EI_EXPOSE_REP"})
public class DynamicPurview implements Serializable {

    /**
     * 数据权限关联的字段名
     */
    private String key;

    /**
     * 所匹配的数据权限级别，最末级为2147483647
     */
    private int level;

    private Purview.Expression levelExp;

    /**
     * 权限树类型
     */
    private int[] type;

    /**
     * 查询模式
     */
    private Purview.Mode mode;

    /**
     * 权限方案
     */
    private Purview.Scheme scheme;

    /**
     * 调用方额外的sql查询条件
     */
    private String condition;

    /**
     * 是否为树结构
     */
    private boolean tree;

    /**
     * 开启预处理功能，应对大数据量查询
     */
    private boolean pretreatment;

    private int checked;

    private boolean replace;

    public DynamicPurview() {
        this.key = "T.id";
        this.level = Integer.MAX_VALUE;
        this.mode = Purview.Mode.SUB_QUERY;
        this.scheme = Purview.Scheme.CASCADE;
        this.levelExp = Purview.Expression.EQ;
        this.condition = Constants.EMPTY;
    }
}
