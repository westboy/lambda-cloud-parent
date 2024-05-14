package com.jingfang.cloud.core.event;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.scope.ScopedProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

/**
 * @author Jin
 * @see org.springframework.context.event.EventListenerMethodProcessor
 */
@Slf4j
public class JFSyncListenerMethodCache implements ApplicationContextAware {


    private final Set<String> syncListenerMethodCache = new HashSet<>();


    @Override
    public void setApplicationContext(@NonNull ApplicationContext applicationContext) throws BeansException {
        String[] beanNames = applicationContext.getBeanNamesForType(Object.class);
        for (String beanName : beanNames) {
            if (!ScopedProxyUtils.isScopedTarget(beanName)) {
                Class<?> beanType = null;
                try {
                    beanType = applicationContext.getType(beanName);
                } catch (Exception ex) {
                    // An unresolvable bean type, probably from a lazy bean - let's ignore it.
                    log.debug("Could not resolve target class for bean with name '{}'", beanName, ex);
                }
                if (beanType != null) {
                    Map<Method, JFSyncListener> annotatedMethods = MethodIntrospector.selectMethods(beanType,
                            (MethodIntrospector.MetadataLookup<JFSyncListener>) method ->
                                    AnnotatedElementUtils.findMergedAnnotation(method, JFSyncListener.class));
                    for (Method method : annotatedMethods.keySet()) {
                        syncListenerMethodCache.add(getDefaultListenerId(method));
                    }
                }
            }
        }
    }


    public boolean isSync(String methodId) {
        return syncListenerMethodCache.contains(methodId);
    }

    public static String getDefaultListenerId(Method method) {
        StringJoiner sj = new StringJoiner(",", "(", ")");
        for (Class<?> paramType : method.getParameterTypes()) {
            sj.add(paramType.getName());
        }
        return ClassUtils.getQualifiedMethodName(method) + sj;
    }
}
