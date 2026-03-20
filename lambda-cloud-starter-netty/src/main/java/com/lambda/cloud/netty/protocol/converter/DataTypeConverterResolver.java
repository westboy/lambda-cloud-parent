package com.lambda.cloud.netty.protocol.converter;

import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;
import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.converter.impl.*;
import com.lambda.cloud.netty.protocol.encrypt.EncryptionService;
import java.util.EnumMap;
import java.util.Map;
import lombok.Setter;

/**
 * 数据类型转换器工厂
 * <p>
 * 管理和提供不同数据类型的转换器实例
 * </p>
 *
 * @author Jin
 */
@Setter
public class DataTypeConverterResolver {

    /**
     * 转换器映射
     */
    private final Map<ProtocolDataType, DataTypeConverter> converters;

    /**
     * 加密服务（可选）
     */
    private EncryptionService encryptionService;

    /**
     * 构造函数
     */
    public DataTypeConverterResolver() {
        this.converters = new EnumMap<>(ProtocolDataType.class);
        initializeConverters();
    }

    /**
     * 构造函数（支持加密）
     *
     * @param encryptionService 加密服务
     */
    public DataTypeConverterResolver(EncryptionService encryptionService) {
        this.converters = new EnumMap<>(ProtocolDataType.class);
        this.encryptionService = encryptionService;
        initializeConverters();
    }

    /**
     * 获取转换器
     *
     * @param dataType 数据类型
     * @return 转换器
     * @throws IllegalArgumentException 不支持的数据类型
     */
    public DataTypeConverter getConverter(ProtocolDataType dataType) {
        DataTypeConverter converter = converters.get(dataType);
        if (converter == null) {
            throw new IllegalArgumentException("不支持的数据类型: " + dataType);
        }
        return converter;
    }

    /**
     * 获取转换器（支持加密字段）
     *
     * @param fieldMetadata 字段元数据
     * @return 转换器
     * @throws IllegalArgumentException 不支持的数据类型
     */
    public DataTypeConverter getConverter(ProtocolFieldMetadata fieldMetadata) {
        DataTypeConverter baseConverter = getConverter(fieldMetadata.getDataType());

        // 如果字段需要加密且有加密服务，返回加密转换器
        if (fieldMetadata.isEncryptedField() && encryptionService != null) {
            return new EncryptedFieldConverter(encryptionService, baseConverter);
        }

        return baseConverter;
    }

    /**
     * 注册转换器
     *
     * @param dataType  数据类型
     * @param converter 转换器
     */
    public void registerConverter(ProtocolDataType dataType, DataTypeConverter converter) {
        converters.put(dataType, converter);
    }

    /**
     * 初始化转换器
     */
    private void initializeConverters() {
        registerConverter(ProtocolDataType.HEX, new HexConverter());
        registerConverter(ProtocolDataType.ASCII, new AsciiConverter());
        registerConverter(ProtocolDataType.BCD, new BcdConverter());
        registerConverter(ProtocolDataType.BIT, new BitConverter());
        registerConverter(ProtocolDataType.BIT, new BmsCurrentConverter(0.1,-400));
        registerConverter(ProtocolDataType.UINT8, new UInt8Converter());
        registerConverter(ProtocolDataType.UINT16, new UInt16Converter());
        registerConverter(ProtocolDataType.UINT32, new UInt32Converter());
        registerConverter(ProtocolDataType.UINT64, new UInt64Converter());
        registerConverter(ProtocolDataType.CP56TIME2A, new CP56Time2aConverter());
        registerConverter(ProtocolDataType.LIST, new ListConverter(this));
    }
}
