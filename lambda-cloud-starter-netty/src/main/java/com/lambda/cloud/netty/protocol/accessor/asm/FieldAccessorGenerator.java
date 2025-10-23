package com.lambda.cloud.netty.protocol.accessor.asm;

import static org.objectweb.asm.Opcodes.*;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * 字段访问器生成器
 * <p>
 * 使用ASM字节码生成技术动态生成高性能的字段访问器，替代反射操作
 * 优化版本：生成真正的字节码访问器，避免反射API调用
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

        // 使用目标类的类加载器加载生成的类，确保访问权限
        ClassLoader targetClassLoader = field.getDeclaringClass().getClassLoader();
        if (targetClassLoader == null) {
            targetClassLoader = ClassLoader.getSystemClassLoader();
        }

        Class<?> accessorClass = defineClass(targetClassLoader, className, classBytes);

        try {
            return (FieldAccessor) accessorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to create field accessor for: " + field, e);
        }
    }

    /**
     * 使用目标类加载器定义类
     */
    private static Class<?> defineClass(ClassLoader classLoader, String className, byte[] classBytes) {
        try {
            // 使用反射调用ClassLoader的defineClass方法
            Method defineClassMethod = ClassLoader.class.getDeclaredMethod(
                    "defineClass", String.class, byte[].class, int.class, int.class);
            defineClassMethod.setAccessible(true);
            return (Class<?>) defineClassMethod.invoke(classLoader, className, classBytes, 0, classBytes.length);
        } catch (Exception e) {
            // 如果失败，回退到自定义类加载器
            AccessorClassLoader fallbackLoader = new AccessorClassLoader(classLoader);
            return fallbackLoader.defineClass(className, classBytes);
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

        // 生成无参构造函数
        generateOptimizedConstructor(cw, internalClassName);

        // 生成优化的setValue方法
        generateOptimizedSetValueMethod(cw, field, internalClassName);

        // 生成优化的getValue方法
        generateOptimizedGetValueMethod(cw, field, internalClassName);

        // 生成getFieldName方法
        generateGetFieldNameMethod(cw, field);

        // 生成getFieldType方法
        generateGetFieldTypeMethod(cw, field);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * 生成优化的无参构造函数
     *
     * @param cw ClassWriter
     * @param internalClassName 内部类名
     */
    private static void generateOptimizedConstructor(ClassWriter cw, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();

        // 调用父类构造函数
        mv.visitVarInsn(ALOAD, 0);
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);

        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 生成优化的setValue方法 - 使用直接字节码访问
     */
    private static void generateOptimizedSetValueMethod(ClassWriter cw, Field field, String className) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        Class<?> declaringClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String ownerType = Type.getInternalName(declaringClass);
        String fieldName = field.getName();
        boolean isStatic = Modifier.isStatic(field.getModifiers());

        if (!isStatic) {
            // 加载目标对象并转换类型
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerType);
        }

        // 加载值并转换为正确类型
        mv.visitVarInsn(ALOAD, 2);
        generateUnboxing(mv, fieldType);

        // 使用PUTFIELD或PUTSTATIC直接设置字段值
        if (isStatic) {
            mv.visitFieldInsn(PUTSTATIC, ownerType, fieldName, Type.getDescriptor(fieldType));
        } else {
            mv.visitFieldInsn(PUTFIELD, ownerType, fieldName, Type.getDescriptor(fieldType));
        }

        mv.visitInsn(RETURN);
        mv.visitMaxs(3, 3);
        mv.visitEnd();
    }

    /**
     * 生成优化的getValue方法 - 使用直接字节码访问
     */
    private static void generateOptimizedGetValueMethod(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv =
                cw.visitMethod(ACC_PUBLIC, "getValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, new String[] {
                    "java/lang/IllegalAccessException"
                });
        mv.visitCode();

        Class<?> declaringClass = field.getDeclaringClass();
        Class<?> fieldType = field.getType();
        String ownerType = Type.getInternalName(declaringClass);
        String fieldName = field.getName();
        boolean isStatic = Modifier.isStatic(field.getModifiers());

        if (!isStatic) {
            // 加载目标对象并转换类型
            mv.visitVarInsn(ALOAD, 1);
            mv.visitTypeInsn(CHECKCAST, ownerType);
        }

        // 使用GETFIELD或GETSTATIC直接获取字段值
        if (isStatic) {
            mv.visitFieldInsn(GETSTATIC, ownerType, fieldName, Type.getDescriptor(fieldType));
        } else {
            mv.visitFieldInsn(GETFIELD, ownerType, fieldName, Type.getDescriptor(fieldType));
        }

        // 装箱基本类型
        generateBoxing(mv, fieldType);

        mv.visitInsn(ARETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();
    }

    /**
     * 生成拆箱代码
     */
    private static void generateUnboxing(MethodVisitor mv, Class<?> type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Integer");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false);
            } else if (type == long.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Long");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false);
            } else if (type == double.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Double");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false);
            } else if (type == float.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Float");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false);
            } else if (type == boolean.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Boolean");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
            } else if (type == byte.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Byte");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false);
            } else if (type == char.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Character");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false);
            } else if (type == short.class) {
                mv.visitTypeInsn(CHECKCAST, "java/lang/Short");
                mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false);
            }
        } else {
            mv.visitTypeInsn(CHECKCAST, Type.getInternalName(type));
        }
    }

    /**
     * 生成装箱代码
     */
    private static void generateBoxing(MethodVisitor mv, Class<?> type) {
        if (type.isPrimitive()) {
            if (type == int.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
            } else if (type == long.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
            } else if (type == double.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
            } else if (type == float.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
            } else if (type == boolean.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
            } else if (type == byte.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
            } else if (type == char.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
            } else if (type == short.class) {
                mv.visitMethodInsn(INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
            }
        }
        // 对象类型不需要装箱
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

        public AccessorClassLoader() {
            super();
        }

        public AccessorClassLoader(ClassLoader parent) {
            super(parent);
        }

        public Class<?> defineClass(String name, byte[] bytes) {
            return defineClass(name, bytes, 0, bytes.length);
        }
    }
}
