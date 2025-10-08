package com.lambda.cloud.netty.protocol.packet;

import io.netty.buffer.ByteBuf;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;

@Getter
@Setter
@Slf4j
@EqualsAndHashCode(callSuper = true)
public class BitBox extends BaseUnit {

    private List<Bit> bits = new ArrayList<>();

    private int count = 0;

    public BitBox() {
        super(0);
    }

    public void add(Bit bit) {
        bit.setSite(count);
        count += bit.getLength();
        this.bits.add(bit);
        super.setLength(this.getLength());
    }

    @Override
    public void read(ByteBuf byteBuf) {
        super.read(byteBuf);

        String data = new BigInteger(super.getFullHexData(), 16).toString(2);
        data = StringUtils.leftPad(data, this.getLength() * 8, "0");

        for (Bit bit : bits) {
            bit.setData(Integer.parseInt(data.substring(bit.getSite(), bit.getSite() + bit.getLength()), 2));
        }
    }

    public void setData(String binData, int start) {
        StringBuilder data = new StringBuilder(
                StringUtils.leftPad(new BigInteger(super.getFullHexData(), 16).toString(2), this.getLength() * 8, "0"));
        data.replace(start, start + binData.length(), binData);
        super.setData(new BigInteger(data.toString(), 2).toString(16));
    }

    @Override
    public int getLength() {
        int byteMaxBit = 8;
        if (this.count % byteMaxBit == 0) {
            return this.count / 8;
        } else {
            return this.count / 8 + 1;
        }
    }

    @Override
    public String getPrintData() {
        return null;
    }
}
