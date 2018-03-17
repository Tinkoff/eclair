package ru.tinkoff.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.definition.MdcDefinition;
import ru.tinkoff.eclair.definition.MdcPackDefinition;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * @author Viacheslav Klapatniuk
 */
final class MdcAdvisor extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final ExpressionEvaluator expressionEvaluator = ExpressionEvaluator.getInstance();
    private final Map<Method, MdcPackDefinition> mdcPackDefinitions;

    private MdcAdvisor(List<MdcPackDefinition> mdcPackDefinitions) {
        this.mdcPackDefinitions = mdcPackDefinitions.stream()
                .collect(toMap(MdcPackDefinition::getMethod, Function.identity()));
    }

    static MdcAdvisor newInstance(List<MdcPackDefinition> mdcPackDefinitions) {
        if (mdcPackDefinitions.isEmpty()) {
            return null;
        }
        return new MdcAdvisor(mdcPackDefinitions);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return mdcPackDefinitions.containsKey(method);
    }

    /**
     * Despite the fact that this method is not being used.
     */
    @Override
    public boolean isPerInstance() {
        return false;
    }

    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        Set<String> keys = new HashSet<>();
        try (MethodMdcKeysHolder ignored = new MethodMdcKeysHolder(keys)) {
            MdcPackDefinition mdcPackDefinition = mdcPackDefinitions.get(invocation.getMethod());
            processMethodDefinitions(mdcPackDefinition.getMethodDefinitions(), keys);
            processArgumentDefinitions(mdcPackDefinition.getArgumentDefinitions(), invocation.getArguments(), keys);
            return invocation.proceed();
        }
    }

    private void processMethodDefinitions(Set<MdcDefinition> definitions, Set<String> keys) {
        definitions.forEach(definition -> {
            String value = expressionEvaluator.evaluate(definition.getValue());
            MDC.put(definition.getKey(), value);
        });
        definitions.stream()
                .filter(definition -> !definition.isGlobal())
                .map(MdcDefinition::getKey)
                .forEach(keys::add);
    }

    private void processArgumentDefinitions(List<Set<MdcDefinition>> definitions, Object[] arguments, Set<String> keys) {
        for (int a = 0; a < definitions.size(); a++) {
            Object argument = arguments[a];
            definitions.get(a).forEach(definition -> {
                String value = expressionEvaluator.evaluate(definition.getValue(), argument);
                MDC.put(definition.getKey(), value);
            });
        }
        definitions.stream()
                .flatMap(Collection::stream)
                .filter(definition -> !definition.isGlobal())
                .map(MdcDefinition::getKey)
                .forEach(keys::add);
    }

    private static final class MethodMdcKeysHolder implements AutoCloseable {

        private final Set<String> keys;

        private MethodMdcKeysHolder(Set<String> keys) {
            this.keys = keys;
        }

        @Override
        public void close() throws Exception {
            keys.forEach(MDC::remove);
        }
    }
}
