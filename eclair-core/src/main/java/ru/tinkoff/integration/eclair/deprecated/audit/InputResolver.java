package ru.tinkoff.integration.eclair.deprecated.audit;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.stream.IntStream;

/**
 * TODO: refactor
 */
@Component
class InputResolver {

    Object resolve(JoinPoint joinPoint, Class<?> inputClass) {
        /*if (inputClass.equals(Logging.DEFAULT_INPUT.class)) {
            return resolveOnlyArgument(joinPoint);
        }
        return resolveByClass(joinPoint, inputClass);*/
        return null;
    }

    private Object resolveOnlyArgument(JoinPoint joinPoint) {
        Object[] arguments = joinPoint.getArgs();
        int quantity = arguments.length;
        if (quantity > 1) {
            throw new RuntimeException("Logged method should have 1 argument or @Logging#value element should be defined");
        }
        return quantity == 0 ? null : arguments[0];
    }

    private Object resolveByClass(JoinPoint joinPoint, Class<?> inputClass) {
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        Method method = methodSignature.getMethod();
        Class<?>[] parameterTypes = method.getParameterTypes();
        int index = IntStream.range(0, parameterTypes.length)
                .filter(i -> parameterTypes[i].equals(inputClass))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Input argument with specified class not found"));
        return joinPoint.getArgs()[index];
    }
}
