package com.lambda.cloud.plugin;

import com.lambda.cloud.plugin.exception.PluginException;

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
