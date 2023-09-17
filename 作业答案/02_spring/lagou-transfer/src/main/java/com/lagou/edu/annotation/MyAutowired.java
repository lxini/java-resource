package com.lagou.edu.annotation;

import java.lang.annotation.*;

/**
 * @author lixin
 */
@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyAutowired {
}
