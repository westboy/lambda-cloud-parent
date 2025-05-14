package com.lambda.cloud.mybatis.utils;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

import javax.annotation.Nonnull;
import java.io.StringReader;
import java.util.Set;
import java.util.StringJoiner;
import java.util.regex.Pattern;

/**
 * @author Jin
 */
@SuppressWarnings({"AlibabaClassNamingShouldBeCamel", "PMD"})
public final class SQLUtils {
    public static final String COMMA = ",";
    public static final String LEFT_BRACKET = "(";
    public static final String RIGHT_BRACKET = ")";
    public static final String SINGLE_QUOTE = "'";
    public static final String IN = "IN";
    public static final String EQUAL = "=";
    public static final String SPACE = " ";


    private static final CCJSqlParserManager PARSER = new CCJSqlParserManager();

    private SQLUtils() {
    }

    /**
     * 将数字数组转为IN模式
     *
     * @param args
     * @return java.lang.String
     */
    public static String toIn(@Nonnull int[] args) {
        StringBuilder builder = new StringBuilder(SPACE);
        if (args.length == 1) {
            builder.append(EQUAL).append(SPACE).append(args[0]);
        } else {
            builder.append(IN).append(SPACE);
            StringJoiner joiner = new StringJoiner(COMMA, LEFT_BRACKET, RIGHT_BRACKET);
            for (int i : args) {
                joiner.add(String.valueOf(i));
            }
            builder.append(joiner);
        }
        return builder.toString();
    }

    /**
     * 将字符集合转为IN模式
     *
     * @param args
     * @return java.lang.String
     */
    public static String toIn(@Nonnull Set<String> args) {
        StringBuilder builder = new StringBuilder(SPACE);
        if (args.size() == 1) {
            builder.append("= '").append(args.iterator().next()).append(SINGLE_QUOTE);
        } else {
            builder.append(IN);
            StringJoiner joiner = new StringJoiner(COMMA, LEFT_BRACKET, RIGHT_BRACKET);
            for (String i : args) {
                joiner.add(SINGLE_QUOTE + i + SINGLE_QUOTE);
            }
            builder.append(joiner);
        }
        return builder.toString();
    }

    /**
     * 将字符串解析为SQL语句
     *
     * @param sql
     * @return net.sf.jsqlparser.statement.select.Select
     */
    public static Select parse(String sql) throws JSQLParserException {
        //将Oracle方言中?||按标准模式添加空格
        sql = Pattern.compile("\\?\\|").matcher(sql).replaceAll("? |");
        return parse(new StringReader(sql));
    }

    /**
     * 将字符串解析为SQL语句
     *
     * @param reader
     * @return net.sf.jsqlparser.statement.select.Select
     */
    public static Select parse(StringReader reader) throws JSQLParserException {
        return (Select) PARSER.parse(reader);
    }
}
