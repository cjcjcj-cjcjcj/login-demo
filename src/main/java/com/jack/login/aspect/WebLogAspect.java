package com.jack.login.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Arrays;

// AOP 日志切面：统一记录所有 Controller 接口的调用情况
// 使用名为 "operation" 的 logger，由 logback-spring.xml 单独持久化到 logs/operation.log
@Aspect
@Component
public class WebLogAspect {
    private static final Logger opLog = LoggerFactory.getLogger("operation");

    @Around("execution(* com.jack.login.controller..*(..))")
    public Object around(ProceedingJoinPoint pjp) throws Throwable {
        String uri = "unknown";
        String method = "unknown";
        String ip = "unknown";
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs != null) {
            HttpServletRequest request = attrs.getRequest();
            uri = request.getRequestURI();
            method = request.getMethod();
            ip = request.getRemoteAddr();
        }
        String target = pjp.getSignature().getDeclaringType().getSimpleName()
                + "." + pjp.getSignature().getName();
        String args = Arrays.toString(pjp.getArgs());

        long start = System.currentTimeMillis();
        try {
            Object result = pjp.proceed();
            opLog.info("{} {} 调用={} 参数={} IP={} 耗时={}ms 结果=成功",
                    method, uri, target, args, ip, System.currentTimeMillis() - start);
            return result;
        } catch (Throwable t) {
            opLog.error("{} {} 调用={} 参数={} IP={} 耗时={}ms 结果=异常({})",
                    method, uri, target, args, ip, System.currentTimeMillis() - start, t.getMessage());
            throw t;
        }
    }
}
