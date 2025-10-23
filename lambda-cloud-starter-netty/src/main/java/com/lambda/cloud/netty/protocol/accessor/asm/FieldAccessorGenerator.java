package com.lambda.cloud.netty.protocol.accessor.asm;

import com.lambda.cloud.netty.protocol.accessor.FieldAccessor;
import org.objectweb.asm.*;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 高性能 Unsafe 字段访问器生成器
 *
 * <p>支持 JDK 8–21，无需 --add-opens，动态生成访问器类与 Lookup 在同一包。</p>
 */
public final class FieldAccessorGenerator {

    private static final String FIELD_ACCESSOR_SUFFIX = "$FieldAccessor";
    private static final AtomicLong COUNTER = new AtomicLong();
    private static final ConcurrentHashMap<String, FieldAccessor> CACHE = new ConcurrentHashMap<>();
    private static final AccessorClassLoader LOADER = new AccessorClassLoader();

    // Unsafe 相关
    private static final String UNSAFE_CLASS_NAME;
    private static final Class<?> UNSAFE_CLASS;

    static {
        Class<?> unsafeClass;
        String className;

        try {
            className = "jdk.internal.misc.Unsafe";
            unsafeClass = Class.forName(className);
            Field f = unsafeClass.getDeclaredField("theUnsafe");
            f.setAccessible(true);
        } catch (Throwable ignore) {
            try {
                className = "sun.misc.Unsafe";
                unsafeClass = Class.forName(className);
                Field f = unsafeClass.getDeclaredField("theUnsafe");
                f.setAccessible(true);
            } catch (Throwable e) {
                throw new IllegalStateException("Cannot access Unsafe", e);
            }
        }
        UNSAFE_CLASS_NAME = className.replace('.', '/');
        UNSAFE_CLASS = unsafeClass;
    }

    private FieldAccessorGenerator() {}

    /** 生成或获取字段访问器 */
    public static FieldAccessor generateAccessor(Field field) {
        if (field == null) throw new IllegalArgumentException("field == null");
        String key = field.getDeclaringClass().getName() + "#" + field.getName();
        return CACHE.computeIfAbsent(key, k -> createAccessor(field));
    }

    /** 创建访问器 */
    private static FieldAccessor createAccessor(Field field) {
        try {
            // 动态生成类与当前类在同一包
            String pkg = FieldAccessorGenerator.class.getPackageName();
            String className = pkg + "." + field.getName() + FIELD_ACCESSOR_SUFFIX + COUNTER.incrementAndGet();
            byte[] bytes = generateClass(field, className);
            Class<?> accessorClass = LOADER.defineClass(className, bytes);
            return (FieldAccessor) accessorClass.getDeclaredConstructor().newInstance();
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate accessor for " + field, e);
        }
    }

    /** 生成访问器字节码 */
    private static byte[] generateClass(Field field, String className) {
        String internalClass = className.replace('.', '/');
        String iface = Type.getInternalName(FieldAccessor.class);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);

        cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL, internalClass, null,
                "java/lang/Object", new String[]{iface});

        // static final Unsafe + OFFSET
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "UNSAFE", "L" + UNSAFE_CLASS_NAME + ";", null, null).visitEnd();
        cw.visitField(Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_FINAL,
                "OFFSET", "J", null, null).visitEnd();

        // <clinit>
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_STATIC, "<clinit>", "()V", null, null);
        mv.visitCode();
        // Unsafe 实例
        mv.visitLdcInsn(Type.getType(UNSAFE_CLASS));
        mv.visitLdcInsn("theUnsafe");
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
        mv.visitInsn(Opcodes.DUP);
        mv.visitInsn(Opcodes.ICONST_1);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Field",
                "setAccessible", "(Z)V", false);
        mv.visitInsn(Opcodes.ACONST_NULL);
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/reflect/Field",
                "get", "(Ljava/lang/Object;)Ljava/lang/Object;", false);
        mv.visitTypeInsn(Opcodes.CHECKCAST, UNSAFE_CLASS_NAME);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, internalClass, "UNSAFE",
                "L" + UNSAFE_CLASS_NAME + ";");

        // 字段偏移量
        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClass, "UNSAFE", "L" + UNSAFE_CLASS_NAME + ";");
        mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
        mv.visitLdcInsn(field.getName());
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
                "getDeclaredField", "(Ljava/lang/String;)Ljava/lang/reflect/Field;", false);
        String offsetMethod = Modifier.isStatic(field.getModifiers()) ? "staticFieldOffset" : "objectFieldOffset";
        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE_CLASS_NAME,
                offsetMethod, "(Ljava/lang/reflect/Field;)J", false);
        mv.visitFieldInsn(Opcodes.PUTSTATIC, internalClass, "OFFSET", "J");

        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(3, 0);
        mv.visitEnd();

        // 构造函数
        MethodVisitor init = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
        init.visitCode();
        init.visitVarInsn(Opcodes.ALOAD, 0);
        init.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
        init.visitInsn(Opcodes.RETURN);
        init.visitMaxs(1, 1);
        init.visitEnd();

        // getValue
        generateGetValue(cw, field, internalClass);
        // setValue
        generateSetValue(cw, field, internalClass);
        // getFieldName
        generateGetFieldName(cw, field);
        // getFieldType
        generateGetFieldType(cw, field);

        cw.visitEnd();
        return cw.toByteArray();
    }

    /** getValue 方法 */
    private static void generateGetValue(ClassWriter cw, Field field, String internalClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getValue",
                "(Ljava/lang/Object;)Ljava/lang/Object;", null, null);
        mv.visitCode();

        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClass, "UNSAFE",
                "L" + UNSAFE_CLASS_NAME + ";");
        if (Modifier.isStatic(field.getModifiers())) {
            mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE_CLASS_NAME,
                    "staticFieldBase", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
        }
        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClass, "OFFSET", "J");

        // 调用对应 getXXX
        String descriptor;
        String method;
        Class<?> type = field.getType();
        if (type == int.class) { method = "getInt"; descriptor = "(Ljava/lang/Object;J)I"; } 
        else if (type == long.class) { method = "getLong"; descriptor = "(Ljava/lang/Object;J)J"; } 
        else if (type == boolean.class) { method = "getBoolean"; descriptor = "(Ljava/lang/Object;J)Z"; } 
        else if (type == byte.class) { method = "getByte"; descriptor = "(Ljava/lang/Object;J)B"; } 
        else if (type == char.class) { method = "getChar"; descriptor = "(Ljava/lang/Object;J)C"; } 
        else if (type == short.class) { method = "getShort"; descriptor = "(Ljava/lang/Object;J)S"; } 
        else if (type == float.class) { method = "getFloat"; descriptor = "(Ljava/lang/Object;J)F"; } 
        else if (type == double.class) { method = "getDouble"; descriptor = "(Ljava/lang/Object;J)D"; } 
        else { method = "getObject"; descriptor = "(Ljava/lang/Object;J)Ljava/lang/Object;"; }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE_CLASS_NAME, method, descriptor, false);

        // 装箱基本类型
        if (type.isPrimitive()) {
            boxPrimitive(mv, type);
        }

        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(4, 2);
        mv.visitEnd();
    }

    /** setValue 方法 */
    private static void generateSetValue(ClassWriter cw, Field field, String internalClass) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "setValue",
                "(Ljava/lang/Object;Ljava/lang/Object;)V", null, null);
        mv.visitCode();

        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClass, "UNSAFE",
                "L" + UNSAFE_CLASS_NAME + ";");
        if (Modifier.isStatic(field.getModifiers())) {
            mv.visitLdcInsn(Type.getType(field.getDeclaringClass()));
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE_CLASS_NAME,
                    "staticFieldBase", "(Ljava/lang/Class;)Ljava/lang/Object;", false);
        } else {
            mv.visitVarInsn(Opcodes.ALOAD, 1);
        }
        mv.visitFieldInsn(Opcodes.GETSTATIC, internalClass, "OFFSET", "J");

        // 加载值并拆箱
        Class<?> type = field.getType();
        mv.visitVarInsn(Opcodes.ALOAD, 2);
        if (type.isPrimitive()) unboxPrimitive(mv, type);

        // 调用 putXXX
        String method;
        String desc;
        if (type == int.class) { method = "putInt"; desc = "(Ljava/lang/Object;JI)V"; } 
        else if (type == long.class) { method = "putLong"; desc = "(Ljava/lang/Object;JJ)V"; } 
        else if (type == boolean.class) { method = "putBoolean"; desc = "(Ljava/lang/Object;JZ)V"; } 
        else if (type == byte.class) { method = "putByte"; desc = "(Ljava/lang/Object;JB)V"; } 
        else if (type == char.class) { method = "putChar"; desc = "(Ljava/lang/Object;JC)V"; } 
        else if (type == short.class) { method = "putShort"; desc = "(Ljava/lang/Object;JS)V"; } 
        else if (type == float.class) { method = "putFloat"; desc = "(Ljava/lang/Object;JF)V"; } 
        else if (type == double.class) { method = "putDouble"; desc = "(Ljava/lang/Object;JD)V"; } 
        else { method = "putObject"; desc = "(Ljava/lang/Object;JLjava/lang/Object;)V"; }

        mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, UNSAFE_CLASS_NAME, method, desc, false);
        mv.visitInsn(Opcodes.RETURN);
        mv.visitMaxs(5, 3);
        mv.visitEnd();
    }

    /** getFieldName */
    private static void generateGetFieldName(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldName",
                "()Ljava/lang/String;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(field.getName());
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /** getFieldType */
    private static void generateGetFieldType(ClassWriter cw, Field field) {
        MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "getFieldType",
                "()Ljava/lang/Class;", null, null);
        mv.visitCode();
        mv.visitLdcInsn(Type.getType(field.getType()));
        mv.visitInsn(Opcodes.ARETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();
    }

    /** 装箱 */
    private static void boxPrimitive(MethodVisitor mv, Class<?> type) {
        if (type == int.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer", "valueOf", "(I)Ljava/lang/Integer;", false);
        else if (type == long.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Long", "valueOf", "(J)Ljava/lang/Long;", false);
        else if (type == boolean.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Boolean", "valueOf", "(Z)Ljava/lang/Boolean;", false);
        else if (type == byte.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Byte", "valueOf", "(B)Ljava/lang/Byte;", false);
        else if (type == char.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Character", "valueOf", "(C)Ljava/lang/Character;", false);
        else if (type == short.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Short", "valueOf", "(S)Ljava/lang/Short;", false);
        else if (type == float.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Float", "valueOf", "(F)Ljava/lang/Float;", false);
        else if (type == double.class) mv.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Double", "valueOf", "(D)Ljava/lang/Double;", false);
    }

    /** 拆箱 */
    private static void unboxPrimitive(MethodVisitor mv, Class<?> type) {
        if (type == int.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Integer"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Integer", "intValue", "()I", false); }
        else if (type == long.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Long"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Long", "longValue", "()J", false); }
        else if (type == boolean.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Boolean"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false); }
        else if (type == byte.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Byte"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Byte", "byteValue", "()B", false); }
        else if (type == char.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Character"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Character", "charValue", "()C", false); }
        else if (type == short.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Short"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Short", "shortValue", "()S", false); }
        else if (type == float.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Float"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Float", "floatValue", "()F", false); }
        else if (type == double.class) { mv.visitTypeInsn(Opcodes.CHECKCAST, "java/lang/Double"); mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Double", "doubleValue", "()D", false); }
        // 对象类型无需拆箱
    }

    /** 简化 ClassLoader */
    private static class AccessorClassLoader extends ClassLoader {
        public Class<?> defineClass(String name, byte[] bytes) {
            try {
                // JDK 9+ 使用 Lookup.defineClass
                return MethodHandles.lookup().defineClass(bytes);
            } catch (IllegalAccessException e) {
                // fallback JDK 8
                return super.defineClass(name, bytes, 0, bytes.length);
            }
        }
    }
}
