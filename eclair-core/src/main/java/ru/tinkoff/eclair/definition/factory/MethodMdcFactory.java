package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.definition.MethodMdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * @author Viacheslav Klapatniuk
 */
public class MethodMdcFactory {

    public static MethodMdc newInstance(Method method,
                                        List<String> parameterNames,
                                        Set<ParameterMdc> methodParameterMdcs,
                                        List<Set<ParameterMdc>> parameterMdcs) {
        if (methodParameterMdcs.isEmpty() && parameterMdcs.stream().allMatch(Collection::isEmpty)) {
            return null;
        }
        return MethodMdc.builder()
                .method(method)
                .parameterNames(parameterNames)
                .methodDefinitions(methodParameterMdcs)
                .parameterDefinitions(parameterMdcs)
                .build();
    }
}
