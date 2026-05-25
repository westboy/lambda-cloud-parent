package com.lambda.cloud.t645.netty;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.t645.message.T645CodecSupport;
import com.lambda.cloud.t645.message.T645Frame;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class T645BodyDecoder extends MessageToMessageDecoder<ByteBuf> {

    private final ProtocolEngine<Object> protocolEngine;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        // 先解析外层帧
        T645Frame frame;
        try {
            frame = (T645Frame) protocolEngine.parse(in, T645Frame.class);
        } catch (Exception e) {
            log.error("Failed to parse T645Frame outer shell", e);
            return;
        }

        int controlCode = frame.getControlCode();
        byte[] rawData = frame.getData();

        if (rawData == null || rawData.length == 0) {
            // 无数据域的报文，尝试查找空数据域的 payload（如纯心跳应答）
            Class<?> payloadClass = T645PayloadRegistry.lookup(controlCode, null);
            if (payloadClass != null) {
                try {
                    Object body = payloadClass.getDeclaredConstructor().newInstance();
                    frame.setBody(body);
                } catch (Exception e) {
                    log.error("Failed to instantiate payload class: {}", payloadClass.getName(), e);
                }
            }
            out.add(frame);
            return;
        }

        byte[] processData;
        String di = null;

        if (T645PayloadRegistry.isStandardControlCode(controlCode)) {
            // 标准报文：数据域减去 0x33 偏移
            processData = T645CodecSupport.subOffset33(rawData);
            if (processData.length >= 4) {
                // 提取 DI（4字节，倒序）
                byte[] diBytes = new byte[4];
                System.arraycopy(processData, 0, diBytes, 0, 4);
                // 645的DI是低字节在前，我们转成高字节在前的十六进制字符串做为 key
                StringBuilder diBuilder = new StringBuilder(8);
                for (int i = 3; i >= 0; i--) {
                    diBuilder.append(String.format("%02X", diBytes[i] & 0xFF));
                }
                di = diBuilder.toString();
            }
        } else {
            // 4G/NB 私有报文：保留原始字节
            processData = rawData;
            // 对于私有心跳，通常没有 4 字节的 DI，可能通过第一个字节（子功能码）或无 DI 路由
            // 这里暂且传递 null，由 registry 根据 controlCode 查找
        }

        Class<?> payloadClass = T645PayloadRegistry.lookup(controlCode, di);
        if (payloadClass == null) {
            log.debug("No payload registered for controlCode: 0x{}, di: {}", Integer.toHexString(controlCode), di);
            out.add(frame);
            return;
        }

        ByteBuf buf = Unpooled.wrappedBuffer(processData);
        try {
            Object body = protocolEngine.parse(buf, payloadClass);
            frame.setBody(body);
        } catch (Exception e) {
            log.error(
                    "Failed to decode payload body for controlCode: 0x{}, di: {}",
                    Integer.toHexString(controlCode),
                    di,
                    e);
        } finally {
            buf.release();
        }

        out.add(frame);
    }
}
