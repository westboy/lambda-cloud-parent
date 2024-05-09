package com.jingfang.cloud.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.time.format.DateTimeFormatter;

public final class Constants {

    private Constants() {
    }

    public static final String ASYNC_EVENT_EXECUTOR = "asyncEventExecutor";
    public static final String HMAC = "HmacSHA ";
    public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final Gson GSON = new GsonBuilder().create();
    public static final String LOCATION_PATTERN = "classpath*:com/jingfanf/cloud/**/*.class";
    public static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter LOCAL_DATE_TIME = DateTimeFormatter.ofPattern(DATE_FORMAT);


}