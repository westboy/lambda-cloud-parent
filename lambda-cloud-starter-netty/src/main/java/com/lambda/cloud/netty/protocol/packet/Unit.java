package com.lambda.cloud.netty.protocol.packet;

import io.netty.buffer.ByteBuf;

/**
 * 数据包被拆分的最小单元,包含了各个常用的基础类型，以后有新的了可以在这里做补充
 *
 */
public interface Unit {
    /**
     * 将接收到的byteBuf内容转化成指定类型内容
     *
     * @param byteBuf 接收到的原数据
     */
    void read(ByteBuf byteBuf);

    /**
     * 将数据内容写入byteBuf
     *
     * @param byteBuf 要写入的byteBuf
     */
    void write(ByteBuf byteBuf);

    /**
     * 获取数据长度
     *
     * @return 数据长度
     */
    int getLength();

    /**
     * 获取要在控制台打印的数据
     *
     * @return 要打印出来的数据
     */
    String getPrintData();
}
