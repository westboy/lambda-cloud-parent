package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 日志召唤应答
 * <p>
 * 帧类型码：0x97
 * 对应协议文档：11.8 日志召唤应答（可选）
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：桩本地日志召唤应答
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "97", name = "日志召唤应答", description = "桩在收到平台日志召唤帧后回复")
public class YkcV20LogCallReplyUp {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 召唤结果 (1字节)
     * 0x00失败
     * 0x01成功
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "召唤结果")
    private Integer result;

    /**
     * 失败原因 (1字节)
     * 0x00无
     * 0x01无数据
     * 0x02数据过大，上传失败
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "失败原因")
    private Integer failReason;

    /**
     * 日志文件数量 (2字节)
     * BIN码
     */
    @ProtocolField(order = 4, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "日志文件数量")
    private Integer logFileCount;

    /**
     * 日志文件编码 (2字节)
     * Bit位表示（0否1是），低位到高位顺序
     * Bit1：使用ASCII
     * Bit2：使用IS0-8859-1
     * Bit3：使用GBK
     * Bit4：使用GB2312
     * Bit5：使用GB18030
     * Bit6：使用UTF-8
     * Bit7：使用UTF-16
     * Bit8：使用UTF-32
     */
    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "日志文件编码")
    private Integer logFileCode;
}