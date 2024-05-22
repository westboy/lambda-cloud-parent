package com.jingfang.cloud.swagger.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * @author w
 */
@Data
@Schema(description = "分页信息")
public class Page<T> {

    @Schema(required = true, description = "当前页码")
    private Integer number;

    @Schema(description = "每页的数据量")
    private Integer size;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "总页数")
    private Integer pages;

    @Schema(description = "数据列表")
    private List<T> data;
}