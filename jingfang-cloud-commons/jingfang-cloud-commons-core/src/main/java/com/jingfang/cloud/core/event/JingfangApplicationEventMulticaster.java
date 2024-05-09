package com.jingfang.cloud.core.event;

import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ApplicationListenerMethodAdapter;
import org.springframework.context.event.SimpleApplicationEventMulticaster;
import org.springframework.core.ResolvableType;
import org.springframework.lang.Nullable;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 * @author Jin
 */

public class JingfangApplicationEventMulticaster extends SimpleApplicationEventMulticaster {

    private final JingfangSyncListenerMethodCache jingfangSyncListenerMethodCache;

    public JingfangApplicationEventMulticaster(JingfangSyncListenerMethodCache jingfangSyncListenerMethodCache) {
        this.jingfangSyncListenerMethodCache = jingfangSyncListenerMethodCache;
    }

    @Override
    public void multicastEvent(@Nullable ApplicationEvent event, @Nullable ResolvableType eventType) {
        if (event == null) {
            return;
        }
        ResolvableType type = (eventType != null ? eventType : ResolvableType.forInstance(event));
        Executor executor = getTaskExecutor();
        Collection<ApplicationListener<?>> applicationListeners = getApplicationListeners(event, type);
        for (ApplicationListener<?> listener : applicationListeners) {
            //判断是否是@EventListener标记的事件监听器
            if (listener instanceof ApplicationListenerMethodAdapter) {
                ApplicationListenerMethodAdapter listenerMethodAdapter = (ApplicationListenerMethodAdapter) listener;
                //与事件监听器方法全限定名做匹配
                String listenerId = listenerMethodAdapter.getListenerId();
                boolean syncMethod = jingfangSyncListenerMethodCache.isSync(listenerId);
                if (syncMethod) {
                    invokeListener(listener, event);
                    continue;
                }
            }
            if (executor != null) {
                executor.execute(() -> invokeListener(listener, event));
            } else {
                invokeListener(listener, event);
            }
        }
    }


}
