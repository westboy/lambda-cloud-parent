package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.down.YkcV20BillingModelDown;
import com.lambda.cloud.ykc.message.v20.model.YkcV20BillingModelFee;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20BillingModelDownTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_shouldProduceValidBytes() throws Exception {
        ProtocolPayloadRegistry.register("0A", YkcV20BillingModelDown.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20BillingModelDown resp = new YkcV20BillingModelDown();
        resp.setEquipmentId("15031412782305");
        resp.setBillingModelNumber("0100");
        resp.setStartTime("2025-01-01 12:00:00");
        resp.setFeeCount(2);
        resp.setServiceFeeCount(1);
        resp.setParkingFeeCount(1);
        resp.setStatusChangeReason(0);

        List<YkcV20BillingModelFee> fees = new ArrayList<>();
        YkcV20BillingModelFee peakFee = new YkcV20BillingModelFee();
        peakFee.setElectricityRate(BigDecimal.valueOf(1.50000));
        peakFee.setServiceRate(BigDecimal.valueOf(0.80000));
        fees.add(peakFee);
        YkcV20BillingModelFee valleyFee = new YkcV20BillingModelFee();
        valleyFee.setElectricityRate(BigDecimal.valueOf(0.50000));
        valleyFee.setServiceRate(BigDecimal.valueOf(0.30000));
        fees.add(valleyFee);
        resp.setFee(fees);

        List<YkcV20BillingModelFee> serviceFees = new ArrayList<>();
        YkcV20BillingModelFee serviceFee = new YkcV20BillingModelFee();
        serviceFee.setElectricityRate(BigDecimal.valueOf(0.20000));
        serviceFee.setServiceRate(BigDecimal.valueOf(0.10000));
        serviceFees.add(serviceFee);
        resp.setServiceFee(serviceFees);

        List<YkcV20BillingModelFee> parkingFees = new ArrayList<>();
        YkcV20BillingModelFee parkingFee = new YkcV20BillingModelFee();
        parkingFee.setElectricityRate(BigDecimal.valueOf(0));
        parkingFee.setServiceRate(BigDecimal.valueOf(5.00000));
        parkingFees.add(parkingFee);
        resp.setParkingFee(parkingFees);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("0A");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);
        assertTrue(raw.length > 0);
        assertEquals(0x68, raw[0] & 0xFF);
    }
}
