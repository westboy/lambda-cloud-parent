package com.lambda.cloud.swagger.model;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import lombok.Data;

/**
 * @author w
 */
@Data
@Schema(description = "分页信息")
public class Page<T> {

    @Schema(description = "当前页码")
    private Integer number;

    @Schema(description = "每页的数据量")
    private Integer size;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Integer pages;

    @SuppressFBWarnings(value = {"EI_EXPOSE_REP"})
    @Schema(description = "数据列表")
    private List<T> data;
}
