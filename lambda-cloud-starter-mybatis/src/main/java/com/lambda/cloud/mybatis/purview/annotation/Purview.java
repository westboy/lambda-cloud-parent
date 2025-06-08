package com.lambda.cloud.mybatis.purview.annotation;

import static com.baomidou.mybatisplus.core.toolkit.StringPool.*;

import java.lang.annotation.*;
import net.sf.jsqlparser.expression.operators.relational.*;
import org.apache.commons.lang.StringUtils;

/**
 * 数据权限注解
 *
 * @author Jin
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface Purview {
    /**
     * 数据权限关联的字段名
     */
    String key() default "T.id";

    /**
     * 所匹配的数据权限级别，最末级为2147483647
     */
    int level() default Integer.MAX_VALUE;

    /**
     * 所匹配的数据权限级别表达式
     */
    Expression levelExp() default Expression.EQ;

    /**
     * 权限树类型
     */
    int[] type() default {0};

    /**
     * 开启预处理功能，应对大数据量查询
     */
    boolean pretreatment() default false;

    /**
     * 查询模式
     */
    Mode mode() default Mode.SUB_QUERY;

    /**
     * 权限方案
     */
    Scheme scheme() default Scheme.CASCADE;

    String condition() default "";

    /**
     * 在默认级联模式下生效(CASCADE)
     * 0：忽略该条件
     * 1：查询全选状态的数据
     * 2：查询半选状态的数据
     */
    int checked() default 0;

    enum Mode {
        /**
         * 子查询模式
         */
        SUB_QUERY,
        /**
         * 内联查询模式
         */
        INNER,
        /**
         * 统计模式
         */
        STATISTICS
    }

    enum Scheme {
        /**
         * 级联方案
         */
        CASCADE,
        /**
         * 非级联方案
         */
        NOT_CASCADE,
        /**
         * 按照组织授权
         */
        ORGAN
    }

    enum Expression {
        /**
         * 等于
         */
        EQ {
            @Override
            public ComparisonOperator getComparisonOperator() {
                return new EqualsTo();
            }

            @Override
            public String getComparison() {
                return EQUALS;
            }
        },
        /**
         * 大于
         */
        GT {
            @Override
            public ComparisonOperator getComparisonOperator() {
                return new GreaterThan();
            }

            @Override
            public String getComparison() {
                return RIGHT_CHEV;
            }
        },
        /**
         * 小于
         */
        LT {
            @Override
            public ComparisonOperator getComparisonOperator() {
                return new MinorThan();
            }

            @Override
            public String getComparison() {
                return LEFT_CHEV;
            }
        },
        /**
         * 大于等于
         */
        GE {
            @Override
            public ComparisonOperator getComparisonOperator() {
                return new GreaterThanEquals();
            }

            @Override
            public String getComparison() {
                return RIGHT_CHEV + EQUALS;
            }
        },
        /**
         * 小于等于
         */
        LE {
            @Override
            public ComparisonOperator getComparisonOperator() {
                return new MinorThanEquals();
            }

            @Override
            public String getComparison() {
                return LEFT_CHEV + EQUALS;
            }
        };

        public abstract ComparisonOperator getComparisonOperator();

        public abstract String getComparison();

        /**
         * 根据运算符获取运算对象
         * @param operator
         * @return
         */
        public static Expression parseExpression(String operator) {
            if (StringUtils.isBlank(operator)) {
                return EQ;
            }
            return switch (operator) {
                case "=" -> EQ;
                case ">" -> GT;
                case ">=" -> GE;
                case "<" -> LT;
                case "<=" -> LE;
                default -> null;
            };
        }
    }
}
