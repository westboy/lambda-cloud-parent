package com.lambda.cloud.t645.message.read;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.annotation.ProtocolField;
import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.t645.message.T645DataBody;
import java.math.BigDecimal;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
@ProtocolPayload(frameType = "91:00010000", name = "读当前组合有功总电能应答")
public class T645ReadEnergyResponse implements T645DataBody {

    @ProtocolField(order = 0, length = 4, dataType = ProtocolDataType.HEX, computed = true, description = "数据标识DI")
    private String di;

    @ProtocolField(
            order = 1,
            length = 4,
            dataType = ProtocolDataType.BCD,
            computed = true,
            precision = 2,
            description = "当前组合有功总电能")
    private BigDecimal totalEnergy;

    @Override
    public String getDi() {
        return di;
    }
}
