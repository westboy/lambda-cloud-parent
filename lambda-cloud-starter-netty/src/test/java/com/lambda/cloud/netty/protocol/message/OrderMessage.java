package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;

@ProtocolPayload(frameType = "base", name = "云快充基础协议", isFrame = true, description = "云快充基础协议字段")
public class OrderMessage extends YkcV16BaseMessage<RawInnerRecord> {}
