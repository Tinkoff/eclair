package ru.tinkoff.eclair.format.printer;

import org.springframework.util.ReflectionUtils;

/**
 * @author Viacheslav Klapatniuk
 */
public class OverriddenToStringPrinter extends ToStringPrinter {

    @Override
    public boolean supports(Class<?> clazz) {
        return !ReflectionUtils.findMethod(clazz, "toString").getDeclaringClass().equals(Object.class);
    }
}
