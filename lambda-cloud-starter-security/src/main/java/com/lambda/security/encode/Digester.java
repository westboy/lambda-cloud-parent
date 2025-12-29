/*
 * Copyright 2011-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.lambda.security.encode;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 消息摘要算法辅助工具类
 * <p>
 * 提供对Java MessageDigest API的封装，支持多次迭代哈希计算。
 * 通过配置的迭代次数对输入数据进行多轮哈希运算，有效防止暴力破解攻击。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>支持多种哈希算法（SHA-1、SHA-256、MD5等）</li>
 *   <li>可配置迭代次数，增强安全性</li>
 *   <li>提供简洁的API接口</li>
 *   <li>内置算法有效性验证</li>
 * </ul>
 *
 * <h3>安全特性：</h3>
 * <ul>
 *   <li>多次迭代哈希，增加破解难度</li>
 *   <li>支持主流安全哈希算法</li>
 *   <li>参数验证，防止无效配置</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 创建SHA-256摘要器，迭代1000次
 * Digester digester = new Digester("SHA-256", 1000);
 * byte[] hash = digester.digest("password".getBytes());
 * }</pre>
 *
 * @author Keith Donald
 * @author Luke Taylor
 * @since 1.0.0
 */
final class Digester {

    /**
     * 哈希算法名称
     * <p>
     * 指定使用的消息摘要算法，如"SHA-1"、"SHA-256"、"MD5"等。
     * 算法名称必须是Java平台支持的标准算法。
     * </p>
     */
    private final String algorithm;

    /**
     * 迭代次数
     * <p>
     * 指定哈希算法的执行次数，用于增强安全性。
     * 迭代次数越多，安全性越高，但计算时间也会相应增加。
     * 必须大于0。
     * </p>
     */
    private int iterations;

    /**
     * 创建新的消息摘要器
     * <p>
     * 使用指定的哈希算法和迭代次数创建摘要器实例。
     * 构造时会立即验证算法的有效性，确保后续操作的可靠性。
     * </p>
     *
     * @param algorithm 摘要算法名称，例如"SHA-1"或"SHA-256"
     * @param iterations 哈希算法的迭代执行次数，必须大于0
     * @throws IllegalStateException 当指定的算法不被支持时抛出
     * @throws IllegalArgumentException 当迭代次数小于等于0时抛出
     */
    Digester(String algorithm, int iterations) {
        // eagerly validate the algorithm
        createDigest(algorithm);
        this.algorithm = algorithm;
        setIterations(iterations);
    }

    /**
     * 对输入数据进行摘要计算
     * <p>
     * 使用配置的算法和迭代次数对输入字节数组进行哈希计算。
     * 每次迭代都会对上一次的结果进行哈希，最终返回经过多轮计算的摘要值。
     * </p>
     *
     * @param value 待计算摘要的字节数组
     * @return 经过多次迭代计算后的摘要字节数组
     * @throws IllegalStateException 当算法不可用时抛出
     */
    public byte[] digest(byte[] value) {
        MessageDigest messageDigest = createDigest(algorithm);
        for (int i = 0; i < iterations; i++) {
            value = messageDigest.digest(value);
        }
        return value;
    }

    /**
     * 设置迭代次数
     * <p>
     * 配置哈希算法的执行次数。迭代次数必须大于0，
     * 较高的迭代次数可以提供更好的安全性，但会增加计算时间。
     * </p>
     *
     * @param iterations 迭代次数，必须大于0
     * @throws IllegalArgumentException 当迭代次数小于等于0时抛出
     */
    void setIterations(int iterations) {
        if (iterations <= 0) {
            throw new IllegalArgumentException("Iterations value must be greater than zero");
        }
        this.iterations = iterations;
    }

    /**
     * 创建消息摘要实例
     * <p>
     * 根据指定的算法名称创建MessageDigest实例。
     * 如果算法不被当前Java平台支持，将抛出异常。
     * </p>
     *
     * @param algorithm 哈希算法名称
     * @return MessageDigest实例
     * @throws IllegalStateException 当指定的算法不被支持时抛出
     */
    private static MessageDigest createDigest(String algorithm) {
        try {
            return MessageDigest.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("No such hashing algorithm", e);
        }
    }
}
