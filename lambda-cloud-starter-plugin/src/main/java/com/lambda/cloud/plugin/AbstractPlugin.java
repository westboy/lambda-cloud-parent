package com.lambda.cloud.plugin;

import org.springframework.beans.factory.InitializingBean;

/**
 * AbstractPlugin
 *
 * @author Jin
 */
public abstract class AbstractPlugin<T> implements IPlugin<T>, Comparable<AbstractPlugin<T>>, InitializingBean {

    protected int weight = 0;

    /**
     * installPlugin
     *
     */
    public abstract void installPlugin() throws Exception;

    @Override
    public int compareTo(AbstractPlugin o) {
        return o.weight - this.weight;
    }
}
