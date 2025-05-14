package com.lambda.cloud.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jin
 */
@Getter
@Setter
@Schema(description = "通用响应结果")
public class Result {

    @Schema(description = "结果状态")
    private boolean status;
    @Schema(description = "提示信息")
    private String message;

}
