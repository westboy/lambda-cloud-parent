package com.jingfang.cloud.redis.annotations;

import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * @author westboy
 */
@Target({ ElementType.TYPE })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface ClusterPipelineBean {

	String value() default "";
	
	String key();
}