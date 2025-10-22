package com.lambda.cloud.netty.protocol.accessor;

import org.objectweb.asm.*;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import static org.objectweb.asm.Opcodes.*;

/**
 * 字段访问器生成器
 * <p>
 * 使用ASM字节码生成技术动态生成高性能的字段访问器，替代反射操作
 * </p>
 *
 * @author Jin
 */
public class FieldAccessorGenerator {
    
    private static final String FIELD_ACCESSOR_SUFFIX = "$FieldAccessor";
    private static final AtomicLong COUNTER = new AtomicLong(0);
    private static final ConcurrentHashMap<String, FieldAccessor> ACCESSOR_CACHE = new ConcurrentHashMap<>();
    
    /**
     * 为指定字段生成访问器
     *
     * @param field 目标字段
     * @return 字段访问器
     */
    public static FieldAccessor generateAccessor(Field field) {
        String cacheKey = field.getDeclaringClass().getName() + "#" + field.getName();
        return ACCESSOR_CACHE.computeIfAbsent(cacheKey, k -> createAccessor(field));
    }
    
    /**
     * 创建字段访问器
     *
     * @param field 目标字段
     * @return 字段访问器
     */
    private static FieldAccessor createAccessor(Field field) {
        String className = generateClassName(field);
        byte[] classBytes = generateAccessorClass(field, className);
        
        // 使用自定义类加载器加载生成的类
        AccessorClassLoader classLoader = new AccessorClassLoader();
        Class<?> accessorClass = classLoader.defineClass(className, classBytes);
        
        try {
            return (FieldAccessor) accessorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create field accessor for: " + field, e);
        }
    }
    
    /**
     * 生成访问器类名
     *
     * @param field 目标字段
     * @return 类名
     */
    private static String generateClassName(Field field) {
        return field.getDeclaringClass().getName() + "$" + field.getName() + 
               FIELD_ACCESSOR_SUFFIX + COUNTER.incrementAndGet();
    }
    
    /**
     * 生成访问器类的字节码
     *
     * @param field     目标字段
     * @param className 生成的类名
     * @return 字节码数组
     */
    private static byte[] generateAccessorClass(Field field, String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String internalClassName = className.replace('.', '/');
        String fieldAccessorType = Type.getInternalName(FieldAccessor.class);
        
        // 定义类
        cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, internalClassName, null, 
                "java/lang/Object", new String[]{fieldAccessorType});
        
        // 生成构造函数
        generateConstructor(cw);
        
        // 生成setValue方法
        generateSetValueMethod(cw, field, internalClassName);
        
        // 生成getValue方法
        generateGetValueMethod(cw, field, internalClassName);
        
        // 生成getFieldName方法
        generateGetFieldNameMethod(cw, field);
        
        // 生成getFieldType方法
        generateGetFieldTypeMethod(cw, field);
        
        cw.visitEnd();
        return cw.toByteArray();
    }
    
    /**
     * 生成构造函数
     */
    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    /**
     * 生成setValue方法
     */
    private static void generateSetValueMethod(ClassWriter cw, Field field, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "setValue", 
                "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();
        
        // 类型转换
        String ownerType = Type.getInternalName(field.getDeclaringClass());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ownerType);
        
        // 加载值并进行类型转换
        mv.visitVarInsn(ALOAD, 2);
        generateValueCast(mv, field.getType());
        
        // 设置字段值
        mv.visitFieldInsn(PUTFIELD, ownerType, field.getName(), 
                Type.getDescriptor(field.getType()));
        
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();
    }
    
    /**
     * 生成getValue方法
     */
    private static void generateGetValueMethod(ClassWriter cw, Field field, String className) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getValue", 
                "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();
        
        // 类型转换
        String ownerType = Type.getInternalName(field.getDeclaringClass());
        mv.visitVarInsn(ALOAD, 1);
        mv.visitTypeInsn(CHECKCAST, ownerType);
        
        // 获取字段值
        mv.visitFieldInsn(GETFIELD, ownerType, field.getName(), 
                Type.getDescriptor(field.getType()));
        
        // 装箱基本类型
        generateBoxing(mv, field.getType());
        
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 2);
        mv.visitEnd();
    }
    
    /**
     * 生成getFieldName方法
     */
    private static void generateGetFieldNameMethod(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getFieldName", 
                "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(field.getName());
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    /**
     * 生成getFieldType方法
     */
    private static void generateGetFieldTypeMethod(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getFieldType", 
                "()Ljava/lang/Class;", null, null);
        mv.visitCode();
        
        // 加载Class对象
        if (field.getType().isPrimitive()) {
            // 基本类型的Class对象
            String wrapperType = getWrapperType(field.getType());
            mv.visitFieldInsn(GETSTATIC, wrapperType, "TYPE", "Ljava/lang/Class;");
        } else {
            // 引用类型的Class对象
            mv.visitLdcInsn(Type.getType(field.getType()));
        }
        
        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }
    
    /**
     * 生成值类型转换代码
     */
    private static void generateValueCast(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            // 基本类型需要拆箱
            String wrapperType = getWrapperType(fieldType);
            mv.visitTypeInsn(CHECKCAST, wrapperType);
            
            String unboxMethod = getUnboxMethod(fieldType);
            mv.visitMethodInsn(INVOKEVIRTUAL, wrapperType, unboxMethod, 
                    "()" + Type.getDescriptor(fieldType), false);
        } else {
            // 引用类型直接转换
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(fieldType));
        }
    }
    
    /**
     * 生成装箱代码
     */
    private static void generateBoxing(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            String wrapperType = getWrapperType(fieldType);
            mv.visitMethodInsn(INVOKESTATIC, wrapperType, "valueOf", 
                    "(" + Type.getDescriptor(fieldType) + ")L" + wrapperType + ";", false);
        }
        // 引用类型不需要装箱
    }
    
    /**
     * 获取基本类型对应的包装类型
     */
    private static String getWrapperType(Class<?> primitiveType) {
        if (primitiveType == int.class) return "java/lang/Integer";
        if (primitiveType == long.class) return "java/lang/Long";
        if (primitiveType == double.class) return "java/lang/Double";
        if (primitiveType == float.class) return "java/lang/Float";
        if (primitiveType == boolean.class) return "java/lang/Boolean";
        if (primitiveType == byte.class) return "java/lang/Byte";
        if (primitiveType == short.class) return "java/lang/Short";
        if (primitiveType == char.class) return "java/lang/Character";
        throw new IllegalArgumentException("Unsupported primitive type: " + primitiveType);
    }
    
    /**
     * 获取拆箱方法名
     */
    private static String getUnboxMethod(Class<?> primitiveType) {
        if (primitiveType == int.class) return "intValue";
        if (primitiveType == long.class) return "longValue";
        if (primitiveType == double.class) return "doubleValue";
        if (primitiveType == float.class) return "floatValue";
        if (primitiveType == boolean.class) return "booleanValue";
        if (primitiveType == byte.class) return "byteValue";
        if (primitiveType == short.class) return "shortValue";
        if (primitiveType == char.class) return "charValue";
        throw new IllegalArgumentException("Unsupported primitive type: " + primitiveType);
    }
    
    /**
     * 自定义类加载器
     */
    private static class AccessorClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}