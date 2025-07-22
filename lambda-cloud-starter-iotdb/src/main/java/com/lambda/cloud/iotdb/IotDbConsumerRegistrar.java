package com.lambda.cloud.iotdb;

import com.lambda.autoconfig.IotDbProperties;
import com.lambda.cloud.iotdb.annotation.IotDbSubscription;
import com.lambda.cloud.iotdb.manager.IotDbConsumerManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.lang.NonNull;

/**
 * IotDbConsumerRegistrar
 *
 * @author Jin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public record IotDbConsumerRegistrar(IotDbConsumerManager manager,
                                     IotDbProperties properties) implements BeanFactoryPostProcessor {

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IotDbSubscription.class));

        Set<BeanDefinition> beans = scanner.findCandidateComponents(properties.getBasePackage());

        for (var bd : beans) {
            try {
                Class<?> clazz = Class.forName(bd.getBeanClassName());
                if (clazz.isAnnotationPresent(IotDbSubscription.class)) {
                    IotDbSubscription annotation = clazz.getAnnotation(IotDbSubscription.class);
                    manager.register(clazz, annotation);
                }
            } catch (Exception ignored) {
            }
        }
    }
}
