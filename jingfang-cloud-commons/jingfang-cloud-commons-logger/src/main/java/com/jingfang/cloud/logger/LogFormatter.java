package com.jingfang.cloud.logger;

import org.slf4j.helpers.MessageFormatter;

/**
 * LogFormatter
 *
 * @author jpjoo
 */
public final class LogFormatter {

    private LogFormatter() {
    }

    public static String format(String msg, Object arg) {
        return MessageFormatter.format(msg, arg).getMessage();
    }

    public static String format(String msg, Object arg1, Object arg2) {
        return MessageFormatter.format(msg, arg1, arg2).getMessage();
    }

    public static String format(String msg, Object... objs) {
        return MessageFormatter.arrayFormat(msg, objs).getMessage();
    }
}
