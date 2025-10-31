package com.lambda.cloud.netty.protocol.annotation;

import com.lambda.cloud.netty.protocol.ProtocolFieldMetadata;

import java.lang.annotation.Annotation;

/**
 * ProtocolField代理类，用于创建元素元数据
 *
 * @author zx
 */
@SuppressWarnings("all")
public record ProtocolFieldProxy(ProtocolFieldMetadata listMetadata) implements ProtocolField {
    @Override
    public int order() {
        return listMetadata.getOrder();
    }

    @Override
    public boolean composite() {
        return listMetadata.isComposite();
    }

    @Override
    public boolean encryptedKey() {
        return false;
    }

    @Override
    public boolean encryptedField() {
        return listMetadata.isEncryptedField();
    }

    @Override
    public boolean computed() {
        return listMetadata.isComputed();
    }

    @Override
    public boolean crcFiled() {
        return false;
    }

    @Override
    public boolean lengthFiled() {
        return listMetadata.isLengthFiled();
    }

    @Override
    public boolean serialFiled() {
        return false;
    }

    @Override
    public int length() {
        return listMetadata.getLength();
    }

    @Override
    public ProtocolDataType dataType() {
        return listMetadata.getListElementDataType();
    }

    @Override
    public int precision() {
        return listMetadata.getPrecision();
    }

    @Override
    public boolean littleEndian() {
        return listMetadata.isLittleEndian();
    }

    @Override
    public String description() {
        return "List元素: " + listMetadata.getDescription();
    }

    @Override
    public boolean optional() {
        return false;
    }

    @Override
    public String defaultValue() {
        return "";
    }

    @Override
    public String charset() {
        return listMetadata.getCharset();
    }

    @Override
    public PaddingDirection padding() {
        return listMetadata.getPaddingDirection();
    }

    @Override
    public String paddingChar() {
        return listMetadata.getPaddingChar();
    }

    @Override
    public ProtocolDataType listElementType() {
        return ProtocolDataType.HEX;
    }

    @Override
    public int listLength() {
        return listMetadata.getListLength();
    }

    @Override
    public int listElementSize() {
        return listMetadata.getListElementSize();
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        return ProtocolField.class;
    }
}