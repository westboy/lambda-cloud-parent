package com.lambda.cloud.iotdb;

import com.lambda.cloud.iotdb.annotation.IotDbSubscription;
import com.lambda.cloud.iotdb.manager.IotDbConsumerManager;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.util.Set;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

/**
 * IotDbConsumerRegistrar
 *
 * @author Jin
 */
@SuppressFBWarnings(value = {"EI_EXPOSE_REP2"})
public class IotDbConsumerRegistrar implements BeanFactoryPostProcessor {

    private final IotDbConsumerManager manager;

    public IotDbConsumerRegistrar(IotDbConsumerManager manager) {
        this.manager = manager;
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(IotDbSubscription.class));

        Set<org.springframework.beans.factory.config.BeanDefinition> beans =
                scanner.findCandidateComponents("com.example.iotdb");

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
