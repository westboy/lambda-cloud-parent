package com.lambda.cloud.netty.protocol.accessor.asm;

import static org.objectweb.asm.Opcodes.*;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import java.lang.reflect.Field;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

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
        // 确保字段可访问
        field.setAccessible(true);

        String className = generateClassName(field);
        byte[] classBytes = generateAccessorClass(field, className);

        // 使用自定义类加载器加载生成的类
        AccessorClassLoader classLoader = new AccessorClassLoader();
        Class<?> accessorClass = classLoader.defineClass(className, classBytes);

        try {
            return (FieldAccessor)
                    accessorClass.getDeclaredConstructor(Field.class).newInstance(field);
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
        return field.getDeclaringClass().getName() + "$" + field.getName() + FIELD_ACCESSOR_SUFFIX
                + COUNTER.incrementAndGet();
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
        cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, internalClassName, null, "java/lang/Object", new String[] {
            fieldAccessorType
        });

        // 添加Field字段来存储目标字段
        cw.visitField(ACC_PRIVATE | ACC_FINAL, "field", "Ljava/lang/reflect/Field;", null, null)
                .visitEnd();

        // 生成构造函数
        generateConstructor(cw, internalClassName);

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
     *
     * @param cw ClassWriter
     * @param internalClassName 内部类名
     */
    private static void generateConstructor(ClassWriter cw, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/lang/reflect/Field;)V", null, null);
        mv.visitCode();

        // 调用父类构造函数
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        // 存储Field参数到实例字段
        mv.visitVarInsn(ALOAD, 0);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitFieldInsn(PUTFIELD, internalClassName, "field", "Ljava/lang/reflect/Field;");

        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * 生成setValue方法
     */
    private static void generateSetValueMethod(ClassWriter cw, Field field, String className) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        // 加载this.field
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, className.replace('.', '/'), "field", "Ljava/lang/reflect/Field;");

        // 加载目标对象
        mv.visitVarInsn(ALOAD, 1);

        // 加载值
        mv.visitVarInsn(ALOAD, 2);

        // 调用Field.set(Object obj, Object value)
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/reflect/Field", "set", "(Ljava/lang/Object;Ljava/lang/Object;)V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    /**
     * 生成getValue方法
     *
     * @param cw                ClassWriter
     * @param field             目标字段
     * @param internalClassName 内部类名
     */
    private static void generateGetValueMethod(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "getValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        // 加载this.field
        mv.visitVarInsn(ALOAD, 0);
        mv.visitFieldInsn(GETFIELD, internalClassName, "field", "Ljava/lang/reflect/Field;");

        // 加载目标对象
        mv.visitVarInsn(ALOAD, 1);

        // 调用Field.get(Object obj)
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * 生成getFieldName方法
     */
    private static void generateGetFieldNameMethod(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getFieldName", "()Ljava/lang/String;", null, null);
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
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "getFieldType", "()Ljava/lang/Class;", null, null);
        mv.visitCode();

        // 直接加载字段类型的Class对象
        mv.visitLdcInsn(Type.getType(field.getType()));

        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
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
