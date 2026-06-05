package com.lambda.security.provider;

import static com.lambda.cloud.core.Constants.GSON;

import com.lambda.cloud.core.utils.ClassTypeUtils;
import lombok.Data;

/**
 * 第三方登录结果封装类
 *
 * <p>设计目标：
 * <ul>
 *   <li>数据封装：统一封装不同第三方平台的登录结果数据</li>
 *   <li>类型安全：提供类型安全的数据访问方法</li>
 *   <li>序列化支持：支持JSON序列化和反序列化</li>
 *   <li>扩展性：支持任意类型的第三方登录数据</li>
 * </ul>
 *
 * <p>主要功能：
 * <ul>
 *   <li>数据存储：以JSON格式存储第三方登录的原始数据</li>
 *   <li>类型转换：支持将JSON数据转换为指定的Java对象</li>
 *   <li>平台标识：记录第三方登录平台的类型标识</li>
 *   <li>数据访问：提供便捷的数据访问接口</li>
 * </ul>
 *
 * <p>使用示例：
 * <pre>{@code
 * // 创建微信登录结果
 * WeChatUserInfo weChatUser = new WeChatUserInfo("openid123", "张三", "avatar.jpg");
 * ThirdPartLoginResult result = new ThirdPartLoginResult("wechat", weChatUser);
 *
 * // 获取原始数据
 * WeChatUserInfo userInfo = result.getBody(WeChatUserInfo.class);
 *
 * // 获取字符串数据
 * String jsonData = result.getBody(String.class);
 *
 * // 获取基本类型数据
 * String openId = result.getBody(String.class);
 * }</pre>
 *
 * <p>支持的数据类型：
 * <ul>
 *   <li>字符串类型：直接返回JSON字符串</li>
 *   <li>基本类型：支持int、long、boolean等基本类型及其包装类</li>
 *   <li>复杂对象：通过GSON反序列化为指定的Java对象</li>
 *   <li>集合类型：支持List、Map等集合类型的反序列化</li>
 * </ul>
 *
 * @author Jin
 * @see AbstractThirdPartLoginProvider
 * @see ThirdPartLoginProvider
 */
@Data
public class ThirdPartLoginResult {

    /**
     * 第三方登录平台类型
     *
     * <p>平台标识说明：
     * <ul>
     *   <li>wechat：微信登录</li>
     *   <li>qq：QQ登录</li>
     *   <li>weibo：微博登录</li>
     *   <li>alipay：支付宝登录</li>
     *   <li>github：GitHub登录</li>
     *   <li>google：Google登录</li>
     * </ul>
     *
     * <p>使用场景：
     * <ul>
     *   <li>平台识别：用于识别登录数据来源平台</li>
     *   <li>业务处理：根据不同平台执行不同的业务逻辑</li>
     *   <li>数据统计：统计各平台的登录使用情况</li>
     *   <li>权限控制：为不同平台设置不同的权限策略</li>
     * </ul>
     */
    private String thirdType;

    /**
     * 第三方登录数据的JSON字符串
     *
     * <p>数据格式：
     * <ul>
     *   <li>JSON字符串：使用GSON将原始对象序列化为JSON</li>
     *   <li>完整数据：包含第三方平台返回的所有用户信息</li>
     *   <li>标准化：统一的数据存储格式，便于处理和传输</li>
     * </ul>
     *
     * <p>数据内容（以微信为例）：
     * <pre>{@code
     * {
     *   "openid": "o6_bmjrPTlm6_2sgVt7hMZOPfL2M",
     *   "nickname": "张三",
     *   "sex": 1,
     *   "language": "zh_CN",
     *   "city": "广州",
     *   "province": "广东",
     *   "country": "中国",
     *   "headimgurl": "http://thirdwx.qlogo.cn/mmopen/...",
     *   "unionid": "o6_bmasdasdsad6_2sgVt7hMZOPfL"
     * }
     * }</pre>
     */
    private String body;

    /**
     * 构造函数
     *
     * <p>功能说明：
     * <ul>
     *   <li>数据封装：将第三方登录的原始数据封装为统一格式</li>
     *   <li>JSON序列化：使用GSON将对象转换为JSON字符串</li>
     *   <li>类型记录：记录第三方登录平台的类型标识</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 封装微信登录结果
     * WeChatUserInfo userInfo = weChatApi.getUserInfo(accessToken);
     * ThirdPartLoginResult result = new ThirdPartLoginResult("wechat", userInfo);
     *
     * // 封装QQ登录结果
     * QQUserInfo qqUser = qqApi.getUserInfo(accessToken);
     * ThirdPartLoginResult qqResult = new ThirdPartLoginResult("qq", qqUser);
     * }</pre>
     *
     * @param thirdType 第三方平台类型标识
     * @param body 第三方登录返回的原始数据对象
     */
    public ThirdPartLoginResult(String thirdType, Object body) {
        this.thirdType = thirdType;
        this.body = GSON.toJson(body);
    }

    /**
     * 获取指定类型的登录数据
     *
     * <p>类型转换规则：
     * <ol>
     *   <li>字符串类型：直接返回JSON字符串，不进行反序列化</li>
     *   <li>基本类型：使用ClassTypeUtils进行类型转换</li>
     *   <li>复杂对象：使用GSON进行JSON反序列化</li>
     * </ol>
     *
     * <p>支持的类型：
     * <ul>
     *   <li>String.class：返回原始JSON字符串</li>
     *   <li>Integer.class, Long.class等：基本类型及其包装类</li>
     *   <li>自定义类：如WeChatUserInfo.class, QQUserInfo.class等</li>
     *   <li>集合类型：List&lt;T&gt;, Map&lt;String, Object&gt;等</li>
     * </ul>
     *
     * <p>使用示例：
     * <pre>{@code
     * // 获取完整的用户信息对象
     * WeChatUserInfo userInfo = result.getBody(WeChatUserInfo.class);
     *
     * // 获取JSON字符串
     * String jsonData = result.getBody(String.class);
     *
     * // 获取Map格式数据
     * Map<String, Object> dataMap = result.getBody(Map.class);
     *
     * // 获取基本类型（如果body是基本类型）
     * String openId = result.getBody(String.class);
     * }</pre>
     *
     * <p>异常处理：
     * <ul>
     *   <li>JSON解析异常：当JSON格式不正确时抛出JsonSyntaxException</li>
     *   <li>类型转换异常：当目标类型不匹配时抛出ClassCastException</li>
     *   <li>空值处理：当body为null时返回null</li>
     * </ul>
     *
     * @param <T> 目标数据类型
     * @param clazz 目标类型的Class对象
     * @return 转换后的数据对象
     * @throws com.google.gson.JsonSyntaxException 当JSON格式不正确时抛出
     * @throws ClassCastException 当类型转换失败时抛出
     */
    public <T> T getBody(Class<T> clazz) {
        if (String.class.equals(clazz)) {
            return clazz.cast(body);
        }
        if (ClassTypeUtils.isPrimitiveOrWrapper(clazz)) {
            Object result = ClassTypeUtils.convertPrimitiveOrWrapper(clazz, body);
            return clazz.cast(result);
        }
        return GSON.fromJson(body, clazz);
    }
}
