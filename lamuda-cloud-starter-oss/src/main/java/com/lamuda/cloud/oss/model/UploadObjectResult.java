package com.lamuda.cloud.oss.model;

import lombok.Builder;
import lombok.Data;

/**
 * UploadObject
 *
 * @author jin
 */
@Data
@Builder
public class UploadObjectResult {

    private String url;
    private String key;
}
