package com.lambda.cloud.iotdb.annotation;

import java.lang.annotation.*;
import org.springframework.stereotype.Component;

/**
 * IotDbSubscription
 *
 * @author Jin
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface IotDbSubscription {
    String consumerId();

    String topic();

    String consumerGroupId() default "default_group";
}
