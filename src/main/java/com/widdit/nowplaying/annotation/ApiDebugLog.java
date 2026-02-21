package com.widdit.nowplaying.annotation;

import java.lang.annotation.*;

/**
 * API 调试日志注解
 * 用于检测请求是否来自 API 调试页面
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ApiDebugLog {

    /**
     * 接口描述（可选）
     */
    String value() default "";
}