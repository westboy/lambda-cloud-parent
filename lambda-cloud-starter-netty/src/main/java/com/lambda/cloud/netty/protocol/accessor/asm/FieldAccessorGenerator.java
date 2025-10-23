package com.lambda.cloud.netty.protocol.accessor.asm;

import static org.objectweb.asm.Opcodes.*;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * 基于Unsafe的高性能字段访问器生成器
 * <p>
 * 使用sun.misc.Unsafe进行直接内存访问，完全绕过Java访问权限检查，
 * 提供比传统字节码访问更高的性能和更好的兼容性。
 * </p>
 *
 * @author Jin
 */
public class FieldAccessorGenerator {

    private static final String FIELD_ACCESSOR_SUFFIX = "$FieldAccessor";
    private static final AtomicLong COUNTER = new AtomicLong(0);

    /**
     * 为指定字段生成基于Unsafe的高性能访问器
     *
     * @param field 目标字段
     * @return 字段访问器实例
     * @throws RuntimeException 如果生成失败
     */
    public static FieldAccessor generateAccessor(Field field) {
        String className = generateClassName(field);
        byte[] classBytes = generateUnsafeAccessorClass(field, className);

        // 使用简单的类加载器，因为Unsafe访问不需要特殊的访问权限
        AccessorClassLoader classLoader = new AccessorClassLoader();
        Class<?> accessorClass = classLoader.defineClass(className, classBytes);

        try {
            return (FieldAccessor) accessorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create unsafe field accessor for: " + field, e);
        }
    }

    /**
     * 生成访问器类名
     */
    private static String generateClassName(Field field) {
        return field.getDeclaringClass().getName() + "$" + field.getName() + FIELD_ACCESSOR_SUFFIX
                + COUNTER.incrementAndGet();
    }

    /**
     * 生成基于Unsafe的访问器类字节码
     */
    private static byte[] generateUnsafeAccessorClass(Field field, String className) {
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        String internalClassName = className.replace('.', '/');
        String fieldAccessorType = Type.getInternalName(FieldAccessor.class);

        // 定义类
        cw.visit(V1_8, ACC_PUBLIC | ACC_FINAL, internalClassName, null, "java/lang/Object", new String[] {
            fieldAccessorType
        });

        // 添加静态字段：UNSAFE 和 FIELD_OFFSET
        generateUnsafeFields(cw, field);

        // 生成静态初始化块
        generateStaticInitializer(cw, field, internalClassName);

        // 生成构造函数
        generateConstructor(cw, internalClassName);

        // 生成基于Unsafe的setValue方法
        generateUnsafeSetValueMethod(cw, field);

        // 生成基于Unsafe的getValue方法
        generateUnsafeGetValueMethod(cw, field);

        // 生成getFieldName方法
        generateGetFieldNameMethod(cw, field);

        // 生成getFieldType方法
        generateGetFieldTypeMethod(cw, field);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * 添加Unsafe相关的静态字段
     */
    private static void generateUnsafeFields(ClassWriter cw, Field field) {
        // private static final Unsafe UNSAFE;
        cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "UNSAFE", "Lsun/misc/Unsafe;", null, null)
                .visitEnd();

        // private static final long FIELD_OFFSET;
        cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "FIELD_OFFSET", "J", null, null)
                .visitEnd();
    }

    /**
     * 生成静态初始化块
     */
    private static void generateStaticInitializer(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        // 获取Unsafe实例
        generateGetUnsafeCall(mv);
        mv.visitFieldInsn(PUTSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");

        // 获取字段偏移量
        mv.visitFieldInsn(GETSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");

        // 加载目标类的Class对象
        mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));

        // 加载字段名
        mv.visitLdcInsn(field.getName());

        // 调用Class.getDeclaredField
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false);

        // 调用Unsafe.objectFieldOffset
        if (Modifier.isStatic(field.getModifiers())) {
            mv.visitMethodInsn(
                    INVOKEVIRTUAL, "sun/misc/Unsafe", "staticFieldOffset", "(Ljava/lang/reflect/Field;)J", false);
        } else {
            mv.visitMethodInsn(
                    INVOKEVIRTUAL, "sun/misc/Unsafe", "objectFieldOffset", "(Ljava/lang/reflect/Field;)J", false);
        }

        mv.visitFieldInsn(PUTSTATIC, internalClassName, "FIELD_OFFSET", "J");

        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();
    }

    /**
     * 生成获取Unsafe实例的字节码
     */
    private static void generateGetUnsafeCall(MethodVisitor mv) {
        // 尝试通过反射获取Unsafe
        mv.visitLdcInsn(Type.getType("sun.misc.Unsafe"));
        mv.visitLdcInsn("theUnsafe");
        mv.visitMethodInsn(
                INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false);
        mv.visitInsn(DUP);
        mv.visitInsn(ICONST_1);
        mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
        mv.visitInsn(ACONST_NULL);
        mv.visitMethodInsn(
                INVOKEVIRTUAL, "java/lang/reflect/Field", "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(CHECKCAST, "sun/misc/Unsafe");
    }

    /**
     * 生成构造函数
     */
    private static void generateConstructor(ClassWriter cw, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 生成基于Unsafe的setValue方法
     */
    private static void generateUnsafeSetValueMethod(ClassWriter cw, Field field) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        Class<?> fieldType = field.getType();
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        String internalClassName = field.getDeclaringClass().getName().replace('.', '/') + "$" + field.getName()
                + FIELD_ACCESSOR_SUFFIX + "1";

        // 加载Unsafe实例
        mv.visitFieldInsn(GETSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");

        if (isStatic) {
            // 对于静态字段，加载静态字段的基地址
            mv.visitFieldInsn(GETSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");
            mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "sun/misc/Unsafe",
                    "staticFieldBase",
                    "(Ljava/lang/Class;)Ljava/lang/Object;",
                    false);
        } else {
            // 对于实例字段，加载目标对象
            mv.visitVarInsn(ALOAD, 1);
        }

        // 加载字段偏移量
        mv.visitFieldInsn(GETSTATIC, internalClassName, "FIELD_OFFSET", "J");

        // 加载值并转换类型
        mv.visitVarInsn(ALOAD, 2);
        generateUnsafeUnboxing(mv, fieldType);

        // 调用相应的Unsafe.putXXX方法
        generateUnsafePutCall(mv, fieldType);

        mv.visitInsn(RETURN);
        mv.visitMaxs(5, 3);
        mv.visitEnd();
    }

    /**
     * 生成基于Unsafe的getValue方法
     */
    private static void generateUnsafeGetValueMethod(ClassWriter cw, Field field) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "getValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        Class<?> fieldType = field.getType();
        boolean isStatic = Modifier.isStatic(field.getModifiers());
        String internalClassName = field.getDeclaringClass().getName().replace('.', '/') + "$" + field.getName()
                + FIELD_ACCESSOR_SUFFIX + "1";

        // 加载Unsafe实例
        mv.visitFieldInsn(GETSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");

        if (isStatic) {
            // 对于静态字段，加载静态字段的基地址
            mv.visitFieldInsn(GETSTATIC, internalClassName, "UNSAFE", "Lsun/misc/Unsafe;");
            mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            mv.visitMethodInsn(
                    INVOKEVIRTUAL,
                    "sun/misc/Unsafe",
                    "staticFieldBase",
                    "(Ljava/lang/Class;)Ljava/lang/Object;",
                    false);
        } else {
            // 对于实例字段，加载目标对象
            mv.visitVarInsn(ALOAD, 1);
        }

        // 加载字段偏移量
        mv.visitFieldInsn(GETSTATIC, internalClassName, "FIELD_OFFSET", "J");

        // 调用相应的Unsafe.getXXX方法
        generateUnsafeGetCall(mv, fieldType);

        // 装箱基本类型
        generateUnsafeBoxing(mv, fieldType);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(4, 2);
        mv.visitEnd();
    }

    /**
     * 生成Unsafe拆箱代码
     */
    private static void generateUnsafeUnboxing(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            } else if (fieldType == byte.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
            } else if (fieldType == char.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            } else if (fieldType == short.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
            } else if (fieldType == int.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            } else if (fieldType == long.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            } else if (fieldType == float.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            } else if (fieldType == double.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            }
        } else {
            // 对象类型直接转换
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(fieldType));
        }
    }

    /**
     * 生成Unsafe装箱代码
     */
    private static void generateUnsafeBoxing(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType.isPrimitive()) {
            if (fieldType == boolean.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if (fieldType == byte.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            } else if (fieldType == char.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            } else if (fieldType == short.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            } else if (fieldType == int.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if (fieldType == long.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            } else if (fieldType == float.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            } else if (fieldType == double.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            }
        }
        // 对象类型不需要装箱
    }

    /**
     * 生成Unsafe的putXXX方法调用
     */
    private static void generateUnsafePutCall(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType == boolean.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putBoolean", "(Ljava/lang/Object;JZ)V", false);
        } else if (fieldType == byte.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putByte", "(Ljava/lang/Object;JB)V", false);
        } else if (fieldType == char.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putChar", "(Ljava/lang/Object;JC)V", false);
        } else if (fieldType == short.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putShort", "(Ljava/lang/Object;JS)V", false);
        } else if (fieldType == int.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putInt", "(Ljava/lang/Object;JI)V", false);
        } else if (fieldType == long.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putLong", "(Ljava/lang/Object;JJ)V", false);
        } else if (fieldType == float.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putFloat", "(Ljava/lang/Object;JF)V", false);
        } else if (fieldType == double.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "putDouble", "(Ljava/lang/Object;JD)V", false);
        } else {
            // 对象类型
            mv.visitMethodInsn(
                    INVOKEVIRTUAL, "sun/misc/Unsafe", "putObject", "(Ljava/lang/Object;JLjava/lang/Object;)V", false);
        }
    }

    /**
     * 生成Unsafe的getXXX方法调用
     */
    private static void generateUnsafeGetCall(MethodVisitor mv, Class<?> fieldType) {
        if (fieldType == boolean.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getBoolean", "(Ljava/lang/Object;J)Z", false);
        } else if (fieldType == byte.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getByte", "(Ljava/lang/Object;J)B", false);
        } else if (fieldType == char.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getChar", "(Ljava/lang/Object;J)C", false);
        } else if (fieldType == short.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getShort", "(Ljava/lang/Object;J)S", false);
        } else if (fieldType == int.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getInt", "(Ljava/lang/Object;J)I", false);
        } else if (fieldType == long.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getLong", "(Ljava/lang/Object;J)J", false);
        } else if (fieldType == float.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getFloat", "(Ljava/lang/Object;J)F", false);
        } else if (fieldType == double.class) {
            mv.visitMethodInsn(INVOKEVIRTUAL, "sun/misc/Unsafe", "getDouble", "(Ljava/lang/Object;J)D", false);
        } else {
            // 对象类型
            mv.visitMethodInsn(
                    INVOKEVIRTUAL, "sun/misc/Unsafe", "getObject", "(Ljava/lang/Object;J)Ljava/lang/Object;", false);
        }
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

        mv.visitLdcInsn(Type.getType(field.getType()));

        mv.visitInsn(ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 简化的类加载器
     */
    private static class AccessorClassLoader extends ClassLoader {

        public AccessorClassLoader() {
            super();
        }

        public Class<?> defineClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
