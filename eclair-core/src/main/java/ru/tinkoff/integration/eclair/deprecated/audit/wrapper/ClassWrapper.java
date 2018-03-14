package ru.tinkoff.integration.eclair.deprecated.audit.wrapper;

import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static java.util.Arrays.stream;

@Component
public class ClassWrapper {

    public <W, P> W wrap(Class<W> wrapperClass, P payload) {
        @SuppressWarnings("unchecked")
        Class<P> payloadClass = (Class<P>) payload.getClass();
        W wrapper = getWrapperClass(wrapperClass);
        Method setter = getSetter(wrapperClass, payloadClass);
        invokeSetter(payload, wrapper, setter);
        return wrapper;
    }

    private <W> W getWrapperClass(Class<W> wrapperClass) {
        try {
            return wrapperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("Wrapper class could not be instantiated: " + wrapperClass.getSimpleName(), e);
        }
    }

    private <W, P> Method getSetter(Class<W> wrapperClass, Class<P> payloadClass) {
        return stream(wrapperClass.getMethods())
                .filter(method -> method.getName().startsWith("set"))
                .filter(method -> method.getParameterTypes().length == 1)
                .filter(method -> method.getParameterTypes()[0].equals(payloadClass))
                .findAny()
                .orElseThrow(() -> new RuntimeException("Setter method not found by payload class: " + payloadClass));
    }

    private <W, P> void invokeSetter(P payload, W wrapper, Method setter) {
        try {
            setter.invoke(wrapper, payload);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Setter method could not be invoked", e);
        }
    }
}
