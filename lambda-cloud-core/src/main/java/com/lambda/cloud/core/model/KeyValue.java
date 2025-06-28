package com.lambda.cloud.core.model;

import java.io.Serializable;
import lombok.Data;

@Data
public class KeyValue implements Serializable {
    private String code;
    private String desc;
}
