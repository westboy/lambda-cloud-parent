package com.lambda.cloud.netty.protocol.accessor.asm;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 字段类型处理器
 */
public enum FieldTypeHandler {
    INT(
            int.class,
            "getInt",
            "putInt",
            "(Ljava/lang/Object;J)I",
            "(Ljava/lang/Object;JI)V",
            "java/lang/Integer",
            "valueOf",
            "(I)Ljava/lang/Integer;",
            "intValue",
            "()I"),
    LONG(
            long.class,
            "getLong",
            "putLong",
            "(Ljava/lang/Object;J)J",
            "(Ljava/lang/Object;JJ)V",
            "java/lang/Long",
            "valueOf",
            "(J)Ljava/lang/Long;",
            "longValue",
            "()J"),
    BOOLEAN(
            boolean.class,
            "getBoolean",
            "putBoolean",
            "(Ljava/lang/Object;J)Z",
            "(Ljava/lang/Object;JZ)V",
            "java/lang/Boolean",
            "valueOf",
            "(Z)Ljava/lang/Boolean;",
            "booleanValue",
            "()Z"),
    BYTE(
            byte.class,
            "getByte",
            "putByte",
            "(Ljava/lang/Object;J)B",
            "(Ljava/lang/Object;JB)V",
            "java/lang/Byte",
            "valueOf",
            "(B)Ljava/lang/Byte;",
            "byteValue",
            "()B"),
    CHAR(
            char.class,
            "getChar",
            "putChar",
            "(Ljava/lang/Object;J)C",
            "(Ljava/lang/Object;JC)V",
            "java/lang/Character",
            "valueOf",
            "(C)Ljava/lang/Character;",
            "charValue",
            "()C"),
    SHORT(
            short.class,
            "getShort",
            "putShort",
            "(Ljava/lang/Object;J)S",
            "(Ljava/lang/Object;JS)V",
            "java/lang/Short",
            "valueOf",
            "(S)Ljava/lang/Short;",
            "shortValue",
            "()S"),
    FLOAT(
            float.class,
            "getFloat",
            "putFloat",
            "(Ljava/lang/Object;J)F",
            "(Ljava/lang/Object;JF)V",
            "java/lang/Float",
            "valueOf",
            "(F)Ljava/lang/Float;",
            "floatValue",
            "()F"),
    DOUBLE(
            double.class,
            "getDouble",
            "putDouble",
            "(Ljava/lang/Object;J)D",
            "(Ljava/lang/Object;JD)V",
            "java/lang/Double",
            "valueOf",
            "(D)Ljava/lang/Double;",
            "doubleValue",
            "()D"),
    OBJECT(
            Object.class,
            "getObject",
            "putObject",
            "(Ljava/lang/Object;J)Ljava/lang/Object;",
            "(Ljava/lang/Object;JLjava/lang/Object;)V",
            null,
            null,
            null,
            null,
            null);

    final Class<?> type;
    final String getMethod;
    final String putMethod;
    final String getDescriptor;
    final String putDescriptor;
    final String wrapperClass;
    final String boxMethod;
    final String boxDescriptor;
    final String unboxMethod;
    final String unboxDescriptor;

    FieldTypeHandler(
            Class<?> type,
            String getMethod,
            String putMethod,
            String getDescriptor,
            String putDescriptor,
            String wrapperClass,
            String boxMethod,
            String boxDescriptor,
            String unboxMethod,
            String unboxDescriptor) {
        this.type = type;
        this.getMethod = getMethod;
        this.putMethod = putMethod;
        this.getDescriptor = getDescriptor;
        this.putDescriptor = putDescriptor;
        this.wrapperClass = wrapperClass;
        this.boxMethod = boxMethod;
        this.boxDescriptor = boxDescriptor;
        this.unboxMethod = unboxMethod;
        this.unboxDescriptor = unboxDescriptor;
    }

    static FieldTypeHandler forType(Class<?> type) {
        for (FieldTypeHandler handler : values()) {
            if (handler.type == type) {
                return handler;
            }
        }
        return OBJECT;
    }

    boolean isPrimitive() {
        return this != OBJECT;
    }

    void generateBoxing(MethodVisitor mv) {
        if (isPrimitive()) {
            mv.visitMethodInsn(Opcodes.INVOKESTATIC, wrapperClass, boxMethod, boxDescriptor, false);
        }
    }

    void generateUnboxing(MethodVisitor mv) {
        if (isPrimitive()) {
            mv.visitTypeInsn(Opcodes.CHECKCAST, wrapperClass);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, wrapperClass, unboxMethod, unboxDescriptor, false);
        }
    }
}