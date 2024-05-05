package com.jingfang.core.exception.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

/**
 * @author jin
 */
@Getter
public class ArgumentError {

    final String field;

    final String details;

    @JsonCreator
    public ArgumentError(@JsonProperty("field") String field, @JsonProperty("details") String details) {
        this.field = field;
        this.details = details;
    }

}
