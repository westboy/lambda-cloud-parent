package com.lambda.cloud.core.exception.model;

import com.google.common.collect.ImmutableList;
import com.google.gson.Gson;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

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

    public void setErrors(List<ArgumentError> errors) {
        if (errors == null) {
            this.errors = ImmutableList.of();
        } else {
            this.errors = ImmutableList.copyOf(errors);
        }
    }

    public String toJsonString() {
        return GSON.toJson(this);
    }
}
