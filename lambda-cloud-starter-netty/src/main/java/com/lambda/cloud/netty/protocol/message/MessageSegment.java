package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.packet.Bit;
import com.lambda.cloud.netty.protocol.packet.BitBox;
import com.lambda.cloud.netty.protocol.packet.Unit;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.extern.slf4j.Slf4j;

/**
 * 消息段处理类
 * <p>
 * 提供以下功能：
 * <ul>
 *   <li>消息单元管理：添加、删除、清空单元</li>
 *   <li>位操作支持：设置位值、位组操作</li>
 *   <li>批量数据处理：读写多个单元</li>
 *   <li>性能优化：预分配容量、并发计数</li>
 *   <li>异常处理：数据验证和错误恢复</li>
 * </ul>
 * </p>
 *
 * @author Lambda Cloud Team
 * @since 1.0.0
 */
@Slf4j
public class MessageSegment implements Unit {
    /**
     * 默认初始容量
     */
    private static final int DEFAULT_CAPACITY = 16;

    /**
     * 单元列表
     */
    private final List<Unit> units;

    /**
     * 单元计数器
     */
    private final AtomicInteger unitCount = new AtomicInteger(0);

    /**
     * 构造函数
     */
    public MessageSegment() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * 指定容量的构造函数
     *
     * @param initialCapacity 初始容量
     */
    public MessageSegment(int initialCapacity) {
        this.units = new ArrayList<>(initialCapacity);
    }

    /**
     * 添加数据单元
     *
     * @param unit 数据单元
     * @throws IllegalArgumentException 如果单元为空
     */
    public void add(Unit unit) {
        Objects.requireNonNull(unit, "数据单元不能为空");
        this.units.add(unit);
        unitCount.incrementAndGet();
    }

    /**
     * 批量添加数据单元
     *
     * @param units 数据单元列表
     * @throws IllegalArgumentException 如果单元列表为空
     */
    public void addAll(List<Unit> units) {
        Objects.requireNonNull(units, "数据单元列表不能为空");
        this.units.addAll(units);
        unitCount.addAndGet(units.size());
    }

    /**
     * 移除指定位置的数据单元
     *
     * @param index 位置索引
     * @return 被移除的数据单元
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public Unit remove(int index) {
        Unit removed = this.units.remove(index);
        if (removed != null) {
            unitCount.decrementAndGet();
        }
        return removed;
    }

    /**
     * 清空所有数据单元
     */
    public void clearUnits() {
        units.clear();
        unitCount.set(0);
    }

    /**
     * 获取数据单元数量
     *
     * @return 单元数量
     */
    public int getUnitCount() {
        return unitCount.get();
    }

    /**
     * 设置位值
     *
     * @param bit 位值
     * @throws IllegalStateException 如果最后一个单元不是 BitBox
     */
    public void setBit(Bit bit) {
        if (units.isEmpty()) {
            throw new IllegalStateException("没有可用的位组单元");
        }
        Unit lastUnit = units.get(units.size() - 1);
        if (!(lastUnit instanceof BitBox)) {
            throw new IllegalStateException("最后一个单元不是位组类型");
        }
        ((BitBox) lastUnit).add(bit);
    }

    @Override
    public void read(ByteBuf byteBuf) {
        Objects.requireNonNull(byteBuf, "ByteBuf 不能为空");
        
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            if (byteBuf.readableBytes() == 0 || byteBuf.readableBytes() < unit.getLength()) {
                log.warn("数据长度不足，停止读取。可读字节：{}，需要字节：{}", 
                    byteBuf.readableBytes(), unit.getLength());
                return;
            }
            try {
                unit.read(byteBuf);
            } catch (Exception e) {
                log.error("读取数据单元失败", e);
                throw new RuntimeException("数据读取失败", e);
            }
        }
    }

    @Override
    public void write(ByteBuf byteBuf) {
        Objects.requireNonNull(byteBuf, "ByteBuf 不能为空");
        
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            try {
                unit.write(byteBuf);
            } catch (Exception e) {
                log.error("写入数据单元失败", e);
                throw new RuntimeException("数据写入失败", e);
            }
        }
    }

    @Override
    public int getLength() {
        return units.stream()
            .filter(Objects::nonNull)
            .mapToInt(Unit::getLength)
            .sum();
    }

    @Override
    public String getPrintData() {
        StringBuilder sb = new StringBuilder();
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            sb.append(unit.getPrintData());
        }
        return sb.toString();
    }

    /**
     * 获取指定位置的数据单元
     *
     * @param index 位置索引
     * @return 数据单元
     * @throws IndexOutOfBoundsException 如果索引越界
     */
    public Unit getUnit(int index) {
        return units.get(index);
    }

    /**
     * 检查消息段是否为空
     *
     * @return true 如果没有数据单元
     */
    public boolean isEmpty() {
        return units.isEmpty();
    }

    @Override
    public String toString() {
        return String.format(
            "MessageSegment{unitCount=%d, totalLength=%d, units=%s}",
            getUnitCount(), getLength(), units
        );
    }
}
