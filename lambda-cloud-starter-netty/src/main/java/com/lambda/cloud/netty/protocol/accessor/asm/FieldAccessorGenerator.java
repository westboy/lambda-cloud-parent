package com.lambda.cloud.netty.protocol.accessor.asm;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.objectweb.asm.*;

/**
 * 高性能 Unsafe 字段访问器生成器
 *
 * <p>支持 JDK 8–21，无需 --add-opens，动态生成访问器类与 Lookup 在同一包。</p>
 */
public final class FieldAccessorGenerator {

    // 常量定义
    private static final String FIELD_ACCESSOR_SUFFIX = "$FieldAccessor";
    private static final String UNSAFE_FIELD_NAME = "UNSAFE";
    private static final String OFFSET_FIELD_NAME = "OFFSET";
    private static final String THE_UNSAFE_FIELD = "theUnsafe";

    // 核心组件
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final ConcurrentHashMap<String, FieldAccessor> CACHE = new ConcurrentHashMap<>();
    private static final AccessorClassLoader LOADER = new AccessorClassLoader();

    // Unsafe 相关
    private static final UnsafeInfo UNSAFE_INFO = initializeUnsafe();


    /**
     * 初始化 Unsafe
     */
    private static UnsafeInfo initializeUnsafe() {
        // 尝试 JDK 9+ 的 jdk.internal.misc.Unsafe
        try {
            String className = "jdk.internal.misc.Unsafe";
            Class<?> unsafeClass = Class.forName(className);
            validateUnsafeAccess(unsafeClass);
            return new UnsafeInfo(className, unsafeClass);
        } catch (Exception e) {
            // 回退到 JDK 8 的 sun.misc.Unsafe
            try {
                String className = "sun.misc.Unsafe";
                Class<?> unsafeClass = Class.forName(className);
                validateUnsafeAccess(unsafeClass);
                return new UnsafeInfo(className, unsafeClass);
            } catch (Exception ex) {
                throw new IllegalStateException("无法访问 Unsafe 类，JDK 版本可能不支持", ex);
            }
        }
    }

    /**
     * 验证 Unsafe 访问
     */
    private static void validateUnsafeAccess(Class<?> unsafeClass) throws Exception {
        Field theUnsafeField = unsafeClass.getDeclaredField(THE_UNSAFE_FIELD);
        theUnsafeField.setAccessible(true);
        Object unsafeInstance = theUnsafeField.get(null);
        if (unsafeInstance == null) {
            throw new IllegalStateException("Unsafe 实例为 null");
        }
    }

    private FieldAccessorGenerator() {}

    /** 生成或获取字段访问器 */
    public static FieldAccessor generateAccessor(Field field) {
        if (field == null) {
            throw new IllegalArgumentException("字段不能为 null");
        }

        String cacheKey = buildCacheKey(field);
        return CACHE.computeIfAbsent(cacheKey, k -> createAccessor(field));
    }

    /**
     * 构建缓存键
     */
    private static String buildCacheKey(Field field) {
        return field.getDeclaringClass().getName() + "#" + field.getName();
    }

    /** 创建访问器 */
    private static FieldAccessor createAccessor(Field field) {
        try {
            String className = generateClassName(field);
            byte[] bytecode = generateClass(field, className);
            Class<?> accessorClass = LOADER.defineClass(className, bytecode);
            return (FieldAccessor) accessorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("生成字段访问器失败: " + field, e);
        }
    }

    /**
     * 生成类名
     */
    private static String generateClassName(Field field) {
        String packageName = FieldAccessorGenerator.class.getPackageName();
        return packageName + "." + field.getName() + FIELD_ACCESSOR_SUFFIX + COUNTER.incrementAndGet();
    }

    /** 生成访问器字节码 */
    private static byte[] generateClass(Field field, String className) {
        String internalClassName = className.replace('.', '/');
        String interfaceName = Type.getInternalName(FieldAccessor.class);

        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
        cw.visit(
                Opcodes.V1_8,
                Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL,
                internalClassName,
                null,
                "java/lang/Object",
                new String[] {interfaceName});

        generateFields(cw);
        generateStaticInitializer(cw, field, internalClassName);
        generateConstructor(cw);
        generateAccessorMethods(cw, field, internalClassName);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /**
     * 生成字段定义
     */
    private static void generateFields(ClassWriter cw) {
        cw.visitField(
                        Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                        UNSAFE_FIELD_NAME,
                        "L" + UNSAFE_INFO.internalName + ";",
                        null,
                        null)
                .visitEnd();

        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL, OFFSET_FIELD_NAME, "J", null, null)
                .visitEnd();
    }

    /**
     * 生成静态初始化器
     */
    private static void generateStaticInitializer(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();

        // 初始化 Unsafe 实例
        generateUnsafeInitialization(mv, internalClassName);

        // 初始化字段偏移量
        generateOffsetInitialization(mv, field, internalClassName);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();
    }

    /**
     * 生成 Unsafe 初始化代码
     */
    private static void generateUnsafeInitialization(MethodVisitor mv, String internalClassName) {
        mv.visitLdcInsn(Type.getType(UNSAFE_INFO.clazz));
        mv.visitLdcInsn(THE_UNSAFE_FIELD);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Field", "setAccessible", "(Z)V", false);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/reflect/Field",
                "get",
                "(Ljava/lang/Object;)Ljava/lang/Object;",
                false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, UNSAFE_INFO.internalName);
        mv.visitFieldInsn(
                Opcodes.PUTSTATIC, internalClassName, UNSAFE_FIELD_NAME, "L" + UNSAFE_INFO.internalName + ";");
    }

    /**
     * 生成偏移量初始化代码
     */
    private static void generateOffsetInitialization(MethodVisitor mv, Field field, String internalClassName) {
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, internalClassName, UNSAFE_FIELD_NAME, "L" + UNSAFE_INFO.internalName + ";");
        mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
        mv.visitLdcInsn(field.getName());
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL,
                "java/lang/Class",
                "getDeclaredField",
                "(Ljava/lang/String;)Ljava/lang/reflect/Field;",
                false);

        String offsetMethod = Modifier.isStatic(field.getModifiers()) ? "staticFieldOffset" : "objectFieldOffset";
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, UNSAFE_INFO.internalName, offsetMethod, "(Ljava/lang/reflect/Field;)J", false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, internalClassName, OFFSET_FIELD_NAME, "J");
    }

    /**
     * 生成构造函数
     */
    private static void generateConstructor(ClassWriter cw) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitCode();
        mv.visitVarInsn(Opcodes.ALOAD, 0);
        mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /**
     * 生成访问器方法
     */
    private static void generateAccessorMethods(ClassWriter cw, Field field, String internalClassName) {
        generateGetValue(cw, field, internalClassName);
        generateSetValue(cw, field, internalClassName);
        generateGetFieldName(cw, field);
        generateGetFieldType(cw, field);
    }

    /** getValue 方法 */
    private static void generateGetValue(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv =
                cw.visitMethod(Opcodes.ACC_PUBLIC, "getValue", "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        FieldTypeHandler handler = FieldTypeHandler.forType(field.getType());

        // 加载 Unsafe 实例
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, internalClassName, UNSAFE_FIELD_NAME, "L" + UNSAFE_INFO.internalName + ";");

        // 加载对象或静态字段基址
        loadFieldBase(mv, field);

        // 加载偏移量
        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClassName, OFFSET_FIELD_NAME, "J");

        // 调用对应的 getXXX 方法
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, UNSAFE_INFO.internalName, handler.getMethod, handler.getDescriptor, false);

        // 装箱基本类型
        handler.generateBoxing(mv);

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 2);
        mv.visitEnd();
    }

    /** setValue 方法 */
    private static void generateSetValue(ClassWriter cw, Field field, String internalClassName) {
        MethodVisitor mv =
                cw.visitMethod(Opcodes.ACC_PUBLIC, "setValue", "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();

        FieldTypeHandler handler = FieldTypeHandler.forType(field.getType());

        // 加载 Unsafe 实例
        mv.visitFieldInsn(
                Opcodes.GETSTATIC, internalClassName, UNSAFE_FIELD_NAME, "L" + UNSAFE_INFO.internalName + ";");

        // 加载对象或静态字段基址
        loadFieldBase(mv, field);

        // 加载偏移量
        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClassName, OFFSET_FIELD_NAME, "J");

        // 加载值并拆箱
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        handler.generateUnboxing(mv);

        // 调用对应的 putXXX 方法
        mv.visitMethodInsn(
                Opcodes.INVOKEVIRTUAL, UNSAFE_INFO.internalName, handler.putMethod, handler.putDescriptor, false);

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(5, 3);
        mv.visitEnd();
    }

    /**
     * 加载字段基址（对象实例或静态字段基址）
     */
    private static void loadFieldBase(MethodVisitor mv, Field field) {
        if (Modifier.isStatic(field.getModifiers())) {
            mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            mv.visitMethodInsn(
                    Opcodes.INVOKEVIRTUAL,
                    UNSAFE_INFO.internalName,
                    "staticFieldBase",
                    "(Ljava/lang/Class;)Ljava/lang/Object;",
                    false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
        }
    }

    /** getFieldName */
    private static void generateGetFieldName(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldName", "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(field.getName());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /** getFieldType */
    private static void generateGetFieldType(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldType", "()Ljava/lang/Class;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(Type.getType(field.getType()));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /** 优化的 ClassLoader */
    private static class AccessorClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] bytes) {
            try {
                // JDK 9+ 使用 Lookup.defineClass，确保包级访问权限
                return MethodHandles.lookup().defineClass(bytes);
            } catch (IllegalAccessException e) {
                // JDK 8 回退方案
                return super.defineClass(name, bytes, 0, bytes.length);
            }
        }
    }
}
