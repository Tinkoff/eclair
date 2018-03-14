package ru.tinkoff.integration.eclair.mask;

import org.springframework.util.Assert;
import ru.tinkoff.integration.eclair.core.ClassUtils;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;

public class MaskRegistry {

    private final Map<Class<?>, Object> masks = new HashMap<>();

    public MaskRegistry() {
        masks.put(byte.class, (byte) 0);
        masks.put(short.class, (short) 0);
        masks.put(char.class, (char) 0);
        masks.put(int.class, 0);
        masks.put(long.class, 0L);
        masks.put(float.class, 0f);
        masks.put(double.class, 0d);
        masks.put(boolean.class, false);
        masks.put(Byte.class, (byte) 0);
        masks.put(Short.class, (short) 0);
        masks.put(Character.class, (char) 0);
        masks.put(Integer.class, 0);
        masks.put(Long.class, 0L);
        masks.put(Float.class, 0f);
        masks.put(Double.class, 0d);
        masks.put(Boolean.class, false);
    }

    public MaskRegistry register(Object mask) {
        Assert.notNull(mask, "'mask' argument is required; it could not be null");
        masks.put(mask.getClass(), mask);
        return this;
    }

    Object find(Class<?> maskedClass) {
        Object mask = masks.get(maskedClass);
        if (nonNull(mask)) {
            return mask;
        }
        Class<?> foundMaskClass = ClassUtils.findMostSpecificAncestor(masks.keySet(), maskedClass);
        return isNull(foundMaskClass) ? null : masks.get(foundMaskClass);
    }
}
