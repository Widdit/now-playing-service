package com.widdit.nowplaying.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * API 调试日志切面
 */
@Aspect
@Component
@Slf4j
public class ApiDebugLogAspect {

    // 自定义的 Debug 请求头名称
    private static final String DEBUG_HEADER_NAME = "X-Api-Debug";
    private static final String DEBUG_HEADER_VALUE = "true";

    @Around("@annotation(com.widdit.nowplaying.annotation.ApiDebugLog)")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable {

        // 获取当前 HTTP 请求
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();

            // 检查是否包含 Debug 标记
            String debugHeader = request.getHeader(DEBUG_HEADER_NAME);

            if (DEBUG_HEADER_VALUE.equalsIgnoreCase(debugHeader)) {
                // 获取请求详情
                String requestUri = request.getRequestURI();
                String requestMethod = request.getMethod();

                // 打印日志
                if (!"/api/system/warmUp".equals(requestUri)) {
                    log.info("<用户进行 API 接口调试：{} {}>", requestMethod, requestUri);
                }
            }
        }

        // 继续执行原方法
        return joinPoint.proceed();
    }

}