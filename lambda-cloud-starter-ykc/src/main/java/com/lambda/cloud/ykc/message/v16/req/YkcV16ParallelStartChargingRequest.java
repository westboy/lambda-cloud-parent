package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 12.1 充电桩主动申请并充充电（上行）
 * 对应协议帧类型 0xA1
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0xA1", name = "充电桩主动申请并充充电", description = "多枪并充时每个枪都分别上送并充启动申请", version = "1.6")
public class YkcV16ParallelStartChargingRequest {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 枪号 (1字节) BCD码 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.BCD, description = "枪号")
    private Integer connectorId;

    /** 启动方式 (1字节) BIN码：0x01刷卡；0x02帐号（暂不支持）；0x03 VIN码 */
    @ProtocolField(order = 3, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "启动方式")
    private Integer startMethod;

    /** 是否需要密码 (1字节) BIN码：0x00不需要；0x01需要 */
    @ProtocolField(order = 4, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "是否需要密码")
    private Integer passwordRequired;

    /** 账号或者物理卡号 (8字节) BIN码，不足8位补0 */
    @ProtocolField(order = 5, length = 8, computed = true, dataType = ProtocolDataType.HEX, description = "账号或物理卡号")
    private String accountOrCardNumber;

    /** 输入密码 (16字节) BIN码，用户输入密码进行16位MD5小写上传 */
    @ProtocolField(order = 6, length = 16, computed = true, dataType = ProtocolDataType.HEX, description = "输入密码")
    private String password;

    /** VIN码 (17字节) ASCII，VIN需要反序上送（VIN启动方式时上送，其他置零） */
    @ProtocolField(order = 7, length = 17, computed = true, dataType = ProtocolDataType.ASCII, description = "VIN码(反序)")
    private String vinCode;

    /** 主辅枪标记 (1字节) BIN码：0x00主枪；0x01辅枪 */
    @ProtocolField(order = 8, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "主辅枪标记")
    private Integer mainOrSubGunFlag;

    /** 并充序号 (6字节) BCD码，年月日时分秒（多个枪并充时一致） */
    @ProtocolField(
            order = 9,
            length = 6,
            computed = true,
            dataType = ProtocolDataType.BCD,
            description = "并充序号(年月日时分秒)")
    private String parallelSequence;
}
