package com.lambda.cloud.core.exception.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jin
 */
@Getter
@Setter
public class ArgumentError implements Serializable {

    private String field;

    private String details;

    @JsonCreator
    public ArgumentError(@JsonProperty("field") String field, @JsonProperty("details") String details) {
        this.field = field;
        this.details = details;
    }
}
