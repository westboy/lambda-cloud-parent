package com.lambda.cloud.ykc.protocol.v20.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20HalfHourEnergy;
import com.lambda.cloud.ykc.message.v20.model.YkcV20TransactionRatePeriod;
import com.lambda.cloud.ykc.message.v20.up.YkcV20TransactionRecordUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20TransactionRecordUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("3D", YkcV20TransactionRecordUp.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20TransactionRecordUp resp = new YkcV20TransactionRecordUp();
        resp.setTransactionSerialNumber("55031412782305012018061910262392");
        resp.setEquipmentId("15031412782305");
        resp.setConnectorId("01");
        resp.setStartTime(LocalDateTime.of(2025, 1, 1, 12, 0, 0));
        resp.setEndTime(LocalDateTime.of(2025, 1, 1, 13, 0, 0));
        resp.setMeterNo("000000");
        resp.setMeterCipher("000000000000000000000000000000000000");
        resp.setMeterProtocolVersion("0000");
        resp.setEncryptMethod(0);
        resp.setMeterStartValue("0000000000");
        resp.setMeterEndValue("0000000000");
        resp.setTotalEnergy(10000);
        resp.setTotalEnergyWithLoss(10000);
        resp.setTotalAmount(5000);
        resp.setVin("00000000000000000");
        resp.setTransactionType(1);
        resp.setTransactionDateTime(LocalDateTime.of(2025, 1, 1, 13, 0, 0));
        resp.setStopReason(1);
        resp.setPhysicalCardNumber("0000000000000000");
        resp.setRatePeriodCount(1);

        List<YkcV20TransactionRatePeriod> ratePeriods = new ArrayList<>();
        YkcV20TransactionRatePeriod period = new YkcV20TransactionRatePeriod();
        period.setUnitPrice(100000);
        period.setEnergy(10000);
        period.setEnergyWithLoss(10000);
        period.setAmount(5000);
        ratePeriods.add(period);
        resp.setRatePeriods(ratePeriods);

        List<YkcV20HalfHourEnergy> halfHourEnergies = new ArrayList<>();
        for (int i = 0; i < 48; i++) {
            YkcV20HalfHourEnergy energy = new YkcV20HalfHourEnergy();
            energy.setEnergy(0);
            halfHourEnergies.add(energy);
        }
        resp.setHalfHourEnergies(halfHourEnergies);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("3D");
        base.setSerialNumber(1);
        base.setDetail(resp);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("3D", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20TransactionRecordUp detail = assertInstanceOf(YkcV20TransactionRecordUp.class, parsed.getDetail());
        assertEquals("55031412782305012018061910262392", detail.getTransactionSerialNumber());
        assertEquals("15031412782305", detail.getEquipmentId());
        assertEquals("1", detail.getConnectorId());
        assertEquals(1, detail.getRatePeriodCount());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
