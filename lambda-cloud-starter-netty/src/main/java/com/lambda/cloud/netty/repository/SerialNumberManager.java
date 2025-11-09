package com.lambda.cloud.netty.repository;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 序列号管理器
 * 负责生成和管理通信序列号，确保序列号的唯一性和循环使用
 *
 * @author Jin
 */
@Data
@Slf4j
@SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
public class SerialNumberManager {

    /**
     * 当前序列号
     */
    private final AtomicInteger currentSerial;

    /**
     * 最大序列号值（默认为0xFFFF）
     */
    private final int maxSerial;

    /**
     * 最小序列号值（默认为1）
     */
    private final int minSerial;

    /**
     * 默认构造函数
     * 使用默认的序列号范围：1 到 0xFFFF
     */
    public SerialNumberManager() {
        this(1, 0xFFFF);
    }

    /**
     * 构造函数
     *
     * @param minSerial 最小序列号
     * @param maxSerial 最大序列号
     */
    public SerialNumberManager(int minSerial, int maxSerial) {
        if (minSerial < 0 || maxSerial <= minSerial) {
            throw new IllegalArgumentException("Invalid serial number range: min=" + minSerial + ", max=" + maxSerial);
        }
        this.minSerial = minSerial;
        this.maxSerial = maxSerial;
        this.currentSerial = new AtomicInteger(minSerial - 1);
    }

    /**
     * 获取下一个序列号
     * 线程安全的序列号生成，支持循环使用
     *
     * @return 下一个序列号
     */
    public int nextSerial() {
        int next = currentSerial.updateAndGet(current -> {
            int nextValue = current + 1;
            if (nextValue > maxSerial) {
                nextValue = minSerial;
            }
            return nextValue;
        });

        log.debug("Generated serial number: {}", next);
        return next;
    }

    /**
     * 获取当前序列号（不递增）
     *
     * @return 当前序列号
     */
    public int getCurrentSerial() {
        int current = currentSerial.get();
        return Math.max(current, minSerial);
    }

    /**
     * 重置序列号到最小值
     */
    public void reset() {
        currentSerial.set(minSerial - 1);
        log.debug("Serial number manager reset to min value: {}", minSerial);
    }

    /**
     * 设置当前序列号
     *
     * @param serial 要设置的序列号
     * @throws IllegalArgumentException 如果序列号超出范围
     */
    public void setCurrentSerial(int serial) {
        if (serial < minSerial || serial > maxSerial) {
            throw new IllegalArgumentException(
                    "Serial number out of range: " + serial + " (valid range: " + minSerial + " - " + maxSerial + ")");
        }
        currentSerial.set(serial);
        log.debug("Serial number set to: {}", serial);
    }

    /**
     * 检查序列号是否在有效范围内
     *
     * @param serial 要检查的序列号
     * @return 如果在有效范围内返回true，否则返回false
     */
    public boolean isValidSerial(int serial) {
        return serial >= minSerial && serial <= maxSerial;
    }

    /**
     * 获取序列号范围信息
     *
     * @return 序列号范围描述
     */
    public String getRangeInfo() {
        return String.format("Serial range: %d - %d, current: %d", minSerial, maxSerial, getCurrentSerial());
    }

    /**
     * 获取剩余可用序列号数量
     *
     * @return 剩余可用序列号数量
     */
    public int getRemainingCount() {
        int current = getCurrentSerial();
        if (current == maxSerial) {
            return maxSerial - minSerial + 1; // 全部可用（即将循环）
        }
        return maxSerial - current;
    }

    /**
     * 创建一个新的序列号管理器实例
     *
     * @param minSerial 最小序列号
     * @param maxSerial 最大序列号
     * @return 新的序列号管理器实例
     */
    public static SerialNumberManager create(int minSerial, int maxSerial) {
        return new SerialNumberManager(minSerial, maxSerial);
    }

    /**
     * 创建一个默认的序列号管理器实例
     *
     * @return 默认的序列号管理器实例
     */
    public static SerialNumberManager createDefault() {
        return new SerialNumberManager();
    }
}
