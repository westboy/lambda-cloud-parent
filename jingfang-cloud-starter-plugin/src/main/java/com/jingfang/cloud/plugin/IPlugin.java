package com.jingfang.cloud.plugin;

import com.jingfang.cloud.plugin.exception.PluginException;

/**
 * IPlugin
 *
 * @author Jin
 */
public interface IPlugin<T> {

    /**
     * invoke
     *
     * @param obj
     * @return
     * @throws PluginException
     */
    Object invoke(T obj) throws PluginException;

}