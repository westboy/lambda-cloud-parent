package com.jingfang.cloud.plugin;

import org.springframework.beans.factory.InitializingBean;

/**
 * AbstractPlugin
 *
 * @author Jin
 */
public abstract class AbstractPlugin<T> implements IPlugin<T>, Comparable<AbstractPlugin>, InitializingBean {

    protected String title = "";
    protected String author = "";
    protected String description = "";
    protected String version = "";
    protected int weight = 0;

    /**
     * installPlugin
     *
     * @throws Exception
     */
    public abstract void installPlugin() throws Exception;

    @Override
    public int compareTo(AbstractPlugin o) {
        return o.weight - this.weight;
    }


}