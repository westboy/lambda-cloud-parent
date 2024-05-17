package com.jingfang.cloud.plugin;

import com.jingfang.cloud.plugin.exception.PluginException;

/**
 * IPlugin
 *
 * @author Jin
 */
public interface IPlugin<T> {

    Object invoke(T obj) throws PluginException;

}