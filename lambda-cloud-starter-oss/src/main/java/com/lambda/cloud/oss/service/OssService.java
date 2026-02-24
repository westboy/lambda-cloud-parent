package com.lambda.cloud.oss.service;

import com.amazonaws.services.s3.model.S3Object;
import com.lambda.cloud.oss.enums.AccessPolicyType;
import com.lambda.cloud.oss.model.UploadObjectResult;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * OSS 服务接口
 * 定义对象存储的核心操作，提供统一的 API 抽象
 * 
 * <p>该接口定义了对象存储的标准操作，包括：
 * <ul>
 *   <li>文件上传（支持字节数组、输入流、文件对象）</li>
 *   <li>分片上传（用于大文件）</li>
 *   <li>文件下载</li>
 *   <li>文件删除</li>
 *   <li>预签名 URL 生成</li>
 *   <li>访问策略管理</li>
 * </ul>
 * 
 * <p>实现类应确保：
 * <ul>
 *   <li>所有方法都进行参数校验</li>
 *   <li>正确处理资源释放</li>
 *   <li>提供详细的异常信息</li>
 *   <li>记录关键操作日志</li>
 * </ul>
 *
 * @author jpjoo
 * @since 2025.1.1
 * @see com.lambda.cloud.oss.client.OssClient
 */
public interface OssService {
    
    /**
     * 上传文件（字节数组）
     * 
     * <p>适用于小文件或已在内存中的数据
     *
     * @param data 文件数据（不能为 null 或空数组）
     * @param objectKey 对象键（不能为空，不能以斜杠开头，最大 1024 字符）
     * @param contentType 内容类型（不能为空，如 "image/png", "application/pdf"）
     * @return 上传结果，包含对象 URL 和其他元数据
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果上传失败
     */
    UploadObjectResult upload(byte[] data, String objectKey, String contentType);
    
    /**
     * 上传文件（输入流）
     * 
     * <p>适用于流式数据或大文件
     * <p>注意：调用者负责关闭输入流
     *
     * @param inputStream 输入流（不能为 null）
     * @param objectKey 对象键（不能为空，不能以斜杠开头，最大 1024 字符）
     * @param contentType 内容类型（不能为空）
     * @return 上传结果，包含对象 URL 和其他元数据
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果上传失败
     */
    UploadObjectResult upload(InputStream inputStream, String objectKey, String contentType);
    
    /**
     * 上传文件（文件对象）
     * 
     * <p>自动检测文件类型，适用于本地文件上传
     *
     * @param file 文件对象（不能为 null，必须存在且可读）
     * @param objectKey 对象键（不能为空，不能以斜杠开头，最大 1024 字符）
     * @return 上传结果，包含对象 URL 和其他元数据
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果上传失败
     */
    UploadObjectResult upload(File file, String objectKey);
    
    /**
     * 分片上传（简化版本）
     * 
     * <p>自动检测文件类型，适用于大文件分片上传
     * <p>注意：需要按顺序上传所有分片，最后一个分片会自动完成上传
     *
     * @param file 文件对象（不能为 null，必须存在且可读）
     * @param objectKey 对象键（不能为空，不能以斜杠开头，最大 1024 字符）
     * @param partNumber 当前分片号（从 1 开始，不能大于 partTotalNumber）
     * @param partTotalNumber 总分片数（必须大于 0，建议不超过 10000）
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果上传失败
     */
    void uploadPart(File file, String objectKey, int partNumber, int partTotalNumber);
    
    /**
     * 分片上传（完整版本）
     * 
     * <p>支持指定内容类型，适用于大文件分片上传
     * <p>注意：需要按顺序上传所有分片，最后一个分片会自动完成上传
     *
     * @param file 文件对象（不能为 null，必须存在且可读）
     * @param contentType 内容类型（不能为空）
     * @param objectKey 对象键（不能为空，不能以斜杠开头，最大 1024 字符）
     * @param partNumber 当前分片号（从 1 开始，不能大于 partTotalNumber）
     * @param partTotalNumber 总分片数（必须大于 0，建议不超过 10000）
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果上传失败
     */
    void uploadPart(File file, String contentType, String objectKey, 
                    int partNumber, int partTotalNumber);
    
    /**
     * 删除文件
     * 
     * <p>如果文件不存在，不会抛出异常
     *
     * @param objectKey 对象键（不能为空）
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果删除失败
     */
    void delete(String objectKey);
    
    /**
     * 获取文件对象
     * 
     * <p>返回的 S3Object 包含文件元数据和输入流
     * <p>注意：调用者负责关闭返回的 S3Object
     *
     * @param objectKey 对象键（不能为空）
     * @return S3 对象，包含文件内容和元数据
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果获取失败
     */
    S3Object getObject(String objectKey);
    
    /**
     * 下载文件到输出流
     * 
     * <p>自动管理资源，确保正确关闭
     * <p>注意：调用者负责关闭输出流
     *
     * @param objectKey 对象键（不能为空）
     * @param outputStream 输出流（不能为 null）
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果下载失败
     */
    void outStream(String objectKey, OutputStream outputStream);
    
    /**
     * 生成预签名 URL
     * 
     * <p>用于临时授权访问私有文件
     * <p>URL 在指定时间后自动失效
     *
     * @param objectKey 对象键（不能为空）
     * @param expirationSeconds 过期时间（秒，必须大于 0，最大 604800 即 7 天）
     * @return 预签名 URL
     * @throws IllegalArgumentException 如果参数无效
     * @throws com.lambda.cloud.oss.exception.OssException 如果生成失败
     */
    String getPrivateUrl(String objectKey, Integer expirationSeconds);
    
    /**
     * 获取存储桶的访问策略
     * 
     * <p>返回当前存储桶的访问策略类型
     *
     * @return 访问策略类型（PRIVATE, PUBLIC, CUSTOM）
     * @throws com.lambda.cloud.oss.exception.OssException 如果获取失败
     */
    AccessPolicyType getAccessPolicy();
    
    /**
     * 创建存储桶
     * 
     * <p>如果存储桶已存在，不会重复创建
     * <p>仅 MinIO 类型的 OSS 支持此操作
     *
     * @throws com.lambda.cloud.oss.exception.OssException 如果创建失败
     */
    void createBucket();
}
