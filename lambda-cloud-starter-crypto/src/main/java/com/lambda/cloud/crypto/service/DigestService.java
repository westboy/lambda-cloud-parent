package com.lambda.cloud.crypto.service;

import com.lambda.cloud.crypto.algorithm.DigestAlgorithm;

/**
 * 摘要服务扩展点
 *
 * @author Jin
 * @since 2026.1.1
 */
public interface DigestService {

    /**
     * 计算数据摘要
     *
     * @param data 原始数据
     * @param algorithm 摘要算法
     * @return 摘要值
     */
    byte[] digest(byte[] data, DigestAlgorithm algorithm);

    /**
     * 计算数据摘要（SHA-256）
     *
     * @param data 原始数据
     * @return 摘要值
     */
    byte[] digest(byte[] data);
}
