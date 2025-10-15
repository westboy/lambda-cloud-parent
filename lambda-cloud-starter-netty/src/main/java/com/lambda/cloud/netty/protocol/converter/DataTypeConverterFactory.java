package com.lambda.cloud.netty.protocol.converter;

import com.lambda.cloud.netty.protocol.annotation.ProtocolDataType;
import com.lambda.cloud.netty.protocol.converter.impl.*;
import java.util.EnumMap;
import java.util.Map;

/**
 * 数据类型转换器工厂
 * <p>
 * 管理和提供不同数据类型的转换器实例
 * </p>
 *
 * @author Jin
 */
public class DataTypeConverterFactory {

    /**
     * 转换器映射
     */
    private final Map<ProtocolDataType, DataTypeConverter> converters;

    /**
     * 构造函数
     */
    public DataTypeConverterFactory() {
        this.converters = new EnumMap<>(ProtocolDataType.class);
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
        converters.put(ProtocolDataType.HEX, new HexConverter());
        converters.put(ProtocolDataType.ASCII, new AsciiConverter());
        converters.put(ProtocolDataType.BCD, new BcdConverter());
        converters.put(ProtocolDataType.BIT, new BitConverter());
        converters.put(ProtocolDataType.UINT8, new UInt8Converter());
        converters.put(ProtocolDataType.UINT16, new UInt16Converter());
        converters.put(ProtocolDataType.UINT32, new UInt32Converter());
        converters.put(ProtocolDataType.CP56TIME2A, new CP56Time2aConverter());
    }
}
