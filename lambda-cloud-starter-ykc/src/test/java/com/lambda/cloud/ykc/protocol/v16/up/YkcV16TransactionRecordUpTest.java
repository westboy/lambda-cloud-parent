package com.lambda.cloud.ykc.protocol.v16.up;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

import com.lambda.cloud.netty.protocol.engine.ProtocolEngine;
import com.lambda.cloud.netty.protocol.engine.ProtocolEngineFactory;
import com.lambda.cloud.netty.protocol.engine.impl.ReflectionProtocolEngine;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import com.lambda.cloud.ykc.message.v16.YkcV16BasePayload;
import com.lambda.cloud.ykc.message.v16.up.YkcV16TransactionRecordUp;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.math.BigDecimal;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class YkcV16TransactionRecordUpTest {
    @BeforeEach
    public void setUp() {
        ProtocolEngineFactory.addEngine(
                ProtocolEngineFactory.EngineType.REFLECTION, new ReflectionProtocolEngine(null));
    }

    @Test
    public void serialize_thenParse_shouldRoundTrip() throws Exception {
        ProtocolPayloadRegistry.register("3B", YkcV16TransactionRecordUp.class);
        ProtocolEngine<YkcV16BasePayload> engine =
                ProtocolEngineFactory.getEngine(ProtocolEngineFactory.EngineType.REFLECTION);

        byte[] t = new byte[] {(byte) 0x98, (byte) 0xB7, 0x0E, 0x11, 0x10, 0x03, 0x14};

        YkcV16TransactionRecordUp req = new YkcV16TransactionRecordUp();
        req.setTransactionId("55031412782305012018061910262392");
        req.setEquipmentId("55031412782305");
        req.setConnectorId(1);
        req.setStartTime(t);
        req.setEndTime(t);
        req.setPeakPrice(new BigDecimal("1.30000"));
        req.setPeakEnergy(new BigDecimal("0"));
        req.setPeakEnergyWithLoss(new BigDecimal("0"));
        req.setPeakAmount(new BigDecimal("0"));
        req.setHighPrice(new BigDecimal("1.30000"));
        req.setHighEnergy(new BigDecimal("0"));
        req.setHighEnergyWithLoss(new BigDecimal("0"));
        req.setHighAmount(new BigDecimal("0"));
        req.setNormalPrice(new BigDecimal("1.30000"));
        req.setNormalEnergy(new BigDecimal("0"));
        req.setNormalEnergyWithLoss(new BigDecimal("0"));
        req.setNormalAmount(new BigDecimal("0"));
        req.setValleyPrice(new BigDecimal("1.30000"));
        req.setValleyEnergy(new BigDecimal("0"));
        req.setValleyEnergyWithLoss(new BigDecimal("0"));
        req.setValleyAmount(new BigDecimal("0"));
        req.setMeterStartValue("0000000000");
        req.setMeterEndValue("0000000000");
        req.setTotalEnergy(new BigDecimal("0"));
        req.setTotalEnergyWithLoss(new BigDecimal("0"));
        req.setTotalAmount(new BigDecimal("0"));
        req.setVinCode("");
        req.setTransactionType(2);
        req.setTransactionDateTime(t);
        req.setStopReason(0);
        req.setPhysicalCardNumber("00000000d14b0a54");

        YkcV16BasePayload base = new YkcV16BasePayload();
        base.setStartFlag("68");
        base.setEncryptFlag("00");
        base.setFrameType("3B");
        base.setSerialNumber(384);
        base.setDetail(req);

        ByteBuf out = Unpooled.buffer();
        engine.serialize(base, out);
        byte[] raw = new byte[out.readableBytes()];
        out.readBytes(raw);

        ByteBuf in = Unpooled.wrappedBuffer(raw);
        YkcV16BasePayload parsed = engine.parse(in, YkcV16BasePayload.class);
        assertEquals("3B", parsed.getFrameType());
        assertEquals(384, parsed.getSerialNumber());

        YkcV16TransactionRecordUp detail = assertInstanceOf(YkcV16TransactionRecordUp.class, parsed.getDetail());
        assertEquals("55031412782305012018061910262392", detail.getTransactionId());
        assertEquals("55031412782305", detail.getEquipmentId());
        assertEquals(1, detail.getConnectorId());

        ByteBuf out2 = Unpooled.buffer();
        engine.serialize(parsed, out2);
        byte[] raw2 = new byte[out2.readableBytes()];
        out2.readBytes(raw2);
        assertArrayEquals(raw, raw2);
    }
}
