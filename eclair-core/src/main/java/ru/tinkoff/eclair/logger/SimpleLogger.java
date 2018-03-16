package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import ru.tinkoff.eclair.core.ActualLevelResolver;
import ru.tinkoff.eclair.core.ClassInvokerResolver;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.ArgLogDefinition;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;
import ru.tinkoff.eclair.logger.facade.LoggerFacade;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * @author Viacheslav Klapatniuk
 */
public class SimpleLogger extends Logger implements ManualLogger {

    private static final Map<Class<?>, String> EVENT_LITERALS = new HashMap<>();

    private final LoggerFacadeFactory loggerFacadeFactory;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final ClassInvokerResolver invokerResolver = ClassInvokerResolver.getInstance();
    private final ActualLevelResolver actualLevelResolver = ActualLevelResolver.getInstance();
    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    private boolean printParameterName = true;

    static {
        EVENT_LITERALS.put(InLogDefinition.class, ">");
        EVENT_LITERALS.put(OutLogDefinition.class, "<");
        EVENT_LITERALS.put(ErrorLogDefinition.class, "!");
        EVENT_LITERALS.put(ManualLogDefinition.class, "-");
    }

    public SimpleLogger(LoggerFacadeFactory loggerFacadeFactory) {
        this.loggerFacadeFactory = loggerFacadeFactory;
    }

    @Override
    protected boolean isLevelEnabled(MethodInvocation invocation, LogLevel expectedLevel) {
        LogLevel actualLevel = actualLevelResolver.resolve(invocation.getMethod());
        return isLevelEnabled(expectedLevel, actualLevel);
    }

    private boolean isLevelEnabled(LogLevel expectedLevel, LogLevel actualLevel) {
        return expectedLevel.ordinal() >= actualLevel.ordinal() && actualLevel != OFF;
    }

    @Override
    public boolean isLevelEnabled(LogLevel expectedLevel) {
        LogLevel actualLevel = actualLevelResolver.resolve(invokerResolver.resolve(SimpleLogger.class));
        return isLevelEnabled(expectedLevel, actualLevel);
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        String loggerName = loggerNameBuilder.build(invokerResolver.resolve(SimpleLogger.class));
        if (isLevelEnabled(level, actualLevelResolver.resolve(loggerName))) {
            String message = buildManualMessage(format);
            Object[] unwrappedArguments = unwrapArguments(arguments);
            loggerFacadeFactory.getLoggerFacade(loggerName).log(level, message, unwrappedArguments);
        }
    }

    private String buildManualMessage(String format) {
        String eventLiteral = buildEventLiteral(ManualLogDefinition.class);
        return format("%s %s", eventLiteral, format);
    }

    private Object[] unwrapArguments(Object... arguments) {
        return Stream.of(arguments)
                .map(argument -> (argument instanceof Supplier) ? ((Supplier) argument).get() : argument)
                .toArray();
    }

    @Override
    public void logIn(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        LogLevel expectedLevel = inLogDefinition.getLevel();
        String message = buildInMessage(invocation, inLogDefinition);
        LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerNameBuilder.build(invocation.getMethod()));
        loggerFacade.log(expectedLevel, message);
    }

    private String buildInMessage(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        String eventLiteral = buildEventLiteral(InLogDefinition.class);
        String argsClause = buildInArgsClause(invocation, inLogDefinition);
        return format("%s%s", eventLiteral, argsClause);
    }

    private String buildEventLiteral(Class<?> logDefinitionClass) {
        return EVENT_LITERALS.get(logDefinitionClass);
    }

    private String buildInArgsClause(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        Method method = invocation.getMethod();
        LogLevel actualLevel = actualLevelResolver.resolve(method);
        boolean verboseFound = false;

        StringBuilder builder = new StringBuilder();
        List<ArgLogDefinition> argLogDefinitions = inLogDefinition.getArgLogDefinitions();
        Object[] arguments = invocation.getArguments();
        String[] parameterNames = null;
        if (printParameterName) {
            parameterNames = parameterNameDiscoverer.getParameterNames(method);
        }
        int length = arguments.length;
        for (int a = 0; a < length; a++) {
            ArgLogDefinition argLogDefinition = argLogDefinitions.get(a);
            if (nonNull(argLogDefinition) && isVerboseArg(inLogDefinition.getVerboseLevel(), argLogDefinition, actualLevel)) {
                if (verboseFound) {
                    builder.append(", ");
                } else {
                    verboseFound = true;
                }
                if (printParameterName && nonNull(parameterNames)) {
                    builder.append(parameterNames[a]).append("=");
                }
                if (isNull(arguments[a])) {
                    builder.append((String) null);
                } else {
                    builder.append(argLogDefinition.getPrinter().print(arguments[a]));
                }
            }
        }

        if (!verboseFound && length == 0) {
            verboseFound = isLevelEnabled(inLogDefinition.getVerboseLevel(), actualLevel);
        }

        return verboseFound ? format(" %s", builder.toString()) : "";
    }


    private boolean isVerboseArg(LogLevel verboseLevel, ArgLogDefinition argLogDefinition, LogLevel actualLevel) {
        LogLevel expectedLevel = isNull(argLogDefinition) ? verboseLevel : argLogDefinition.getIfEnabledLevel();
        return isLevelEnabled(expectedLevel, actualLevel);
    }

    @Override
    public void logOut(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, boolean emergency) {
        LogLevel expectedLevel = outLogDefinition.getLevel();
        String message = buildOutMessage(invocation, result, outLogDefinition, emergency);
        LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerNameBuilder.build(invocation.getMethod()));
        loggerFacade.log(expectedLevel, message);
    }

    private String buildOutMessage(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, boolean emergency) {
        if (emergency) {
            return buildEventLiteral(ErrorLogDefinition.class);
        }
        String eventLiteral = buildEventLiteral(OutLogDefinition.class);
        String argClause = buildOutArgClause(invocation, result, outLogDefinition);
        return format("%s%s", eventLiteral, argClause);
    }

    private String buildOutArgClause(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition) {
        LogLevel actualLevel = actualLevelResolver.resolve(invocation.getMethod());
        if (isVerboseArg(outLogDefinition.getVerboseLevel(), null, actualLevel)) {
            if (isNull(result)) {
                Class<?> returnType = invocation.getMethod().getReturnType();
                if (returnType == void.class || returnType == Void.class) {
                    return "";
                }
                return " null";
            }
            return format(" %s", outLogDefinition.getPrinter().print(result));
        }
        return "";
    }

    @Override
    public void logError(MethodInvocation invocation, Throwable throwable, ErrorLogDefinition errorLogDefinition) {
        LogLevel expectedLevel = errorLogDefinition.getLevel();
        String message = buildErrorMessage(throwable);
        LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerNameBuilder.build(invocation.getMethod()));
        loggerFacade.log(expectedLevel, message, throwable);
    }

    private String buildErrorMessage(Throwable throwable) {
        String eventLiteral = buildEventLiteral(ErrorLogDefinition.class);
        String argClause = buildStackTraceClause(throwable);
        return format("%s%s", eventLiteral, argClause);
    }

    private String buildStackTraceClause(Throwable throwable) {
        return format(" %s", throwable.toString());
    }

    public void setPrintParameterName(boolean printParameterName) {
        this.printParameterName = printParameterName;
    }

    private static class ManualLogDefinition {

        private ManualLogDefinition() {
        }
    }
}
