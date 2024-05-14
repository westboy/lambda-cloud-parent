package com.jingfang.cloud.redis.cluster;

import com.jingfang.cloud.redis.annotations.ClusterPipelineBean;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ClassUtils;

import java.beans.Introspector;

/**
 * @author westboy
 */
@Slf4j
class ClusterBeanFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        String[] beans = beanFactory.getBeanNamesForAnnotation(ClusterPipelineBean.class);
        for (String bean : beans) {
            Class<?> clazz = beanFactory.getType(bean);
            if (clazz == null) {
                throw new org.springframework.beans.factory.BeanDefinitionStoreException("clazz must not be null");
            }
            ClusterPipelineBean annotation = AnnotationUtils.findAnnotation(clazz, ClusterPipelineBean.class);
            if (annotation == null) {
                throw new org.springframework.beans.factory.BeanDefinitionStoreException("annotation must not be null");
            }
            String value = annotation.value();
            String key = annotation.key();
            if (StringUtils.isNotBlank(key)) {
                BeanDefinition beanDefinition = beanFactory.getBeanDefinition(StringUtils.isEmpty(value) ?
                        buildDefaultBeanName(clazz) : value);
                MutablePropertyValues propertyValues = beanDefinition.getPropertyValues();
                propertyValues.addPropertyValue("key", key);
                if (log.isTraceEnabled()) {
                    log.trace("name: {}", beanDefinition.getBeanClassName());
                }
            }
        }
    }

    private static String buildDefaultBeanName(Class<?> clazz) {
        String shortClassName = ClassUtils.getShortName(clazz);
        return Introspector.decapitalize(shortClassName);
    }
}