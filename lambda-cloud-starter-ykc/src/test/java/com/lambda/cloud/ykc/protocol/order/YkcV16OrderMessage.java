package com.lambda.cloud.ykc.protocol.order;

import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import com.lambda.cloud.ykc.protocol.base.YkcV16BaseMessage;

@ProtocolFrame(frameType = "base", name = "云快充基础协议", isPayload = true, description = "云快充基础协议字段")
public class YkcV16OrderMessage extends YkcV16BaseMessage<YkcV16OrderDetail> {}
