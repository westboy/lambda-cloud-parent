package com.lambda.cloud.t645.netty;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.t645.message.T645CodecSupport;
import com.lambda.cloud.t645.message.T645DataBody;
import com.lambda.cloud.t645.message.T645Frame;
import com.lambda.cloud.t645.message.T645PayloadRegistry;
import com.lambda.cloud.t645.message.control.T645BroadcastTime;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * T645 协议业务体解码器（第二层解码）。
 *
 * <p>继承 Netty 的 {@link MessageToMessageDecoder}，在 {@link T645FrameDecoder} 完成帧定界后，
 * 对帧内的数据域进行业务解码，流程如下：</p>
 * <ol>
 *   <li>使用 {@link ProtocolEngine} 解析外层 {@link T645Frame} 结构</li>
 *   <li>根据控制码区分私有心跳、广播校时和标准 DI 报文</li>
 *   <li>通过 {@link T645PayloadRegistry} 查找控制码+DI 对应的载荷类</li>
 *   <li>使用 {@link ProtocolEngine} 将数据域解析为具体的 {@link T645DataBody} 实体</li>
 *   <li>将解析结果设置到 {@link T645Frame#body} 字段，传递给下游 Handler</li>
 * </ol>
 *
 * <p>该解码器标记为 {@link io.netty.channel.ChannelHandler.Sharable}，可安全复用。</p>
 *
 * @see T645FrameDecoder
 * @see T645PayloadRegistry
 * @see T645CodecSupport#subOffset33(byte[])
 */
@Slf4j
@RequiredArgsConstructor
@ChannelHandler.Sharable
public class T645BodyDecoder extends MessageToMessageDecoder<ByteBuf> {

    private static final int HEARTBEAT_REQUEST_CONTROL_CODE = 0x00;
    private static final int HEARTBEAT_RESPONSE_CONTROL_CODE = 0x80;
    private static final int BROADCAST_TIME_CONTROL_CODE = 0x08;

    /** 协议引擎，负责将字节流解析为协议帧和业务载荷对象。 */
    private final ProtocolEngine<Object> protocolEngine;

    /**
     * 解码 T645 帧的业务载荷体。
     *
     * @param ctx Channel 上下文
     * @param in  经过帧定界后的 ByteBuf（由 {@link T645FrameDecoder} 输出）
     * @param out 解码结果输出列表
     */
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
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

        if (isHeartbeatControlCode(controlCode)) {
            processData = rawData;
        } else {
            processData = T645CodecSupport.subOffset33(rawData);
            if (controlCode == BROADCAST_TIME_CONTROL_CODE) {
                di = T645BroadcastTime.DI;
            } else if (processData.length >= 4) {
                di = extractDi(processData);
            }
        }

        Class<?> payloadClass = T645PayloadRegistry.lookup(controlCode, di);
        if (payloadClass == null && controlCode == 0x91 && "00000000".equals(di)) {
            log.warn("Fallback to legacy DI mapping for controlCode=0x91, di={}", di);
            payloadClass = T645PayloadRegistry.lookup(controlCode, "00010000");
        }

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

    private static boolean isHeartbeatControlCode(int controlCode) {
        return controlCode == HEARTBEAT_REQUEST_CONTROL_CODE || controlCode == HEARTBEAT_RESPONSE_CONTROL_CODE;
    }

    private static String extractDi(byte[] processData) {
        byte[] diBytes = new byte[4];
        System.arraycopy(processData, 0, diBytes, 0, 4);
        StringBuilder diBuilder = new StringBuilder(8);
        for (int i = 3; i >= 0; i--) {
            diBuilder.append(String.format("%02X", diBytes[i] & 0xFF));
        }
        return diBuilder.toString();
    }
}
