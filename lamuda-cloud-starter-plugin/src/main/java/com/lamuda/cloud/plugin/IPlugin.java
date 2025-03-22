package com.lamuda.cloud.plugin;

import com.lamuda.cloud.plugin.exception.PluginException;

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