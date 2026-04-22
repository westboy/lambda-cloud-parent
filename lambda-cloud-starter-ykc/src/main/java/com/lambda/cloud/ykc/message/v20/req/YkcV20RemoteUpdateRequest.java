package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "94", name = "远程更新", description = "运营平台下发软件升级信息到充电桩")
public class YkcV20RemoteUpdateRequest {

    @ProtocolField(order = 1, length = 7, dataType = ProtocolDataType.BCD, description = "桩编号")
    private String equipmentId;

    @ProtocolField(order = 2, length = 1, dataType = ProtocolDataType.UINT8, description = "桩型号")
    private Integer stationType;

    @ProtocolField(order = 3, length = 2, dataType = ProtocolDataType.UINT16, littleEndian = true, description = "桩功率")
    private Integer stationPower;

    @ProtocolField(order = 4, length = 16, dataType = ProtocolDataType.ASCII, description = "升级服务器地址")
    private String upgradeServerAddress;

    @ProtocolField(
            order = 5,
            length = 2,
            dataType = ProtocolDataType.UINT16,
            littleEndian = true,
            description = "升级服务器端口")
    private Integer upgradeServerPort;

    @ProtocolField(order = 6, length = 16, dataType = ProtocolDataType.ASCII, description = "用户名")
    private String username;

    @ProtocolField(order = 7, length = 16, dataType = ProtocolDataType.ASCII, description = "密码")
    private String password;

    @ProtocolField(order = 8, length = 32, dataType = ProtocolDataType.ASCII, description = "文件路径")
    private String filePath;

    @ProtocolField(order = 9, length = 32, dataType = ProtocolDataType.ASCII, description = "文件名称")
    private String fileName;

    @ProtocolField(order = 10, length = 1, dataType = ProtocolDataType.UINT8, description = "执行控制")
    private Integer executeControl;

    @ProtocolField(order = 11, length = 1, dataType = ProtocolDataType.UINT8, description = "下载超时时间")
    private Integer downloadTimeoutMinutes;

    @ProtocolField(order = 12, length = 32, dataType = ProtocolDataType.ASCII, description = "文件MD5签名")
    private String fileMd5;
}
