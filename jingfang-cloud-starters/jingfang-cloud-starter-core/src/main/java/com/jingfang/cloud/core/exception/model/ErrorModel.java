package com.jingfang.cloud.core.exception.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

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
}
