package ru.tinkoff.integration.eclair.deprecated.audit.wrapper;

import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;

@Component
public class ObjectFactoryWrapper {

    public Object tryWrapByPayloadClassIteratively(Object payload) {
        Class<?> payloadClass = payload.getClass();
        Iterator<String> iterator = new PackageNameIterator(payloadClass);
        while (iterator.hasNext()) {
            try {
                return wrapByPayloadClass(iterator.next(), payloadClass, payload);
            } catch (RuntimeException e) {
                // go to next iteration
            }
        }
        throw new RuntimeException("ObjectFactory class not found by payload class recursively: " + payloadClass);
    }

    private Object wrapByPayloadClass(String packageName, Class<?> payloadClass, Object payload) {
        Class<?> wrapperClass = getWrapperClass(packageName);
        Method setter = getSetter(payloadClass, wrapperClass);
        Object wrapper = getWrapper(wrapperClass);
        return invokeWrapperMethod(setter, wrapper, payload);
    }

    private Class<?> getWrapperClass(String packageName) {
        try {
            return Class.forName(packageName + ".ObjectFactory");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("ObjectFactory class not found", e);
        }
    }

    private Method getSetter(Class<?> payloadClass, Class<?> wrapperClass) {
        for (Method setter : wrapperClass.getMethods()) {
            Class<?>[] parameterClasses = setter.getParameterTypes();
            if (parameterClasses.length != 1) {
                continue;
            }
            if (payloadClass.equals(parameterClasses[0])) {
                return setter;
            }
        }
        throw new RuntimeException("ObjectFactory wrapper method not found by payload class: " + payloadClass);
    }

    private Object getWrapper(Class<?> wrapperClass) {
        try {
            return wrapperClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("ObjectFactory class could not be instantiated", e);
        }
    }

    private Object invokeWrapperMethod(Method wrapperMethod, Object wrapper, Object payload) {
        try {
            return wrapperMethod.invoke(wrapper, payload);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("ObjectFactory method could not be invoked", e);
        }
    }
}
