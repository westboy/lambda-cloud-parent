package com.lambda.cloud.t645.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

/**
 * T645 协议帧定界解码器（第一层解码）。
 *
 * <p>继承 Netty 的 {@link ByteToMessageDecoder}，基于 DL/T 645-2007 帧结构
 * 从 TCP 字节流中提取完整帧，交给下游 {@link T645BodyDecoder} 进行业务解码。</p>
 *
 * <p>帧结构定界规则：</p>
 * <ul>
 *   <li>以连续两个 {@code 0x68} 为帧头标识（第一个 {@code 0x68} 为起始符1，
 *       第二个位于偏移量 9 处为起始符2）</li>
 *   <li>数据长度字段位于第 10 字节（偏移量 9），用于计算整帧长度</li>
 *   <li>整帧长度 = 12（固定头尾） + 数据长度</li>
 *   <li>帧尾标识为 {@code 0x16}</li>
 *   <li>最大帧长度限制为 267 字节，超长帧视为异常丢弃</li>
 * </ul>
 *
 * @see T645BodyDecoder
 */
@Slf4j
public class T645FrameDecoder extends ByteToMessageDecoder {

    /** 最小帧长度：起始符(1) + 地址域(6) + 起始符(1) + 控制码(1) + 数据长度(1) + CS(1) + 结束符(1) = 12 */
    private static final int MIN_FRAME_LENGTH = 12;

    /** 最大帧长度：12 + 255（数据域最大长度） = 267 */
    private static final int MAX_FRAME_LENGTH = 267;

    /** 帧头/起始符，固定为 0x68 */
    private static final byte FRAME_HEADER = 0x68;

    /** 帧结束符，固定为 0x16 */
    private static final byte FRAME_END = 0x16;

    /** 数据长度字段在帧中的偏移量（0起始，跳过起始符1+地址域6+起始符2+控制码 = 9） */
    private static final int LENGTH_OFFSET = 9;

    /**
     * 从字节流中搜索并提取完整的 T645 帧。
     *
     * <p>处理流程：
     * <ol>
     *   <li>搜索第一个 {@code 0x68} 帧头，丢弃之前的无效数据</li>
     *   <li>验证第二个 {@code 0x68}（偏移量 9）是否存在</li>
     *   <li>读取数据长度字段，计算整帧长度</li>
     *   <li>等待数据完整后，校验帧尾 {@code 0x16}</li>
     *   <li>通过校验则输出完整帧 ByteBuf 给下游</li>
     * </ol>
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        while (in.readableBytes() >= MIN_FRAME_LENGTH) {
            int startIndex = in.indexOf(in.readerIndex(), in.writerIndex(), FRAME_HEADER);
            if (startIndex < 0) {
                // 如果找不到帧头，丢弃所有数据
                in.skipBytes(in.readableBytes());
                return;
            }
            if (startIndex > in.readerIndex()) {
                // 丢弃帧头之前的无效数据
                in.skipBytes(startIndex - in.readerIndex());
            }

            if (in.readableBytes() < MIN_FRAME_LENGTH) {
                return;
            }

            in.markReaderIndex();
            in.readByte(); // 读取起始符1（0x68）

            if (in.readableBytes() < LENGTH_OFFSET - 1) {
                in.resetReaderIndex();
                return;
            }

            // 跳过地址域(6字节)
            in.skipBytes(6);
            // 验证起始符2是否为 0x68
            byte secondHeader = in.readByte();
            if (secondHeader != FRAME_HEADER) {
                in.resetReaderIndex();
                in.skipBytes(1); // 重新搜索
                continue;
            }

            in.skipBytes(1);
            int dataLength = in.readUnsignedByte();
            int fullFrameLength = MIN_FRAME_LENGTH + dataLength;

            if (fullFrameLength > MAX_FRAME_LENGTH) {
                log.warn("Frame length exceeds maximum: {}", fullFrameLength);
                in.resetReaderIndex();
                in.skipBytes(1); // 丢弃当前 0x68，继续搜索
                continue;
            }

            in.resetReaderIndex();

            if (in.readableBytes() < fullFrameLength) {
                return;
            }

            byte endByte = in.getByte(in.readerIndex() + fullFrameLength - 1);
            if (endByte != FRAME_END) {
                log.warn("Invalid frame end marker: 0x{}", String.format("%02X", endByte & 0xFF));
                in.skipBytes(1); // 丢弃当前 0x68，继续搜索
                continue;
            }

            ByteBuf frame = in.readRetainedSlice(fullFrameLength);
            out.add(frame);
            log.debug("Decoded T645 frame, length: {}", fullFrameLength);
        }
    }
}
