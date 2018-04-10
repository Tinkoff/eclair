/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.printer.processor;

import org.springframework.beans.BeanUtils;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.SystemPropertyUtils;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class JaxbElementWrapper implements PrinterPreProcessor {

    /**
     * Incorrect method for empty cache stub.
     */
    private static final Method EMPTY_METHOD = BeanUtils.findMethod(JaxbElementWrapper.class, "process", Object.class);

    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    private final Map<Class<?>, Method> wrapperMethodCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object> wrapperCache = new ConcurrentHashMap<>();

    private final Jaxb2Marshaller jaxb2Marshaller;

    public JaxbElementWrapper(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Override
    public Object process(Object input) {
        if (input instanceof JAXBElement) {
            return input;
        }
        if (nonNull(input.getClass().getAnnotation(XmlRootElement.class))) {
            return input;
        }
        return wrap(jaxb2Marshaller, input);
    }

    Map<Class<?>, Object> getWrapperCache() {
        return wrapperCache;
    }

    private Object wrap(Jaxb2Marshaller jaxb2Marshaller, Object input) {
        Class<?> clazz = input.getClass();

        Object cached = wrapperMethodCache.get(clazz);
        if (nonNull(cached)) {
            return cached == EMPTY_METHOD ? input : wrap(input, (Method) cached);
        }

        Class<?>[] wrapperClasses = findWrapperClasses(jaxb2Marshaller);
        if (isNull(wrapperClasses)) {
            wrapperMethodCache.put(clazz, EMPTY_METHOD);
            return input;
        }

        Method method = findMethod(wrapperClasses, clazz);
        if (isNull(method)) {
            wrapperMethodCache.put(clazz, EMPTY_METHOD);
            return input;
        }

        wrapperMethodCache.put(clazz, method);
        return wrap(input, method);
    }

    private Class<?>[] findWrapperClasses(Jaxb2Marshaller jaxb2Marshaller) {
        String contextPath = jaxb2Marshaller.getContextPath();
        if (!hasText(contextPath)) {
            return jaxb2Marshaller.getClassesToBeBound();
        }
        List<Class<?>> classes = new ArrayList<>();
        for (String path : contextPath.split(":")) {
            for (Resource resource : pathToResources(path)) {
                if (resource.isReadable()) {
                    classes.add(forName(resource));
                }
            }
        }
        return classes.toArray(new Class[classes.size()]);
    }

    private Resource[] pathToResources(String path) {
        String resourcePath = ClassUtils.convertClassNameToResourcePath(SystemPropertyUtils.resolvePlaceholders(path));
        String packageSearchPath = ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX + resourcePath + "/*.class";
        try {
            return resourcePatternResolver.getResources(packageSearchPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Class<?> forName(Resource resource) {
        try {
            String className = metadataReaderFactory.getMetadataReader(resource).getClassMetadata().getClassName();
            return Class.forName(className);
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?>[] classes, Class<?> parameterClass) {
        for (Class<?> clazz : classes) {
            if (nonNull(clazz.getAnnotation(XmlRegistry.class))) {
                for (Method method : ReflectionUtils.getAllDeclaredMethods(clazz)) {
                    if (byParameterType(method, parameterClass) && byReturnType(method, parameterClass) && byAnnotation(method)) {
                        return method;
                    }
                }
            }
        }
        return null;
    }

    private boolean byParameterType(Method method, Class<?> parameterClass) {
        return method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(parameterClass);
    }

    private boolean byReturnType(Method method, Class<?> parameterClass) {
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            if (parameterizedType.getRawType().equals(JAXBElement.class)) {
                Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                if (actualTypeArguments.length == 1 && actualTypeArguments[0].equals(parameterClass)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean byAnnotation(Method method) {
        return nonNull(method.getAnnotation(XmlElementDecl.class));
    }

    private Object wrap(Object input, Method method) {
        Class<?> wrapperClass = method.getDeclaringClass();
        Object wrapper = wrapperCache.computeIfAbsent(wrapperClass, BeanUtils::instantiate);
        return ReflectionUtils.invokeMethod(method, wrapper, input);
    }
}
