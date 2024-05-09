package com.jingfang.cloud.core.event;

import java.lang.annotation.*;

/**
 * @author Jin
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface JingfangSyncListener {
}
