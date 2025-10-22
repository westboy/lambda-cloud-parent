package com.lambda.cloud.netty.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParsedData {
    private String raw;
    private Boolean isComputed;
    private Integer order;
}
