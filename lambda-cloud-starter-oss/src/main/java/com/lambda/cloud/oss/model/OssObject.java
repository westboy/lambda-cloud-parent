package com.lambda.cloud.oss.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import lombok.Builder;
import lombok.Getter;

/**
 * OSS 对象模型
 * 封装从对象存储读取的对象内容和元数据，与具体 SDK 类型解耦
 *
 * <p>调用者负责调用 {@link #close()} 释放底层资源，推荐使用 try-with-resources：
 * <pre>{@code
 * try (OssObject object = ossClient.getObject("path/to/file.txt")) {
 *     InputStream content = object.getContent();
 * }
 * }</pre>
 *
 * @author jpjoo
 * @since 2026.1.1
 */
@Getter
@Builder
public class OssObject implements Closeable {

    /**
     * 存储桶名称
     */
    private final String bucket;

    /**
     * 对象键
     */
    private final String key;

    /**
     * 内容类型
     */
    private final String contentType;

    /**
     * 内容长度（字节）
     */
    private final Long contentLength;

    /**
     * 对象内容输入流
     */
    private final InputStream content;

    @Override
    public void close() throws IOException {
        if (content != null) {
            content.close();
        }
    }
}
