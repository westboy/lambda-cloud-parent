package com.lambda.cloud.netty.protocol.message;

import com.lambda.cloud.netty.protocol.packet.Bit;
import com.lambda.cloud.netty.protocol.packet.BitBox;
import com.lambda.cloud.netty.protocol.packet.Unit;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MessageSegment implements Unit {
    private final List<Unit> units = new ArrayList<>();

    public void add(Unit unit) {
        this.units.add(unit);
    }

    public void clearUnits() {
        units.clear();
    }

    public void setBit(Bit bit) {
        ((BitBox) this.units.getLast()).add(bit);
    }

    @Override
    public void read(ByteBuf byteBuf) {
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            if (byteBuf.readableBytes() == 0 || byteBuf.readableBytes() < unit.getLength()) {
                return;
            }
            unit.read(byteBuf);
        }
    }

    @Override
    public void write(ByteBuf byteBuf) {
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            unit.write(byteBuf);
        }
    }

    @Override
    public int getLength() {
        int length = 0;
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            length += unit.getLength();
        }
        return length;
    }

    @Override
    public String getPrintData() {
        StringBuilder sb = new StringBuilder();
        for (Unit unit : this.units) {
            if (unit == null) {
                continue;
            }
            sb.append(unit.getPrintData());
        }
        return sb.toString();
    }
}
