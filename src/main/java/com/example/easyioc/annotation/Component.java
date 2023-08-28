package com.example.easyioc.annotation;

import java.lang.annotation.*;

/**
 * @author geeksix
 * @create 2023/8/27 19:31
 */


@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Component {
    String value() default ""; //value相当于注解的属性,默认是空
}
