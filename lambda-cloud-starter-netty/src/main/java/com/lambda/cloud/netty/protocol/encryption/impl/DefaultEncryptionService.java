package com.lambda.cloud.netty.protocol.encryption.impl;

import com.lambda.cloud.netty.exception.ProtocolException;
import com.lambda.cloud.netty.protocol.encryption.EncryptionService;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认AES加密服务实现
 * <p>
 * 使用AES-256-GCM算法提供高安全性的加密解密服务，支持字段级别的密钥管理
 * </p>
 *
 * @author Jin
 */
@Slf4j
public class DefaultEncryptionService implements EncryptionService {

    /**
     * 加密算法
     */
    private static final String ALGORITHM = "AES";

    /**
     * 加密模式
     */
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";

    /**
     * 密钥长度（位）
     */
    private static final int KEY_LENGTH = 256;

    /**
     * GCM标签长度（位）
     */
    private static final int GCM_TAG_LENGTH = 128;

    /**
     * IV长度（字节）
     */
    private static final int IV_LENGTH = 12;

    /**
     * 默认密钥（用于演示，生产环境应使用安全的密钥管理）
     */
    private final SecretKey defaultKey;

    /**
     * 字段级别的密钥缓存
     */
    private final Map<String, SecretKey> fieldKeys = new ConcurrentHashMap<>();

    /**
     * 安全随机数生成器
     */
    private final SecureRandom secureRandom = new SecureRandom();

    /**
     * 构造函数
     */
    public DefaultEncryptionService() {
        this.defaultKey = generateDefaultKey();
        log.info("默认AES加密服务已初始化，算法: {}, 密钥长度: {} 位", TRANSFORMATION, KEY_LENGTH);
    }

    /**
     * 构造函数（使用指定的默认密钥）
     *
     * @param defaultKeyBytes 默认密钥字节数组
     */
    public DefaultEncryptionService(byte[] defaultKeyBytes) {
        if (defaultKeyBytes.length != KEY_LENGTH / 8) {
            throw new IllegalArgumentException("密钥长度必须为 " + KEY_LENGTH / 8 + " 字节");
        }
        this.defaultKey = new SecretKeySpec(defaultKeyBytes, ALGORITHM);
        log.info("默认AES加密服务已初始化（使用指定密钥），算法: {}, 密钥长度: {} 位", TRANSFORMATION, KEY_LENGTH);
    }

    @Override
    public byte[] encrypt(byte[] data, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (data == null || data.length == 0) {
            return data;
        }

        try {
            // 获取字段对应的密钥
            SecretKey key = getFieldKey(fieldMetadata);

            // 创建加密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);

            // 生成随机IV
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            // 初始化加密器
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.ENCRYPT_MODE, key, gcmSpec);

            // 执行加密
            byte[] encryptedData = cipher.doFinal(data);

            // 将IV和加密数据组合
            ByteBuffer buffer = ByteBuffer.allocate(IV_LENGTH + encryptedData.length);
            buffer.put(iv);
            buffer.put(encryptedData);

            byte[] result = buffer.array();

            if (log.isDebugEnabled()) {
                log.debug("字段加密成功: {}, 原始长度: {}, 加密后长度: {}", fieldMetadata.getFieldName(), data.length, result.length);
            }

            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.SERIALIZE_ERROR,
                    "字段加密失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public byte[] decrypt(byte[] encryptedData, ProtocolFieldMetadata fieldMetadata) throws ProtocolException {
        if (encryptedData == null || encryptedData.length <= IV_LENGTH) {
            return encryptedData;
        }

        try {
            // 获取字段对应的密钥
            SecretKey key = getFieldKey(fieldMetadata);

            // 分离IV和加密数据
            ByteBuffer buffer = ByteBuffer.wrap(encryptedData);
            byte[] iv = new byte[IV_LENGTH];
            buffer.get(iv);

            byte[] cipherText = new byte[buffer.remaining()];
            buffer.get(cipherText);

            // 创建解密器
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(GCM_TAG_LENGTH, iv);
            cipher.init(Cipher.DECRYPT_MODE, key, gcmSpec);

            // 执行解密
            byte[] result = cipher.doFinal(cipherText);

            if (log.isDebugEnabled()) {
                log.debug(
                        "字段解密成功: {}, 加密长度: {}, 解密后长度: {}",
                        fieldMetadata.getFieldName(),
                        encryptedData.length,
                        result.length);
            }

            return result;

        } catch (Exception e) {
            throw new ProtocolException(
                    ProtocolException.ErrorCode.PARSE_ERROR,
                    "字段解密失败: " + fieldMetadata.getFieldName() + ", 原因: " + e.getMessage(),
                    fieldMetadata.getFieldName(),
                    e);
        }
    }

    @Override
    public boolean supportsEncryption(ProtocolFieldMetadata fieldMetadata) {
        // 支持所有标记为加密的字段
        return fieldMetadata != null && fieldMetadata.isEncrypted();
    }

    @Override
    public String getAlgorithmName() {
        return TRANSFORMATION;
    }

    @Override
    public int getKeyLength() {
        return KEY_LENGTH;
    }

    /**
     * 获取字段对应的密钥
     *
     * @param fieldMetadata 字段元数据
     * @return 密钥
     */
    private SecretKey getFieldKey(ProtocolFieldMetadata fieldMetadata) {
        String fieldName = fieldMetadata.getFieldName();

        // 尝试从缓存获取字段专用密钥
        SecretKey fieldKey = fieldKeys.get(fieldName);
        if (fieldKey != null) {
            return fieldKey;
        }

        // 检查字段描述中是否包含密钥信息（简单示例）
        String description = fieldMetadata.getDescription();
        if (description != null && description.contains("key:")) {
            try {
                String keyStr =
                        description.substring(description.indexOf("key:") + 4).trim();
                if (keyStr.length() >= 32) { // 至少32个字符用于256位密钥
                    byte[] keyBytes = keyStr.substring(0, 32).getBytes();
                    fieldKey = new SecretKeySpec(Arrays.copyOf(keyBytes, KEY_LENGTH / 8), ALGORITHM);
                    fieldKeys.put(fieldName, fieldKey);
                    return fieldKey;
                }
            } catch (Exception e) {
                log.warn("解析字段密钥失败: {}, 使用默认密钥", fieldName, e);
            }
        }

        // 返回默认密钥
        return defaultKey;
    }

    /**
     * 生成默认密钥
     *
     * @return 默认密钥
     */
    private SecretKey generateDefaultKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_LENGTH);
            return keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("无法生成AES密钥", e);
        }
    }

    /**
     * 设置字段专用密钥
     *
     * @param fieldName 字段名称
     * @param key       密钥
     */
    public void setFieldKey(String fieldName, SecretKey key) {
        fieldKeys.put(fieldName, key);
        log.debug("设置字段专用密钥: {}", fieldName);
    }

    /**
     * 设置字段专用密钥
     *
     * @param fieldName 字段名称
     * @param keyBytes  密钥字节数组
     */
    public void setFieldKey(String fieldName, byte[] keyBytes) {
        if (keyBytes.length != KEY_LENGTH / 8) {
            throw new IllegalArgumentException("密钥长度必须为 " + KEY_LENGTH / 8 + " 字节");
        }
        SecretKey key = new SecretKeySpec(keyBytes, ALGORITHM);
        setFieldKey(fieldName, key);
    }

    /**
     * 移除字段专用密钥
     *
     * @param fieldName 字段名称
     */
    public void removeFieldKey(String fieldName) {
        fieldKeys.remove(fieldName);
        log.debug("移除字段专用密钥: {}", fieldName);
    }

    /**
     * 获取默认密钥的Base64编码（用于配置）
     *
     * @return Base64编码的默认密钥
     */
    public String getDefaultKeyBase64() {
        return Base64.getEncoder().encodeToString(defaultKey.getEncoded());
    }
}
