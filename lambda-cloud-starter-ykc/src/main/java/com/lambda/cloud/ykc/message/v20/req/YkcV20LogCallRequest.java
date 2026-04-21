package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "98", name = "日志召唤", description = "远程召唤桩本地日志上传")
public class YkcV20LogCallRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 16, dataType = ProtocolDataType.ASCII, description = "上传服务器地址")
    private String uploadServerAddress;

    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "上传服务器端口")
    private Integer uploadServerPort;

    @ProtocolField(order = 4, length = 16, dataType = ProtocolDataType.ASCII, description = "用户名")
    private String username;

    @ProtocolField(order = 5, length = 16, dataType = ProtocolDataType.ASCII, description = "密码")
    private String password;

    @ProtocolField(order = 6, length = 64, dataType = ProtocolDataType.ASCII, description = "上传服务器路径")
    private String uploadServerPath;

    @ProtocolField(order = 7, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "日志文件开始时间")
    private LocalDateTime startTime;

    @ProtocolField(order = 8, length = 7, dataType = ProtocolDataType.CP56TIME2A, description = "日志文件结束时间")
    private LocalDateTime endTime;

    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "日志类型")
    private Integer logType;

    @ProtocolField(order = 10, length = 64, dataType = ProtocolDataType.ASCII, description = "桩日志文件路径")
    private String localLogPath;

    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "上传超时时间")
    private Integer uploadTimeoutMinutes;
}

