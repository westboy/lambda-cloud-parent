package com.lambda.cloud.ykc.message.v20.up;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩主动申请启动充电
 * <p>
 * 帧类型码：0xA5
 * 对应协议文档：8.1 充电桩主动申请启动充电
 * 数据传输方向：充电桩 → 运营平台（上行）
 * 功能：用户通过帐号密码及刷卡在充电桩上操作请求充电
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "A5", name = "充电桩主动申请启动充电", description = "充电桩主动向运营平台申请启动充电")
public class YkcV20StartChargingUp {

    /**
     * 桩编号 (7字节)
     * BCD码，不足7位补0
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 枪号 (1字节)
     * BCD码
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.BCD, description = "枪号")
    private String connectorId;

    /**
     * 启动方式 (1字节)
     * 0x01表示通过刷卡启动充电
     * 0x02表示通过帐号启动充电（暂不支持）
     * 0x03表示vin码启动充电
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "启动方式")
    private Integer startMethod;

    /**
     * 是否需要密码 (1字节)
     * 0x00不需要
     * 0x01需要
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "是否需要密码")
    private Integer needPassword;

    /**
     * 账号或者物理卡号 (8字节)
     * BIN码，不足8位补0
     */
    @ProtocolField(order = 5, length = 8, dataType = ProtocolDataType.HEX, description = "账号或者物理卡号")
    private String accountOrCardNumber;

    /**
     * 输入密码 (16字节)
     * BIN码，对用户输入的密码进行16位MD5加密，采用小写上传
     */
    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.ASCII, description = "输入密码")
    private String inputPassword;

    /**
     * VIN码 (17字节)
     * ASCII码，启动方式为vin码启动充电时上送，其他方式置零，VIN码需要反序上送
     */
    @ProtocolField(order = 7, length = 17, dataType = ProtocolDataType.ASCII, description = "VIN码")
    private String vin;
}
