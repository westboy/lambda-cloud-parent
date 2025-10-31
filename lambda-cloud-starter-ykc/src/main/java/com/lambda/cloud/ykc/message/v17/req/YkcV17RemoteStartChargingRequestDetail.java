package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议运营平台远程控制启机消息体
 * <p>
 * 对应协议帧类型 0x34，当用户通过远程启动充电时，发送本命令
 * </p>
 *
 * @author Jin
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "0x34", name = "运营平台远程控制启机", description = "当用户通过远程启动充电时，发送本命令", version = "1.7")
public class YkcV17RemoteStartChargingRequestDetail {

    /**
     * 交易流水号 (16字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 16, computed = true, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionId;

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 2, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /**
     * 逻辑卡号 (8字节)
     * BCD码，显示在屏幕上，不足补0，逻辑卡号为卡面印刷卡号
     */
    @ProtocolField(order = 4, length = 8, computed = true, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 物理卡号 (8字节)
     * BIN码，不足补0，桩与平台交互需使用的物理卡号
     */
    @ProtocolField(order = 5, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 账户余额 (4字节)
     * BIN码，保留到小数点两位
     */
    @ProtocolField(
            order = 6,
            length = 4,
            computed = true,
            dataType = ProtocolDataType.UINT32,
            littleEndian = true,
            description = "账户余额")
    private Long accountBalance;
}
