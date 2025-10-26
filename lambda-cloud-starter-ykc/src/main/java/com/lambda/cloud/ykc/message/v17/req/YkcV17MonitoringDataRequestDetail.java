package com.lambda.cloud.ykc.message.v17.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.7协议读取实时监测数据请求消息体
 * <p>
 * 对应协议帧类型 0x12，读取实时监测数据的消息体部分
 * 样例报文: 68 0C 0000 00 12 32010200000001 01 0069
 * </p>
 *
 * @author Jin
 */
@ToString
@Getter
@Setter
@ProtocolFrame(frameType = "0x12", name = "读取实时监测数据请求", description = "读取实时监测数据请求消息体", version = "1.7")
public class YkcV17MonitoringDataRequestDetail {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(
            order = 1,
            length = 7,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "桩编号")
    private String stationCode;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(
            order = 2,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.BCD,
            littleEndian = true,
            description = "枪号")
    private Integer connectorId;

    /**
     * 默认构造函数
     */
    public YkcV17MonitoringDataRequestDetail() {}

    /**
     * 全参数构造函数
     *
     * @param stationCode 桩编号
     * @param connectorId 枪号
     */
    public YkcV17MonitoringDataRequestDetail(String stationCode, Integer connectorId) {
        this.stationCode = stationCode;
        this.connectorId = connectorId;
    }
}
