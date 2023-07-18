package com.atguigu.gmall.common.cache;



import java.lang.annotation.*;

/**
 * @author atguigu-mqx
 */
@Target({ElementType.METHOD})//注解的作用范围 用在方法 或类 字段 构造器
@Retention(RetentionPolicy.RUNTIME)//注解的生命周期
@Inherited//文档注释注解
@Documented//文档注释注解
public @interface GmallCache {
    /**
     * return:
     * author: smile
     * version: 1.0
     * description:定义key的前缀
     */
    String prefix() default "cache" ;

    /**
     * return:
     * author: smile
     * version: 1.0
     * description:定义key的后缀
     */
    String suffix() default ":info";


}
