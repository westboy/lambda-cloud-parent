package com.lambda.cloud.core.utils;

import java.math.BigDecimal;
import java.text.DecimalFormat;

/**
 * 数字工具类
 * @author 熊猫de 2018/7/12
 */
public class NumberUtils {

    private static final DecimalFormat FINANCE = new DecimalFormat("##,##0.00");

    /**
     * BigDecimal 数字转成千分位的财务计数方式
     * @param number 需要转换的数字
     * @return 千分位字符串
     */
    public static String decimalToMoneyString(BigDecimal number) {
        return FINANCE.format(number);
    }

}
