package com.lambda.cloud.ykc.message.v20.req;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolFrame;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 云快充2.0协议 - 充电桩登录认证请求详细信息
 * <p>
 * 帧类型码：0x01
 * 对应协议文档：6.1 充电桩登录认证
 * </p>
 *
 * @author zx
 */
@Getter
@Setter
@ToString
@ProtocolFrame(frameType = "01", name = "充电桩登录认证请求", description = "充电桩登录认证请求详细信息")
public class YkcV20LoginRequestDetail {

    /**
     * 随机密钥 (88字节)
     * 16位随机密钥（BIN码）并用RSA公钥加密，参照11.3章节说明
     */
    @ProtocolField(order = 1, length = 88, dataType = ProtocolDataType.ASCII, description = "随机密钥")
    private String randomKey;

    /**
     * 桩编码 (7字节)
     * 不足7位补0
     */
    @ProtocolField(order = 2, length = 7, dataType = ProtocolDataType.BCD, description = "桩编码")
    private String stationCode;

    /**
     * 桩类型 (1字节)
     * 0表示直流桩，1表示交流桩
     */
    @ProtocolField(order = 3, length = 1, dataType = ProtocolDataType.UINT8, description = "桩类型")
    private Integer stationType;

    /**
     * 充电枪数量 (1字节)
     */
    @ProtocolField(order = 4, length = 1, dataType = ProtocolDataType.UINT8, description = "充电枪数量")
    private Integer connectorCount;

    /**
     * 通信协议版本 (3字节)
     * 如果协议版本号为1.0.11，则为0x01，0x00,0x0B
     */
    @ProtocolField(order = 5, length = 3, dataType = ProtocolDataType.UINT32, description = "通信协议版本")
    private byte[] protocolVersion;

    /**
     * 程序版本 (8字节)
     * 不足8位补零
     */
    @ProtocolField(order = 6, length = 8, dataType = ProtocolDataType.ASCII, description = "程序版本")
    private String programVersion;

    /**
     * 网络链接类型 (1字节)
     * 0x00 SIM卡
     * 0x01 LAN
     * 0x02 WAN
     * 0x03 其他
     */
    @ProtocolField(order = 7, length = 1, dataType = ProtocolDataType.UINT8, description = "网络链接类型")
    private Integer networkType;

    /**
     * Sim卡 (10字节)
     * 不足10位补零，取不到置零
     */
    @ProtocolField(order = 8, length = 10, dataType = ProtocolDataType.BCD, description = "Sim卡")
    private String simCard;

    /**
     * 运营商 (1字节)
     * 0x00 移动
     * 0x02 电信
     * 0x03 联通
     * 0x04 其他
     */
    @ProtocolField(order = 9, length = 1, dataType = ProtocolDataType.UINT8, description = "运营商")
    private Integer operator;

    /**
     * Token (7字节)
     * 取不到置零
     */
    @ProtocolField(order = 10, length = 7, dataType = ProtocolDataType.BCD, description = "Token")
    private String token;

    /**
     * 手机号码 (11字节)
     * 不足11位补零，取不到置零
     */
    @ProtocolField(order = 11, length = 11, dataType = ProtocolDataType.ASCII, description = "手机号码")
    private String phoneNumber;

    /**
     * 支持网络制式 (1字节)
     * Bit位表示（0否1是），低位到高位顺序
     * Bit1：支持2G；
     * Bit2：支持3G；
     * Bit3：支持4G；
     * Bit4：支持5G；
     * 示例：
     * 0x06：只支持3G、4G
     * 0x0F：同时支持2、3、4、5G
     */
    @ProtocolField(order = 12, length = 1, dataType = ProtocolDataType.UINT8, description = "支持网络制式")
    private Integer supportedNetworkStandards;

    /**
     * 当前网络制式 (1字节)
     * Bit位表示（0否1是），低位到高位顺序
     * Bit1：当前使用2G；
     * Bit2：当前使用3G；
     * Bit3：当前使用4G；
     * Bit4：当前使用5G；
     * 示例：
     * 0x04：当前使用4G
     * 0x08：当前使用5G
     */
    @ProtocolField(order = 13, length = 1, dataType = ProtocolDataType.UINT8, description = "当前网络制式")
    private Integer currentNetworkStandard;

    /**
     * 经度 (4字节)
     * 偏移量：180；精确到小数点后6位，取不到置零。
     * 示例：东经90°(+90.000000)，上报十进制为270000000，对应16进制为0x1017DF80
     */
    @ProtocolField(order = 14, length = 4, dataType = ProtocolDataType.UINT32, description = "经度")
    private Integer longitude;

    /**
     * 纬度 (4字节)
     * 偏移量：90；精确到小数点后6位，取不到置零。
     * 示例：南纬45°(-45.000000)，上报十进制为45000000，对应16进制为0x02AEA540
     */
    @ProtocolField(order = 15, length = 4, dataType = ProtocolDataType.UINT32, description = "纬度")
    private Integer latitude;
}
