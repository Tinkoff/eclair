package ru.tinkoff.eclair.printer;

import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
public class OverriddenToStringPrinter extends ToStringPrinter {

    @Override
    public boolean supports(Class<?> clazz) {
        Method toString = ReflectionUtils.findMethod(clazz, "toString");
        return isNull(toString) || !toString.getDeclaringClass().equals(Object.class);
    }
}
