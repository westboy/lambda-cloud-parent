package com.lambda.cloud.feign.webflux;

import java.util.HashMap;
import java.util.Map;

/**
 * AttributeHolder
 *
 * @author westboy
 */
public class AttributeHolder {

    private final ThreadLocal<Map<String, String>> holder = new ThreadLocal<>();

    /**
     * *放入值
     *
     * @param key   key
     * @param value value
     */
    public void setAttribute(String key, String value) {
        if (holder.get() == null) {
            holder.set(new HashMap<>(10));
        }
        holder.get().put(key, value);
    }

    /**
     * *获取指定的值
     *
     * @param key key
     * @return value
     */
    public String getAttribute(String key) {
        if (holder.get() == null) {
            return null;
        }
        return holder.get().get(key);
    }

    /**
     * *获取所有值
     *
     * @return All attributes
     */
    public Map<String, String> getAttributes() {
        return holder.get();
    }

    /**
     * 清理数据
     */
    public void clear() {
        holder.remove();
    }
}
