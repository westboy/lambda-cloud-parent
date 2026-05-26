package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * DL/T 645-2007 读当前组合有功总电能应答报文。
 *
 * <p>控制码 {@code 0x91}，DI {@code 00010000}。
 * 表计返回当前组合有功总电能值，数据域包含 DI（4 字节）和电能值（4 字节 BCD）。</p>
 *
 * <p>电能值解码规则：{@code XXXXXX.XX} kWh（2 位小数精度，低字节在前）。</p>
 */
@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "91:00010000", name = "读当前组合有功总电能应答")
public class T645ReadEnergyResponse implements T645DataBody {

    /** 读当前组合有功总电能 DI，固定为 {@code 00010000}。 */
    public static final String DI = "00010000";

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    /**
     * 当前组合有功总电能值。
     *
     * <p>4 字节 BCD 编码，低字节在前，2 位小数精度。
     * 例如原始字节 {@code 01 02 03 04} 解码后为 {@code 40302.01} kWh。</p>
     */
    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.BCD,
            computed = true,
            littleEndian = true,
            precision = 2,
            description = "当前组合有功总电能")
    private BigDecimal totalEnergy;

    @Override
    public String getDi() {
        return di;
    }
}
