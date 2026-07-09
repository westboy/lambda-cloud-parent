package com.lambda.cloud.ocpp.netty;

import com.lambda.cloud.ocpp.codec.OcppMessageCodec;
import com.lambda.cloud.ocpp.message.OcppMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OCPP-J 解码器:{@link TextWebSocketFrame}(JSON 数组)-> {@link OcppMessage}。
 * <p>无状态,可共享({@code @Sharable});编解码委托 {@link OcppMessageCodec}。</p>
 */
@Slf4j
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class OcppMessageDecoder extends MessageToMessageDecoder<TextWebSocketFrame> {

    private final OcppMessageCodec codec;

    @Override
    protected void decode(ChannelHandlerContext ctx, TextWebSocketFrame frame, List<Object> out) throws Exception {
        String json = frame.text();
        try {
            OcppMessage message = codec.decode(json);
            out.add(message);
        } catch (Exception e) {
            log.error(
                    "Failed to decode OCPP message from channel {}: {}",
                    ctx.channel().id().asShortText(),
                    json,
                    e);
            throw e;
        }
    }
}
