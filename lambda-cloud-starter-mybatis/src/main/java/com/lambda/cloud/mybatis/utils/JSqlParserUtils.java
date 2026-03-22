package com.lambda.cloud.mybatis.utils;

import java.io.StringReader;
import java.util.regex.Pattern;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

public class JSqlParserUtils {

    private static final CCJSqlParserManager PARSER = new CCJSqlParserManager();

    private static final Pattern ORACLE_PATTERN = Pattern.compile("\\?\\|");

    /**
     * 将字符串解析为SQL语句
     *
     * @param sql
     * @return net.sf.jsqlparser.statement.select.Select
     */
    public static Select parse(String sql) throws JSQLParserException {
        // 将Oracle方言中?||按标准模式添加空格
        sql = ORACLE_PATTERN.matcher(sql).replaceAll("? |");
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
