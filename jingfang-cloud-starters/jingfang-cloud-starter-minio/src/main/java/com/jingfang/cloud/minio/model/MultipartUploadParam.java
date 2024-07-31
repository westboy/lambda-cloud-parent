package com.jingfang.cloud.minio.model;

import com.google.common.collect.Multimap;
import io.minio.messages.Part;
import lombok.Getter;
import org.springframework.stereotype.Service;

/**
 * MultipartUploadParam
 *
 * @author jpjoo
 */
@Getter
@Service
public class MultipartUploadParam {
    private String bucketName;

    private String region;

    private String objectName;

    private Multimap<String, String> headers;

    private Multimap<String, String> extraQueryParams;

    private String uploadId;

    private Integer maxParts;

    private Part[] parts;

    private Integer partNumberMarker;

}