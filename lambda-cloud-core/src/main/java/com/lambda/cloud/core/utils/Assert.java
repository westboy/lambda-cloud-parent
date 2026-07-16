package com.lambda.cloud.core.utils;

import cn.hutool.core.util.StrUtil;
import com.lambda.cloud.core.exception.IllegalArgumentException;
import com.lambda.cloud.core.exception.IllegalStateException;
import java.util.Collection;
import java.util.Map;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * 断言工具类，提供各种条件验证方法
 * <p>
 * 该类提供了一系列静态方法用于验证程序运行时的各种条件，当条件不满足时抛出相应的异常。
 * 主要用于参数验证、状态检查等场景，帮助开发者编写更健壮的代码。
 * </p>
 *
 * <h3>主要功能：</h3>
 * <ul>
 *   <li>状态断言：验证程序状态是否符合预期</li>
 *   <li>参数断言：验证方法参数是否有效</li>
 *   <li>对象断言：验证对象是否为null、集合是否为空等</li>
 *   <li>字符串断言：验证字符串长度、内容等</li>
 *   <li>类型断言：验证对象类型是否符合要求</li>
 * </ul>
 *
 * <h3>使用示例：</h3>
 * <pre>{@code
 * // 参数验证
 * Assert.notNull(user, "用户对象不能为空");
 * Assert.hasText(username, "用户名不能为空");
 * Assert.isTrue(age > 0, "年龄必须大于0");
 *
 * // 状态验证
 * Assert.state(isInitialized, "系统尚未初始化");
 *
 * // 集合验证
 * Assert.notEmpty(userList, "用户列表不能为空");
 * }</pre>
 *
 * @author Jin
 * @since 1.0.0
 * @see IllegalArgumentException
 * @see IllegalStateException
 */
public class Assert {
    private Assert() {}

    /**
     * 断言布尔表达式为true，如果为false则抛出{@code IllegalStateException}
     * <p>
     * 该方法主要用于验证程序状态是否符合预期。与{@link #isTrue(boolean, String)}不同，
     * 此方法抛出的是{@code IllegalStateException}，表示程序状态错误，
     * 而不是参数错误。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证对象状态是否已正确初始化</li>
     *   <li>检查系统组件是否处于可用状态</li>
     *   <li>确认业务流程是否按预期执行</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.state(id == null, "The id property must not already be initialized");
     * Assert.state(isConnected, "数据库连接尚未建立");
     * Assert.state(!isShutdown, "系统已关闭，无法执行操作");
     * </pre>
     *
     * @param expression 要验证的布尔表达式
     * @param message 断言失败时的异常消息
     * @throws IllegalStateException 如果{@code expression}为{@code false}
     * @see #isTrue(boolean, String) 用于参数验证的类似方法
     */
    public static void state(boolean expression, String message) {
        if (!expression) {
            throw new IllegalStateException(message);
        }
    }

    /**
     * 断言布尔表达式为true，如果为false则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法主要用于验证方法参数是否符合预期条件。与{@link #state(boolean, String)}不同，
     * 此方法抛出的是{@code IllegalArgumentException}，表示参数错误，
     * 而不是程序状态错误。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证数值参数的范围</li>
     *   <li>检查参数之间的逻辑关系</li>
     *   <li>确认业务规则的满足情况</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isTrue(i &gt; 0, "The value must be greater than zero");
     * Assert.isTrue(age >= 18, "年龄必须大于等于18岁");
     * Assert.isTrue(startDate.before(endDate), "开始时间必须早于结束时间");
     * </pre>
     *
     * @param expression 要验证的布尔表达式
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果{@code expression}为{@code false}
     * @see #state(boolean, String) 用于状态验证的类似方法
     */
    public static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言布尔表达式为false，如果为true则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法用于验证某个条件不应该成立。当需要确保某个条件为假时使用此方法，
     * 比{@code Assert.isTrue(!expression, message)}更加直观和语义化。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证某个状态不应该存在</li>
     *   <li>检查禁止的操作条件</li>
     *   <li>确认排斥性条件</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isFalse(i == 0, "The value must not be zero");
     * Assert.isFalse(user.isDeleted(), "用户已被删除，无法执行操作");
     * Assert.isFalse(StrUtil.isEmpty(email), "邮箱地址不能为空");
     * </pre>
     *
     * @param expression 要验证的布尔表达式，期望为false
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果{@code expression}为{@code true}
     * @see #isTrue(boolean, String) 验证表达式为true的对应方法
     */
    public static void isFalse(boolean expression, String message) {
        if (expression) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象为null，如果不为null则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法用于验证某个对象应该为null的场景，通常用于确保某个字段
     * 在特定状态下没有被初始化或已被清空。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证对象在创建前未被初始化</li>
     *   <li>确认清理操作已正确执行</li>
     *   <li>检查互斥条件（某个字段为null时另一个才能设置）</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isNull(value, "The value must be null");
     * Assert.isNull(user.getId(), "新用户ID必须为空");
     * Assert.isNull(session.getToken(), "会话令牌应已清除");
     * </pre>
     *
     * @param object 要检查的对象
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果对象不为{@code null}
     * @see #notNull(Object, String) 验证对象不为null的对应方法
     */
    public static void isNull(Object object, String message) {
        if (object != null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象不为null，如果为null则抛出{@code IllegalArgumentException}
     * <p>
     * 这是最常用的断言方法之一，用于验证方法参数、返回值或关键对象不为null。
     * 在方法入口处进行参数校验时特别有用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>方法参数验证</li>
     *   <li>依赖注入对象检查</li>
     *   <li>关键业务对象验证</li>
     *   <li>配置对象完整性检查</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.notNull(clazz, "The class must not be null");
     * Assert.notNull(user, "用户对象不能为空");
     * Assert.notNull(config.getDatabase(), "数据库配置不能为空");
     * </pre>
     *
     * @param object 要检查的对象
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果对象为{@code null}
     * @see #isNull(Object, String) 验证对象为null的对应方法
     */
    public static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串有长度（不为null且不为空字符串），否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证字符串不为null且长度大于0，但允许字符串只包含空白字符。
     * 如果需要更严格的验证（不允许空白字符），请使用{@link #hasText(String, String)}。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证用户输入的基本有效性</li>
     *   <li>检查配置参数是否提供</li>
     *   <li>确保字符串字段已被设置</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.hasLength(name, "Name must not be empty");
     * Assert.hasLength(config.getUrl(), "URL配置不能为空");
     * Assert.hasLength(" ", "允许空白字符"); // 不会抛出异常
     * </pre>
     *
     * @param text 要检查的字符串
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果字符串为null或空字符串
     * @see StringUtils#hasLength
     * @see #hasText(String, String) 更严格的文本验证方法
     */
    public static void hasLength(String text, String message) {
        if (!StringUtils.hasLength(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串包含有效文本内容，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证字符串不为null且包含至少一个非空白字符。这比{@link #hasLength(String, String)}
     * 更严格，因为它不允许只包含空白字符的字符串。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证用户输入的实际内容</li>
     *   <li>检查业务数据的有效性</li>
     *   <li>确保文本字段包含有意义的内容</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.hasText(name, "'name' must not be empty");
     * Assert.hasText(user.getEmail(), "邮箱地址不能为空");
     * Assert.hasText("   ", "只有空白字符"); // 会抛出异常
     * </pre>
     *
     * @param text 要检查的字符串
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果字符串为null或不包含有效文本内容
     * @see StringUtils#hasText
     * @see #hasLength(String, String) 较宽松的长度验证方法
     */
    public static void hasText(String text, String message) {
        if (!StringUtils.hasText(text)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言给定文本不包含指定的子字符串，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法用于验证字符串中不应包含某些特定的内容，常用于安全检查、
     * 内容过滤或业务规则验证。只有当两个参数都有长度时才进行检查。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>安全检查：确保输入不包含危险字符</li>
     *   <li>内容过滤：验证文本不包含禁用词汇</li>
     *   <li>格式验证：确保标识符不包含特殊字符</li>
     *   <li>业务规则：验证名称不包含保留关键字</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.doesNotContain(name, "rod", "Name must not contain 'rod'");
     * Assert.doesNotContain(username, "admin", "用户名不能包含'admin'");
     * Assert.doesNotContain(sql, "DROP", "SQL语句不能包含DROP操作");
     * </pre>
     *
     * @param textToSearch 要搜索的文本
     * @param substring 要查找的子字符串
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果文本包含指定的子字符串
     */
    public static void doesNotContain(String textToSearch, String substring, String message) {
        if (StringUtils.hasLength(textToSearch)
                && StringUtils.hasLength(substring)
                && textToSearch.contains(substring)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数组包含元素，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证数组不为null且至少包含一个元素。这是验证数组参数有效性的
     * 常用方法，确保后续操作有数据可处理。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证方法参数数组的有效性</li>
     *   <li>确保批量操作有数据可处理</li>
     *   <li>检查配置数组是否已正确设置</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.notEmpty(array, "The array must contain elements");
     * Assert.notEmpty(userIds, "用户ID数组不能为空");
     * Assert.notEmpty(config.getServers(), "服务器配置列表不能为空");
     * </pre>
     *
     * @param array 要检查的数组
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果数组为{@code null}或不包含任何元素
     * @see #notEmpty(Collection, String) 集合版本的对应方法
     */
    public static void notEmpty(Object[] array, String message) {
        if (ObjectUtils.isEmpty(array)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言数组不包含null元素，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证数组中的所有元素都不为null。注意：如果数组为空，此方法不会抛出异常。
     * 这个方法通常与{@link #notEmpty(Object[], String)}结合使用，先确保数组不为空，
     * 再确保所有元素都不为null。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证对象数组的完整性</li>
     *   <li>确保批量处理的数据都有效</li>
     *   <li>检查配置数组中没有缺失项</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.noNullElements(array, "The array must contain non-null elements");
     * Assert.noNullElements(users, "用户数组中不能包含null元素");
     * Assert.noNullElements(new String[0], "空数组不会抛出异常"); // 不会抛出异常
     * </pre>
     *
     * @param array 要检查的数组
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果数组包含{@code null}元素
     * @see #notEmpty(Object[], String) 验证数组不为空的方法
     */
    public static void noNullElements(Object[] array, String message) {
        if (array != null) {
            for (Object element : array) {
                if (element == null) {
                    throw new IllegalArgumentException(message);
                }
            }
        }
    }

    /**
     * 断言集合包含元素，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证集合不为null且至少包含一个元素。这是验证集合参数有效性的
     * 常用方法，确保后续的遍历或处理操作有数据可用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证方法参数集合的有效性</li>
     *   <li>确保批量操作有数据可处理</li>
     *   <li>检查查询结果是否包含数据</li>
     *   <li>验证配置列表是否已正确设置</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.notEmpty(collection, "Collection must contain elements");
     * Assert.notEmpty(userList, "用户列表不能为空");
     * Assert.notEmpty(config.getRoles(), "角色配置列表不能为空");
     * </pre>
     *
     * @param collection 要检查的集合
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果集合为{@code null}或不包含任何元素
     * @see #notEmpty(Object[], String) 数组版本的对应方法
     * @see #notEmpty(Map, String) Map版本的对应方法
     */
    public static void notEmpty(Collection<?> collection, String message) {
        if (CollectionUtils.isEmpty(collection)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言Map包含条目，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证Map不为null且至少包含一个键值对。这是验证Map参数有效性的
     * 常用方法，确保后续的查找或遍历操作有数据可用。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>验证配置参数Map的有效性</li>
     *   <li>确保缓存数据已加载</li>
     *   <li>检查请求参数是否包含必要数据</li>
     *   <li>验证查询结果映射表不为空</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.notEmpty(map, "Map must contain entries");
     * Assert.notEmpty(configMap, "配置参数不能为空");
     * Assert.notEmpty(request.getParameterMap(), "请求参数不能为空");
     * </pre>
     *
     * @param map 要检查的Map
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果Map为{@code null}或不包含任何条目
     * @see #notEmpty(Collection, String) 集合版本的对应方法
     */
    public static void notEmpty(Map<?, ?> map, String message) {
        if (CollectionUtils.isEmpty(map)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言对象是指定类的实例，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证给定对象是否为指定类型的实例。这在需要确保对象类型安全的场景中
     * 非常有用，特别是在处理多态对象或进行类型转换之前。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>类型转换前的安全检查</li>
     *   <li>多态对象的类型验证</li>
     *   <li>插件或扩展点的类型检查</li>
     *   <li>反序列化后的对象类型验证</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isInstanceOf(Foo.class, foo, "Foo expected");
     * Assert.isInstanceOf(User.class, obj, "期望User类型的对象");
     * Assert.isInstanceOf(List.class, collection, "必须是List类型");
     * </pre>
     *
     * @param type 要检查的目标类型
     * @param obj 要检查的对象
     * @param message 断言失败时的异常消息前缀。如果为空或以":"、";"、","、"."结尾，
     *                将追加完整的异常消息。如果以空格结尾，将追加违规对象的类型名称。
     *                其他情况下，将追加":"和违规对象的类型名称。
     * @throws IllegalArgumentException 如果对象不是指定类型的实例
     * @see #isInstanceOf(Class, Object) 不带消息的重载版本
     */
    public static void isInstanceOf(Class<?> type, Object obj, String message) {
        notNull(type, "Type to check against must not be null");
        if (!type.isInstance(obj)) {
            instanceCheckFailed(type, obj, message);
        }
    }

    /**
     * 断言对象是指定类的实例，否则抛出{@code IllegalArgumentException}
     * <p>
     * 这是{@link #isInstanceOf(Class, Object, String)}的简化版本，
     * 使用默认的错误消息。适用于不需要自定义错误消息的场景。
     * </p>
     *
     * <pre class="code">
     * Assert.isInstanceOf(Foo.class, foo);
     * Assert.isInstanceOf(String.class, obj);
     * </pre>
     *
     * @param type 要检查的目标类型
     * @param obj 要检查的对象
     * @throws IllegalArgumentException 如果对象不是指定类型的实例
     * @see #isInstanceOf(Class, Object, String) 带自定义消息的完整版本
     */
    public static void isInstanceOf(Class<?> type, Object obj) {
        isInstanceOf(type, obj, "");
    }

    /**
     * 断言子类型可以分配给超类型，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法验证{@code superType.isAssignableFrom(subType)}为true。这用于检查
     * 类型兼容性，确保子类型可以安全地赋值给超类型变量。常用于泛型类型检查
     * 和框架中的类型验证。
     * </p>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>泛型类型参数验证</li>
     *   <li>插件接口实现检查</li>
     *   <li>反射操作前的类型兼容性验证</li>
     *   <li>框架中的类型安全检查</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass, "Number expected");
     * Assert.isAssignable(List.class, ArrayList.class, "必须是List的子类型");
     * Assert.isAssignable(Service.class, impl, "必须实现Service接口");
     * </pre>
     *
     * @param superType 超类型（父类或接口）
     * @param subType 子类型（子类或实现类）
     * @param message 断言失败时的异常消息前缀。如果为空或以":"、";"、","、"."结尾，
     *                将追加完整的异常消息。如果以空格结尾，将追加违规子类型的名称。
     *                其他情况下，将追加":"和违规子类型的名称。
     * @throws IllegalArgumentException 如果类型不可分配
     * @see #isAssignable(Class, Class) 不带消息的重载版本
     */
    public static void isAssignable(Class<?> superType, Class<?> subType, String message) {
        notNull(superType, "Super type to check against must not be null");
        if (subType == null || !superType.isAssignableFrom(subType)) {
            assignableCheckFailed(superType, subType, message);
        }
    }

    /**
     * 断言子类型可以分配给超类型，否则抛出{@code IllegalArgumentException}
     * <p>
     * 这是{@link #isAssignable(Class, Class, String)}的简化版本，
     * 使用默认的错误消息。适用于不需要自定义错误消息的场景。
     * </p>
     *
     * <pre class="code">
     * Assert.isAssignable(Number.class, myClass);
     * Assert.isAssignable(Collection.class, List.class);
     * </pre>
     *
     * @param superType 超类型（父类或接口）
     * @param subType 子类型（子类或实现类）
     * @throws IllegalArgumentException 如果类型不可分配
     * @see #isAssignable(Class, Class, String) 带自定义消息的完整版本
     */
    public static void isAssignable(Class<?> superType, Class<?> subType) {
        isAssignable(superType, subType, "");
    }

    private static void instanceCheckFailed(Class<?> type, Object obj, String msg) {
        String className = (obj != null ? obj.getClass().getName() : "null");
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, className);
                defaultMessage = false;
            }
        }
        if (defaultMessage) {
            result = result + ("Object of class [" + className + "] must be an instance of " + type);
        }
        throw new IllegalArgumentException(result);
    }

    private static void assignableCheckFailed(Class<?> superType, Class<?> subType, String msg) {
        String result = "";
        boolean defaultMessage = true;
        if (StringUtils.hasLength(msg)) {
            if (endsWithSeparator(msg)) {
                result = msg + " ";
            } else {
                result = messageWithTypeName(msg, subType);
                defaultMessage = false;
            }
        }
        if (defaultMessage) {
            result = result + (subType + " is not assignable to " + superType);
        }
        throw new IllegalArgumentException(result);
    }

    private static boolean endsWithSeparator(String msg) {
        return (msg.endsWith(":") || msg.endsWith(";") || msg.endsWith(",") || msg.endsWith("."));
    }

    private static String messageWithTypeName(String msg, Object typeName) {
        return msg + (msg.endsWith(" ") ? "" : ": ") + typeName;
    }

    /**
     * 断言字符串不为空白，否则抛出{@code IllegalArgumentException}
     * <p>
     * 该方法检查字符串是否为空白（null、空字符串或只包含空白字符），如果是空白则抛出异常。
     * 这与{@link #hasText(String, String)}功能类似，但提供了更明确的语义，
     * 专门用于检查和拒绝空白字符串的场景。
     * </p>
     *
     * <h3>空白字符串的定义：</h3>
     * <ul>
     *   <li>null值</li>
     *   <li>空字符串（""）</li>
     *   <li>只包含空白字符的字符串（空格、制表符、换行符等）</li>
     * </ul>
     *
     * <h3>使用场景：</h3>
     * <ul>
     *   <li>用户输入验证</li>
     *   <li>配置参数检查</li>
     *   <li>表单字段验证</li>
     * </ul>
     *
     * <pre class="code">
     * Assert.isBlank(null, "字符串不能为空");      // 抛出异常
     * Assert.isBlank("", "字符串不能为空");        // 抛出异常
     * Assert.isBlank(" ", "字符串不能为空白");       // 抛出异常
     * Assert.isBlank("bob", "字符串不能为空");     // 不抛出异常
     * Assert.isBlank("  bob  ", "字符串不能为空"); // 不抛出异常
     * </pre>
     *
     * @param content 要检查的字符串，可以为null
     * @param message 断言失败时的异常消息
     * @throws IllegalArgumentException 如果字符串为空白
     * @see #hasText(String, String) 检查字符串包含有效文本的方法
     * @since 2.0
     */
    public static void isBlank(String content, String message) {
        if (StrUtil.isBlank(content)) {
            throw new IllegalArgumentException(message);
        }
    }
}
