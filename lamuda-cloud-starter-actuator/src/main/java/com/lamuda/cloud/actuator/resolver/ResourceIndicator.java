package com.lamuda.cloud.actuator.resolver;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author jin
 */
@Getter
@Setter
public class ResourceIndicator {

    @Schema(description = "版本号")
    private String version;
    @Schema(description = "修改时间")
    private String modified;

}
