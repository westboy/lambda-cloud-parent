package com.lambda.cloud.ykc.message.v20.down;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 日志召唤
 * <p>
 * 帧类型码：0x98
 * 对应协议文档：11.7 日志召唤（可选）
 * 数据传输方向：运营平台 → 充电桩（下行）
 * 功能：远程对桩本地储存的日志进行召唤，辅助进行问题定位
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "98", name = "日志召唤", description = "日志召唤命令")
public class YkcV20LogCallDown {

    /**
     * 桩编号 (7字节)
     * BCD码
     */
    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    /**
     * 上传服务器地址 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 2, length = 16, dataType = ProtocolDataType.ASCII, description = "上传服务器地址")
    private String uploadServerAddress;

    /**
     * 上传服务器端口 (2字节)
     * BIN码，不足2位补零
     */
    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "上传服务器端口")
    private Integer uploadServerPort;

    /**
     * 用户名 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 4, length = 16, dataType = ProtocolDataType.ASCII, description = "用户名")
    private String username;

    /**
     * 密码 (16字节)
     * ASCII码，不足16位补零
     */
    @ProtocolField(order = 5, length = 16, dataType = ProtocolDataType.ASCII, description = "密码")
    private String password;

    /**
     * 上传服务器路径 (64字节)
     * ASCII码，不足64位补零，只需把文件上传到指定目录
     */
    @ProtocolField(order = 6, length = 64, dataType = ProtocolDataType.ASCII, description = "上传服务器路径")
    private String uploadServerPath;

    /**
     * 日志文件开始时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(order = 7, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "日志文件开始时间")
    private String logStartTime;

    /**
     * 日志文件结束时间 (7字节)
     * CP56Time2a格式
     */
    @ProtocolField(order = 8, length = 7, dataType = ProtocolDataType.CP56TIME2A, littleEndian = true, description = "日志文件结束时间")
    private String logEndTime;

    /**
     * 日志类型 (1字节)
     * 0x00：全部日志
     * 0x01：BMS日志
     * 0x02：数据库日志
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "日志类型")
    private Integer logType;

    /**
     * 桩日志文件路径 (64字节)
     * ASCII码，不足64位补零，不设置就全零
     */
    @ProtocolField(order = 10, length = 64, dataType = ProtocolDataType.ASCII, description = "桩日志文件路径")
    private String chargerLogFilePath;

    /**
     * 上传超时时间 (1字节)
     * BIN码，单位：min
     */
    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "上传超时时间")
    private Integer uploadTimeout;
}