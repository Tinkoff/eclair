package ru.tinkoff.integration.eclair.deprecated.splunk;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static java.lang.System.currentTimeMillis;

@Component
public class ProcessTimer {

    private static final ThreadLocal<Map<Method, Long>> threadLocal = ThreadLocal.withInitial(HashMap::new);

    public void start(JoinPoint joinPoint) {
        threadLocal.get().put(extractMethod(joinPoint), currentTimeMillis());
    }

    public double stop(JoinPoint joinPoint) {
        return (currentTimeMillis() - threadLocal.get().remove(extractMethod(joinPoint))) / (double) 1000;
    }

    private Method extractMethod(JoinPoint joinPoint) {
        return ((MethodSignature) joinPoint.getSignature()).getMethod();
    }
}
