package com.lamuda.cloud.logger.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class OperationBody {

    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "编号")
    String id;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "方法")
    String method;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "所属模块")
    String module;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "描述")
    String description;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "操作人员")
    String operator;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "操作类型")
    String httpMethod;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "操作时间")
    Date time;
    @JsonProperty("cast")
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "花费时长")
    long duration;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "详情")
    String detail;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "")
    String operatorId;
    String tenantId;
    @Schema(requiredMode = Schema.RequiredMode.REQUIRED, description = "IP地址")
    String ipaddress;
}