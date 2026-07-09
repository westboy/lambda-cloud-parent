package com.lambda.cloud.ocpp.netty;

import com.lambda.cloud.ocpp.codec.OcppMessageCodec;
import com.lambda.cloud.ocpp.message.OcppMessage;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import java.util.List;
import lombok.RequiredArgsConstructor;

/**
 * OCPP-J 编码器:{@link OcppMessage} -> {@link TextWebSocketFrame}(JSON 数组)。
 * <p>无状态,可共享({@code @Sharable});编解码委托 {@link OcppMessageCodec}。</p>
 */
@ChannelHandler.Sharable
@RequiredArgsConstructor
public class OcppMessageEncoder extends MessageToMessageEncoder<OcppMessage> {

    private final OcppMessageCodec codec;

    @Override
    protected void encode(ChannelHandlerContext ctx, OcppMessage message, List<Object> out) throws Exception {
        String json = codec.encode(message);
        out.add(new TextWebSocketFrame(json));
    }
}
