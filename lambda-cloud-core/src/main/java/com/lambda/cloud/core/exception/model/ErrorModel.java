package com.lambda.cloud.core.exception.model;

import static com.lambda.cloud.core.Constants.GSON;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jin
 */
@Getter
@Setter
public class ErrorModel {
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
