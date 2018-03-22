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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;
import static org.springframework.util.StringUtils.hasText;

/**
 * @author Viacheslav Klapatniuk
 */
public class JaxbElementWrapper implements PrinterPreProcessor {

    private static final Object EMPTY_METHOD = new Object();
    private static final ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
    private static final MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);

    private final Map<Class<?>, Object> wrapperMethodCache = new ConcurrentHashMap<>();
    private final Jaxb2Marshaller jaxb2Marshaller;

    public JaxbElementWrapper(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Override
    public Object process(Object input) {
        if (isNull(findAnnotation(input.getClass(), XmlRootElement.class))) {
            return wrap(jaxb2Marshaller, input);
        }
        return input;
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
        return Stream.of(contextPath.split(":"))
                .map(this::pathToResources)
                .flatMap(Stream::of)
                .filter(Resource::isReadable)
                .map(this::forName)
                .toArray(Class<?>[]::new);
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
            return Class.forName(metadataReaderFactory.getMetadataReader(resource).getClassMetadata().getClassName());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private Method findMethod(Class<?>[] classes, Class<?> argumentClass) {
        return Stream.of(classes)
                .filter(clazz -> nonNull(findAnnotation(clazz, XmlRegistry.class)))
                .map(ReflectionUtils::getAllDeclaredMethods)
                .flatMap(Stream::of)
                .filter(byParameterType(argumentClass))
                .filter(byReturnType(argumentClass))
                .filter(byAnnotation())
                .findFirst()
                .orElse(null);
    }

    private Predicate<Method> byParameterType(Class<?> argumentClass) {
        return method -> method.getParameterCount() == 1 && method.getParameterTypes()[0].equals(argumentClass);
    }

    private Predicate<Method> byReturnType(Class<?> argumentClass) {
        return method -> {
            Type genericReturnType = method.getGenericReturnType();
            if (genericReturnType instanceof ParameterizedType) {
                ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
                if (parameterizedType.getRawType().equals(JAXBElement.class)) {
                    Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                    if (actualTypeArguments.length == 1 && actualTypeArguments[0].equals(argumentClass)) {
                        return true;
                    }
                }
            }
            return false;
        };
    }

    private Predicate<Method> byAnnotation() {
        return method -> nonNull(findAnnotation(method, XmlElementDecl.class));
    }

    private Object wrap(Object input, Method method) {
        Object wrapper = BeanUtils.instantiate(method.getDeclaringClass());
        return ReflectionUtils.invokeMethod(method, wrapper, input);
    }
}
