package com.lambda.cloud.netty.utils;

import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.ProtocolPayloadMetadata;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@Slf4j
public class EncryptionUtils {

    /**
     * 判断是否启用加密控制
     */
    public static boolean isEncryptionEnabled(Object instance, ProtocolPayloadMetadata msgMetadata) {
        try {
            for (ProtocolFieldMetadata fieldMetadata : msgMetadata.fields()) {
                if (fieldMetadata.isEncryptionKey()) {
                    Object value = fieldMetadata.getValue(instance);
                    return isEnableEncryption(value);
                }
            }
            return false;
        } catch (Exception e) {
            log.debug("判断加密控制失败，视为未启用: {}", e.getMessage());
            return false;
        }
    }

    /**
     * 判断控制值是否为启用状态
     * <p/>
     * 仅当存在加密控制字段且其值为 0x01（数值 1）时启用
     */
    public static boolean isEnableEncryption(Object value) {
        return switch (value) {
            case Number num -> num.intValue() == 1;
            case String num -> StrUtil.equals(num, "01");
            case null, default -> false;
        };
    }
}
