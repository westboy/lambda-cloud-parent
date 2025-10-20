package com.lambda.cloud.netty.protocol.encryption;

import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.metadata.ProtocolFrameMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import static cn.hutool.core.util.ReflectUtil.getFieldValue;

@Data
@Slf4j
public class EncryptionHelper {

    /**
     * 判断是否启用加密控制
     * 仅当存在加密控制字段且其值为 0x01（数值 1）时启用
     */
    public boolean isEncryptionEnabled(Object instance, ProtocolFrameMetadata msgMetadata) {
        try {
            for (ProtocolFieldMetadata meta : msgMetadata.fields()) {
                if (meta.isEncryptionKey()) {
                    Object value = getFieldValue(instance, meta.field());
                    return isValueEnableEncryption(value);
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("判断加密控制失败，视为未启用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断控制值是否为启用状态（0x01）
     */
    private boolean isValueEnableEncryption(Object value) {
        return switch (value) {
            case Number num -> num.intValue() == 0;
            case String num -> StrUtil.equals(num, "00");
            case null, default -> false;
        };
    }

}
