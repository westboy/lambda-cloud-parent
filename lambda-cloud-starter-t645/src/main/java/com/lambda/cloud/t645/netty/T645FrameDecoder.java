package com.lambda.cloud.t645.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class T645FrameDecoder extends ByteToMessageDecoder {

    private static final int MIN_FRAME_LENGTH = 12;
    private static final int MAX_FRAME_LENGTH = 267;
    private static final byte FRAME_HEADER = 0x68;
    private static final byte FRAME_END = 0x16;
    private static final int LENGTH_OFFSET = 9;

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
            in.readByte(); // 读取第一个 0x68

            if (in.readableBytes() < LENGTH_OFFSET - 1) {
                in.resetReaderIndex();
                return;
            }

            in.skipBytes(6);
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
