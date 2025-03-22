package com.lamuda.cloud.logger.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "操作详情")
public class OperationDetail {

    String operationId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "请求地址")
    String uri;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "请求参数")
    Object parameters;
    @Schema(description = "请求消息体")
    Object body;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "请求结果")
    Object result;

}