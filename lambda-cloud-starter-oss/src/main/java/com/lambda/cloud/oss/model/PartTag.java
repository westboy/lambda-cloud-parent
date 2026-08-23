package com.lambda.cloud.oss.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分片标签模型
 * 封装分片上传所需的分片号与 ETag，与具体 SDK 类型解耦
 *
 * @author jpjoo
 * @since 2026.1.1
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PartTag {

    /**
     * 分片号（从 1 开始）
     */
    private Integer partNumber;

    /**
     * 分片 ETag
     */
    private String eTag;
}
