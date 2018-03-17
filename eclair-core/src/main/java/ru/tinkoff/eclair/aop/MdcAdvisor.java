package ru.tinkoff.eclair.aop;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.slf4j.MDC;
import org.springframework.aop.support.StaticMethodMatcherPointcutAdvisor;
import ru.tinkoff.eclair.core.ExpressionEvaluator;
import ru.tinkoff.eclair.definition.MdcDefinition;
import ru.tinkoff.eclair.definition.MdcPack;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * @author Viacheslav Klapatniuk
 */
final class MdcAdvisor extends StaticMethodMatcherPointcutAdvisor implements MethodInterceptor {

    private final ExpressionEvaluator expressionEvaluator = ExpressionEvaluator.getInstance();
    private final Map<Method, MdcPack> mdcPacks;

    private MdcAdvisor(List<MdcPack> mdcPacks) {
        this.mdcPacks = mdcPacks.stream().collect(toMap(MdcPack::getMethod, identity()));
    }

    static MdcAdvisor newInstance(List<MdcPack> mdcPacks) {
        return mdcPacks.isEmpty() ? null : new MdcAdvisor(mdcPacks);
    }

    @Override
    public Advice getAdvice() {
        return this;
    }

    @Override
    public boolean matches(Method method, Class<?> targetClass) {
        return mdcPacks.containsKey(method);
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
            MdcPack mdcPack = mdcPacks.get(invocation.getMethod());
            processMethodDefinitions(mdcPack.getMethodDefinitions(), keys);
            processParameterDefinitions(mdcPack.getParameterDefinitions(), invocation.getArguments(), keys);
            return invocation.proceed();
        }
    }

    private void processMethodDefinitions(Set<MdcDefinition> definitions, Set<String> keys) {
        for (MdcDefinition definition : definitions) {
            String key = definition.getKey();
            if (!definition.isGlobal()) {
                keys.add(key);
            }
            MDC.put(key, expressionEvaluator.evaluate(definition.getValue()));
        }
    }

    private void processParameterDefinitions(List<Set<MdcDefinition>> definitions, Object[] arguments, Set<String> keys) {
        for (int a = 0; a < definitions.size(); a++) {
            Object argument = arguments[a];
            for (MdcDefinition definition : definitions.get(a)) {
                String key = definition.getKey();
                if (!definition.isGlobal()) {
                    keys.add(key);
                }
                MDC.put(key, expressionEvaluator.evaluate(definition.getValue(), argument));
            }
        }
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
