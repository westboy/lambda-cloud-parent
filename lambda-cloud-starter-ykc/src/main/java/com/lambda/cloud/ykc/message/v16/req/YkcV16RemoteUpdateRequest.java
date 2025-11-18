package com.lambda.cloud.ykc.message.v16.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充1.6协议 11.3 远程更新（下行）
 * 对应协议帧类型 0x94
 */
@ToString
@Getter
@Setter
@ProtocolPayload(frameType = "0x94", name = "远程更新", description = "平台通过FTP下发更新文件信息供桩下载升级", version = "1.6")
public class YkcV16RemoteUpdateRequest {

    /** 桩编号 (7字节) BCD码 */
    @ProtocolField(order = 1, length = 7, computed = true, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /** 桩型号 (1字节) BIN：0x01直流；0x02交流 */
    @ProtocolField(order = 2, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "桩型号")
    private Integer equipmentModel;

    /** 桩功率 (2字节) BIN，不足2位补零 */
    @ProtocolField(
            order = 3,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "桩功率")
    private Integer equipmentPower;

    /** 升级服务器地址 (16字节) ASCII，不足补零 */
    @ProtocolField(order = 4, length = 16, computed = true, dataType = ProtocolDataType.ASCII, description = "升级服务器地址")
    private String serverAddress;

    /** 升级服务器端口 (2字节) BIN，不足补零 */
    @ProtocolField(
            order = 5,
            length = 2,
            computed = true,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "服务器端口")
    private Integer serverPort;

    /** 用户名 (16字节) ASCII，不足补零 */
    @ProtocolField(order = 6, length = 16, computed = true, dataType = ProtocolDataType.ASCII, description = "用户名")
    private String username;

    /** 密码 (16字节) ASCII，不足补零 */
    @ProtocolField(order = 7, length = 16, computed = true, dataType = ProtocolDataType.ASCII, description = "密码")
    private String password;

    /** 文件路径 (32字节) ASCII，不足补零 */
    @ProtocolField(order = 8, length = 32, computed = true, dataType = ProtocolDataType.ASCII, description = "文件路径")
    private String filePath;

    /** 执行控制 (1字节) BIN：0x01立即执行；0x02空闲执行 */
    @ProtocolField(order = 9, length = 1, computed = true, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executeControl;

    /** 下载超时时间 (1字节) BIN，单位min */
    @ProtocolField(
            order = 10,
            length = 1,
            computed = true,
            dataType = ProtocolDataType.UINT8,
            description = "下载超时时间(min)")
    private Integer downloadTimeoutMin;
}
