package ru.tinkoff.eclair.aop;

import org.slf4j.LoggerFactory;
import org.springframework.aop.Advisor;
import org.springframework.aop.TargetSource;
import org.springframework.aop.framework.autoproxy.AbstractAutoProxyCreator;
import org.springframework.beans.BeanInstantiationException;
import org.springframework.beans.BeansException;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import ru.tinkoff.eclair.core.AnnotationDefinitionFactory;
import ru.tinkoff.eclair.core.AnnotationExtractor;
import ru.tinkoff.eclair.core.LoggerBeanNamesResolver;
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.format.printer.Printer;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.BeanClassValidator;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparing;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

/**
 * @author Viacheslav Klapatniuk
 */
public class LogProxyCreator extends AbstractAutoProxyCreator {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LogProxyCreator.class);
    private static final Object[] EMPTY_ARRAY = new Object[0];

    private final Map<String, Class<?>> beanClassCache = new ConcurrentHashMap<>();
    private final Map<Class<?>, Object[]> advisorsCache = new ConcurrentHashMap<>();

    private final Map<String, EclairLogger> loggers;
    private final GenericApplicationContext genericApplicationContext;
    private final BeanClassValidator beanClassValidator;

    private final AnnotationDefinitionFactory annotationDefinitionFactory;
    private final LoggerBeanNamesResolver loggerBeanNamesResolver = LoggerBeanNamesResolver.getInstance();
    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    private boolean validate = false;

    public LogProxyCreator(Map<String, Printer> printerMap,
                           List<Printer> printerList,
                           Map<String, EclairLogger> loggers,
                           GenericApplicationContext genericApplicationContext,
                           BeanClassValidator beanClassValidator) {
        this.annotationDefinitionFactory = new AnnotationDefinitionFactory(new PrinterResolver(initPrinters(printerMap, printerList)));
        this.loggers = initLoggers(loggers);
        this.genericApplicationContext = genericApplicationContext;
        this.beanClassValidator = beanClassValidator;
        setFrozen(true);
    }

    private Map<String, Printer> initPrinters(Map<String, Printer> printerMap, List<Printer> printerList) {
        return printerList.stream()
                .collect(toMap(
                        printer -> printerMap.entrySet().stream()
                                .filter(entry -> entry.getValue().equals(printer))
                                .findFirst()
                                .map(Map.Entry::getKey)
                                // never happens
                                .orElseThrow(() -> new IllegalArgumentException("EclairLogger bean not found on map")),
                        identity(),
                        this::mergeFunction,
                        LinkedHashMap::new
                ));
    }

    private Map<String, EclairLogger> initLoggers(Map<String, EclairLogger> loggers) {
        return loggers.entrySet().stream()
                .sorted(comparing(Map.Entry::getValue, AnnotationAwareOrderComparator.INSTANCE))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        this::mergeFunction,
                        LinkedHashMap::new
                ));
    }

    private <T> T mergeFunction(T b1, T b2) {
        // never happens
        throw new IllegalArgumentException(format("Two beans with one name: '%s', '%s'", b1, b2));
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) {
        Class<?> beanClass = bean.getClass();
        beanClassCache.put(beanName, beanClass);
        if (nonNull(advisorsCache.get(beanClass))) {
            return bean;
        }
        if (!beanClassValidator.supports(beanClass)) {
            advisorsCache.put(beanClass, EMPTY_ARRAY);
            return bean;
        }
        if (validate) {
            validateBeanClass(beanClass, beanName);
        }
        MdcAdvisor mdcAdvisor = getMdcAdvisor(beanClass);
        List<LogAdvisor> logAdvisors = getLoggingAdvisors(beanClass);
        advisorsCache.put(beanClass, composeAdvisors(mdcAdvisor, logAdvisors));
        return bean;
    }

    private void validateBeanClass(Class<?> beanClass, String beanName) {
        BindingResult bindingResult = new BeanPropertyBindingResult(beanClass, beanName);
        beanClassValidator.validate(beanClass, bindingResult);
        if (bindingResult.hasErrors()) {
            bindingResult.getAllErrors()
                    .forEach(objectError -> logger.error(objectError.getDefaultMessage()));
            throw new BeanInstantiationException(beanClass, "Incorrect logging annotations usage", new BindException(bindingResult));
        }
    }

    @Override
    protected Object[] getAdvicesAndAdvisorsForBean(Class<?> beanClass, String beanName, TargetSource customTargetSource) throws BeansException {
        Class<?> targetClass = beanClassCache.get(beanName);
        if (isNull(targetClass)) {
            return null;
        }
        Object[] cached = advisorsCache.get(targetClass);
        return (isNull(cached) || cached.length == 0) ? null : cached;
    }

    @Override
    protected boolean advisorsPreFiltered() {
        return true;
    }

    private MdcAdvisor getMdcAdvisor(Class<?> beanClass) {
        List<MdcPackDefinition> mdcDefinitions = annotationExtractor.getCandidateMethods(beanClass).stream()
                .map(annotationDefinitionFactory::buildMdcPackDefinition)
                .filter(Objects::nonNull)
                .collect(toList());
        return MdcAdvisor.newInstance(mdcDefinitions);
    }

    private List<LogAdvisor> getLoggingAdvisors(Class<?> beanClass) {
        return loggers.entrySet().stream()
                .map(entry -> getLogAdvisor(beanClass, entry.getKey(), entry.getValue()))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private LogAdvisor getLogAdvisor(Class<?> beanClass, String loggerName, EclairLogger eclairLogger) {
        List<LogDefinition> logDefinitions = getLogDefinitions(beanClass, loggerName);
        return LogAdvisor.newInstance(eclairLogger, logDefinitions);
    }

    private List<LogDefinition> getLogDefinitions(Class<?> beanClass, String loggerName) {
        Set<String> loggerNames = loggerBeanNamesResolver.resolve(genericApplicationContext, loggerName);
        return annotationExtractor.getCandidateMethods(beanClass).stream()
                .map(method -> getLogDefinition(loggerNames, method))
                .filter(Objects::nonNull)
                .collect(toList());
    }

    private LogDefinition getLogDefinition(Set<String> loggerNames, Method method) {
        InLogDefinition inLogDefinition = annotationDefinitionFactory.buildInLogDefinition(loggerNames, method);
        OutLogDefinition outLogDefinition = annotationDefinitionFactory.buildOutLogDefinition(loggerNames, method);
        Set<ErrorLogDefinition> errorLogDefinitions = annotationDefinitionFactory.buildErrorLogDefinitions(loggerNames, method);
        return LogDefinition.newInstance(method, inLogDefinition, outLogDefinition, errorLogDefinitions);
    }

    private Object[] composeAdvisors(MdcAdvisor mdcAdvisor, List<LogAdvisor> logAdvisors) {
        if (isNull(mdcAdvisor)) {
            return logAdvisors.toArray();
        }
        List<Advisor> advisors = new ArrayList<>(singletonList(mdcAdvisor));
        advisors.addAll(logAdvisors);
        return advisors.toArray();
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
