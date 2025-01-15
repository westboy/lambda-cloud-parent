package com.jingfang.cloud.plugin.manager;

import cn.hutool.core.collection.CollUtil;
import com.jingfang.cloud.plugin.IPlugin;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * PluginsManager
 *
 * @author Jin
 */
public class PluginsManager {

    private final Map<Class<?>, List<IPlugin<?>>> pluginsMap = new HashMap<>();

    public void registerPlugin(IPlugin<?> plugin) {
        Class<?> clazz = plugin.getClass().getSuperclass();
        List<IPlugin<?>> pluginList = pluginsMap.get(clazz);
        if (CollUtil.isEmpty(pluginList)) {
            pluginList = new LinkedList<>();
        }
        pluginsMap.put(clazz, pluginList);
        pluginList.add(plugin);
    }


    public <T> List<IPlugin<?>> getPlugins(Class<T> clazz) {
        return pluginsMap.get(clazz);
    }

}