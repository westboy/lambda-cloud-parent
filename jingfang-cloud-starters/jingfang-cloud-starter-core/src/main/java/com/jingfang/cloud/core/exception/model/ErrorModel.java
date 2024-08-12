package com.jingfang.cloud.core.exception.model;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author jin
 */
@Getter
@Setter
public class ErrorModel {
    public static final Gson GSON = new Gson();
    private int status;
    private long timestamp;
    private String error;
    private String message;
    private List<ArgumentError> errors;
    private String path;

    public String toJsonString(){
        return GSON.toJson(this);
    }
}
