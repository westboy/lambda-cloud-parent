package com.lambda.cloud.t645.message.control;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 广播校时报文。
 *
 * <p>控制码 {@code 0x08}，DI {@code BROADCAST_TIME}。
 * 主站向所有表计广播下发当前时间，表计收到后自行校准内部时钟。</p>
 *
 * <p>数据域内容（已去偏移后）：秒 分 时 日 月 年（各 1 字节 BCD，共 6 字节）。</p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "08:BROADCAST_TIME", name = "广播校时")
public class T645BroadcastTime implements T645DataBody {

    /** 广播校时 DI 常量。 */
    public static final String DI = "BROADCAST_TIME";

    /**
     * 校时时间原始数据（6 字节十六进制字符串）。
     *
     * <p>字节顺序：秒 分 时 日 月 年，每个字节均为 BCD 编码。</p>
     */
    @ProtocolField(order = 0, length = 6, dataType = ProtocolDataType.HEX, computed = true, description = "校时时间原始数据")
    private String broadcastTime;

    @Override
    public String getDi() {
        return DI;
    }
}
