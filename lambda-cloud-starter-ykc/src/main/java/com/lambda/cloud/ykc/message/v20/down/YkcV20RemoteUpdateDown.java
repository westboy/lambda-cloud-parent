package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 远程更新
 * <p>
 * 帧类型码：0x94
 * 对应协议文档：11.3 远程更新
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：对桩进行软件升级，平台升级模式为ftp文件升级
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "94", name = "远程更新", description = "远程更新命令")
public class YkcV20RemoteUpdateDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 桩型号 (1字节)
     * 0x01：直流
     * 0x02：交流
     */
    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "桩型号")
    private Integer chargerModel;

    /**
     * 桩功率 (2字节)
     * BIN码，不足2位补零
     */
    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "桩功率")
    private Integer chargerPower;

    /**
     * 升级服务器地址 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 4, length = 16, dataType = ProtocolDataType.ASCII, description = "升级服务器地址")
    private String serverAddress;

    /**
     * 升级服务器端口 (2字节)
     * BIN码，不足2位补零
     */
    @ProtocolField(order = 5, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "升级服务器端口")
    private Integer serverPort;

    /**
     * 用户名 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.ASCII, description = "用户名")
    private String username;

    /**
     * 密码 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 7, length = 16, dataType = ProtocolDataType.ASCII, description = "密码")
    private String password;

    /**
     * 文件路径 (32字节)
     * ASCII码，不足32位补零，文件路径名由平台定义
     */
    @ProtocolField(order = 8, length = 32, dataType = ProtocolDataType.ASCII, description = "文件路径")
    private String filePath;

    /**
     * 文件名称 (32字节)
     * ASCII码，不足32位补零，文件名由平台定义
     */
    @ProtocolField(order = 9, length = 32, dataType = ProtocolDataType.ASCII, description = "文件名称")
    private String fileName;

    /**
     * 执行控制 (1字节)
     * 0x01：立即执行
     * 0x02：空闲执行
     */
    @ProtocolField(order = 10, length = 1, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executionControl;

    /**
     * 下载超时时间 (1字节)
     * BIN码，单位：min
     */
    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "下载超时时间")
    private Integer downloadTimeout;

    /**
     * 文件MD5签名 (32字节)
     * ASCII码，升级文件的MD5校验码
     */
    @ProtocolField(order = 12, length = 32, dataType = ProtocolDataType.ASCII, description = "文件MD5签名")
    private String fileMd5;
}