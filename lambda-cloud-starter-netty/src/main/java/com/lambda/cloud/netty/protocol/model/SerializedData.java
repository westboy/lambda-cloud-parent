package com.lambda.cloud.netty.protocol.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SerializedData {
    private Long crc;
    private Integer length;
}
