package com.lambda.cloud.ykc.message.v20.resp;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 交易记录响应详细信息
 * <p>
 * 帧类型码：0x3D
 * 对应协议文档：8.7 交易记录
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "3D", name = "交易记录", description = "交易记录响应详细信息")
public class YkcV20TransactionRecordResponseDetail {

    /**
     * 交易流水号 (16字节)
     * 见名词解释
     */
    @ProtocolField(order = 1, length = 16, dataType = ProtocolDataType.BCD, description = "交易流水号")
    private String transactionSerialNumber;

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 开始时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(order = 4, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "开始时间")
    private byte[] startTime;

    /**
     * 结束时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(order = 5, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "结束时间")
    private byte[] endTime;

    /**
     * 充电时长 (4字节)
     * 单位：分钟
     */
    @ProtocolField(order = 6, length = 4, dataType = ProtocolDataType.HEX, description = "充电时长")
    private String chargingDuration;

    /**
     * 开始SOC (1字节)
     * 百分比
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.HEX, description = "开始SOC")
    private String startSoc;

    /**
     * 结束SOC (1字节)
     * 百分比
     */
    @ProtocolField(order = 8, length = 1, dataType = ProtocolDataType.HEX, description = "结束SOC")
    private String endSoc;

    /**
     * 充电前电表读数 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(order = 9, length = 4, dataType = ProtocolDataType.HEX, description = "充电前电表读数")
    private String startMeterReading;

    /**
     * 充电后电表读数 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(order = 10, length = 4, dataType = ProtocolDataType.HEX, description = "充电后电表读数")
    private String endMeterReading;

    /**
     * 充电度数 (4字节)
     * 精确到小数点后四位
     */
    @ProtocolField(order = 11, length = 4, dataType = ProtocolDataType.HEX, description = "充电度数")
    private String chargingEnergy;

    /**
     * 停止原因 (1字节)
     * 0x01人工停止，0x02自动停止，0x03故障停止，0x04平台停止，0x05紧急停止，0x06其他
     */
    @ProtocolField(order = 12, length = 1, dataType = ProtocolDataType.BCD, description = "停止原因")
    private String stopReason;

    /**
     * 充电金额 (4字节)
     * 精确到小数点后两位
     */
    @ProtocolField(order = 13, length = 4, dataType = ProtocolDataType.HEX, description = "充电金额")
    private String chargingAmount;

    /**
     * 服务费金额 (4字节)
     * 精确到小数点后两位
     */
    @ProtocolField(order = 14, length = 4, dataType = ProtocolDataType.HEX, description = "服务费金额")
    private String serviceFeeAmount;

    /**
     * 总金额 (4字节)
     * 精确到小数点后两位
     */
    @ProtocolField(order = 15, length = 4, dataType = ProtocolDataType.HEX, description = "总金额")
    private String totalAmount;

    /**
     * 逻辑卡号 (8字节)
     * 显示在屏幕上，不足8位补零
     */
    @ProtocolField(order = 16, length = 8, dataType = ProtocolDataType.BCD, description = "逻辑卡号")
    private String logicalCardNumber;

    /**
     * 物理卡号 (8字节)
     * 不足8位补零
     */
    @ProtocolField(order = 17, length = 8, dataType = ProtocolDataType.HEX, description = "物理卡号")
    private String physicalCardNumber;

    /**
     * 计费模型编号 (2字节)
     */
    @ProtocolField(order = 18, length = 2, dataType = ProtocolDataType.BCD, description = "计费模型编号")
    private String billingModelNumber;

    /**
     * VIN码 (17字节)
     * ASCII编码
     */
    @ProtocolField(order = 19, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vinCode;
}
