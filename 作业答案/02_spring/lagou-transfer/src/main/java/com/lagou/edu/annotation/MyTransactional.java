package com.lagou.edu.annotation;


import java.lang.annotation.*;

/**
 * @author lixin
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface MyTransactional {
}
