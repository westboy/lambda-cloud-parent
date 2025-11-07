package com.lambda.cloud.netty.protocol.scanner;

import com.lambda.cloud.netty.protocol.annotation.ProtocolPayload;
import com.lambda.cloud.netty.protocol.message.ProtocolPayloadRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Set;

/**
 * 协议消息扫描器
 * <p>
 * 扫描指定包路径下所有带有 @ProtocolPayload 注解的类，并自动注册到 ProtocolPayloadRegistry
 * </p>
 *
 * @author zx
 */
@Slf4j
public class ProtocolPayloadScanner {

    /**
     * 扫描并注册协议消息
     *
     * @param basePackages 要扫描的包路径数组
     */
    public void scanAndRegister(String... basePackages) {
        if (basePackages == null || basePackages.length == 0) {
            log.warn("No base packages specified for protocol scanning");
            return;
        }

        ClassPathScanningCandidateComponentProvider scanner = createScanner();
        int totalRegistered = 0;

        for (String basePackage : basePackages) {
            if (!StringUtils.hasText(basePackage)) {
                continue;
            }

            log.info("Scanning for @ProtocolPayload classes in package: {}", basePackage);
            Set<BeanDefinition> candidates = scanner.findCandidateComponents(basePackage);

            for (BeanDefinition beanDefinition : candidates) {
                String className = beanDefinition.getBeanClassName();
                try {
                    Class<?> clazz = ClassUtils.forName(className, null);
                    ProtocolPayload annotation = clazz.getAnnotation(ProtocolPayload.class);
                    
                    if (annotation != null && StringUtils.hasText(annotation.frameType())) {
                        // 只注册 isFrame = false 的类
                        if (!annotation.isFrame()) {
                            ProtocolPayloadRegistry.register(annotation.frameType(), clazz);
                            totalRegistered++;
                            log.debug("Registered protocol class: {} with frameType: {}", 
                                className, annotation.frameType());
                        } else {
                            log.debug("Skipped frame class: {} (isFrame=true)", className);
                        }
                    }
                } catch (ClassNotFoundException e) {
                    log.error("Failed to load class: {}", className, e);
                } catch (Exception e) {
                    log.error("Error registering protocol class: {}", className, e);
                }
            }
        }

        log.info("Protocol scanning completed. Total registered: {} classes", totalRegistered);
    }

    /**
     * 创建类路径扫描器
     *
     * @return 配置好的扫描器
     */
    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner = 
            new ClassPathScanningCandidateComponentProvider(false);
        
        // 添加 @ProtocolPayload 注解过滤器
        scanner.addIncludeFilter(new AnnotationTypeFilter(ProtocolPayload.class));
        
        return scanner;
    }
}