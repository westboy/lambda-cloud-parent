package com.lambda.cloud.ykc.protocol.v20.down;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import cn.hutool.core.util.HexUtil;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v20.YkcV20BasePayload;
import com.lambda.cloud.ykc.message.v20.model.YkcV20HalfHourEnergy;
import com.lambda.cloud.ykc.message.v20.model.YkcV20TransactionRatePeriod;
import com.lambda.cloud.ykc.message.v20.up.YkcV20TransactionRecordRequest;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV20TransactionRecordRequestTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("3D", YkcV20TransactionRecordRequest.class);
        ProtocolEngine<YkcV20BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        YkcV20TransactionRecordRequest detail = new YkcV20TransactionRecordRequest();
        detail.setTransactionSerialNumber("1234567890123456789012345678901");
        detail.setEquipmentId("1234567890123");
        detail.setConnectorId("1");
        detail.setStartTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        detail.setEndTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        detail.setMeterNo("12345678901");
        detail.setMeterCipher("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef0123");
        detail.setMeterProtocolVersion("0123");
        detail.setEncryptMethod(1);
        detail.setMeterStartValue("0123456789");
        detail.setMeterEndValue("0123456789");
        detail.setTotalEnergy(40000);
        detail.setTotalEnergyWithLoss(40000);
        detail.setTotalAmount(40000);
        detail.setVin("A".repeat(17));
        detail.setTransactionType(1);
        detail.setTransactionDateTime(LocalDateTime.of(2026, 1, 2, 3, 4, 5));
        detail.setStopReason(1);
        detail.setPhysicalCardNumber("012345");
        detail.setRatePeriodCount(1);
        detail.setRatePeriodCount(3);
        List<YkcV20TransactionRatePeriod> ratePeriodsList = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            YkcV20TransactionRatePeriod item = new YkcV20TransactionRatePeriod();
            item.setUnitPrice(1);
            item.setEnergy(2);
            item.setEnergyWithLoss(3);
            item.setAmount(4);
            ratePeriodsList.add(item);
        }
        detail.setRatePeriods(ratePeriodsList);
        List<YkcV20HalfHourEnergy> halfHourEnergiesList = new ArrayList<>();
        for (int i = 0; i < 48; i++) {
            YkcV20HalfHourEnergy item = new YkcV20HalfHourEnergy();
            item.setEnergy(i + 1);
            halfHourEnergiesList.add(item);
        }
        detail.setHalfHourEnergies(halfHourEnergiesList);

        YkcV20BasePayload base = new YkcV20BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("3D");
        base.setSerialNumber(1);
        base.setDetail(detail);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV20BasePayload parsed = engine.parse(in, YkcV20BasePayload.class);

        assertEquals("3D", parsed.getFrameType());
        assertEquals(1, parsed.getSerialNumber());

        YkcV20TransactionRecordRequest parsedDetail =
                assertInstanceOf(YkcV20TransactionRecordRequest.class, parsed.getDetail());
        assertEquals("1234567890123456789012345678901", parsedDetail.getTransactionSerialNumber());
        assertEquals("1234567890123", parsedDetail.getEquipmentId());
        assertEquals("1", parsedDetail.getConnectorId());
        assertEquals(48, parsedDetail.getHalfHourEnergies().size());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
        String hexString = HexUtil.encodeHexStr(raw, false);
        System.out.println(hexString);
    }
}
