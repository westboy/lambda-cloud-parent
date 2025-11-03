package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩主动申请启动充电请求详细信息
 * <p>
 * 帧类型码：0xA5
 * 对应协议文档：8.1 充电桩主动申请启动充电
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A5", name = "充电桩主动申请启动充电", description = "充电桩主动申请启动充电请求详细信息")
public class YkcV20StartChargingRequestDetail {

    /**
     * 桩编号 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 启动方式 (1字节)
     * 0x01表示通过刷卡启动充电
     * 0x02表求通过帐号启动充电（暂不支持）
     * 0x03表示vin码启动充电
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "启动方式")
    private Integer startMode;

    /**
     * 是否需要密码 (1字节)
     * 0x00不需要，0x01需要
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "是否需要密码")
    private Integer needPassword;

    /**
     * 账号或者物理卡号 (8字节)
     * 不足8位补0，具体见示例
     */
    @ProtocolField(order = 5, length = 8, dataType = ProtocolDataType.HEX, description = "账号或者物理卡号")
    private byte[] accountOrCardNumber;

    /**
     * 输入密码 (16字节)
     * 对用户输入的密码进行16位MD5加密，采用小写上传
     */
    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.HEX, description = "输入密码")
    private byte[] inputPassword;

    /**
     * VIN码 (17字节)
     * 启动方式为vin码启动充电时上送，其他方式置零（ASCII码），VIN码需要反序上送
     */
    @ProtocolField(order = 7, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vinCode;
}
