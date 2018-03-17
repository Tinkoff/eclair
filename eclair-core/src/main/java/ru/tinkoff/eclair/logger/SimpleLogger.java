package ru.tinkoff.eclair.logger;

import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.ArgLogDefinition;
import ru.tinkoff.eclair.definition.ErrorLogDefinition;
import ru.tinkoff.eclair.definition.InLogDefinition;
import ru.tinkoff.eclair.definition.OutLogDefinition;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;

import java.util.List;
import java.util.function.Supplier;

import static java.lang.String.format;
import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * @author Viacheslav Klapatniuk
 */
public class SimpleLogger extends EclairLogger implements ManualLogger {

    private static final String IN = ">";
    private static final String OUT = "<";
    private static final String ERROR = "!";
    private static final String MANUAL = "-";

    private static final LoggingSystem loggingSystem = LoggingSystem.get(SimpleLogger.class.getClassLoader());

    private final LoggerFacadeFactory loggerFacadeFactory;

    private final ParameterNameDiscoverer parameterNameDiscoverer = new DefaultParameterNameDiscoverer();
    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Setter
    private boolean printParameterName = true;

    public SimpleLogger(LoggerFacadeFactory loggerFacadeFactory) {
        this.loggerFacadeFactory = loggerFacadeFactory;
    }

    @Override
    public boolean isLevelEnabled(LogLevel expectedLevel) {
        String loggerName = loggerNameBuilder.build(this.getClass());
        return isLevelEnabled(loggerName, expectedLevel);
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        String loggerName = loggerNameBuilder.build(this.getClass());
        if (isLevelEnabled(loggerName, level)) {
            String message = MANUAL + " " + format;
            Object[] unwrappedArguments = unwrapArguments(arguments);
            loggerFacadeFactory.getLoggerFacade(loggerName).log(level, message, unwrappedArguments);
        }
    }

    private Object[] unwrapArguments(Object[] arguments) {
        int length = arguments.length;
        Object[] result = new Object[length];
        for (int a = 0; a < length; a++) {
            Object argument = arguments[a];
            if (argument instanceof Supplier) {
                result[a] = ((Supplier) argument).get();
            } else {
                result[a] = argument;
            }
        }
        return result;
    }

    @Override
    protected String getLoggerName(MethodInvocation invocation) {
        return loggerNameBuilder.build(invocation);
    }

    @Override
    protected boolean isLevelEnabled(String loggerName, LogLevel expectedLevel) {
        LogLevel actualLevel = loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
        return expectedLevel.ordinal() >= actualLevel.ordinal() && actualLevel != OFF;
    }

    @Override
    protected void logIn(MethodInvocation invocation, InLogDefinition definition, String loggerName) {
        String message = buildInMessage(invocation, definition, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(definition.getLevel(), message);
    }

    private String buildInMessage(MethodInvocation invocation, InLogDefinition definition, String loggerName) {
        boolean verboseFound = false;

        StringBuilder builder = new StringBuilder();
        List<ArgLogDefinition> argLogDefinitions = definition.getArgLogDefinitions();
        Object[] arguments = invocation.getArguments();
        String[] parameterNames = null;
        if (printParameterName) {
            parameterNames = parameterNameDiscoverer.getParameterNames(invocation.getMethod());
        }
        int length = arguments.length;
        for (int a = 0; a < length; a++) {
            ArgLogDefinition argLogDefinition = argLogDefinitions.get(a);
            if (nonNull(argLogDefinition) && isLevelEnabled(loggerName, argLogDefinition.getIfEnabledLevel())) {
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
            verboseFound = isLevelEnabled(loggerName, definition.getVerboseLevel());
        }

        String inArgsClause = verboseFound ? format(" %s", builder.toString()) : "";

        return IN + inArgsClause;
    }

    @Override
    protected void logOut(MethodInvocation invocation, OutLogDefinition definition, Object result, String loggerName) {
        String message = buildOutMessage(invocation, result, definition, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(definition.getLevel(), message);
    }

    private String buildOutMessage(MethodInvocation invocation, Object result, OutLogDefinition definition, String loggerName) {
        return OUT + buildOutArgClause(invocation, result, definition, loggerName);
    }

    private String buildOutArgClause(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, String loggerName) {
        if (isLevelEnabled(loggerName, outLogDefinition.getVerboseLevel())) {
            if (nonNull(result)) {
                return " " + outLogDefinition.getPrinter().print(result);
            }
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType != void.class && returnType != Void.class) {
                return " null";
            }
        }
        return "";
    }

    @Override
    public void logError(MethodInvocation invocation, ErrorLogDefinition definition, Throwable throwable, String loggerName) {
        String message = buildErrorMessage(throwable, definition, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(definition.getLevel(), message, throwable);
    }

    private String buildErrorMessage(Throwable throwable, ErrorLogDefinition definition, String loggerName) {
        if (isLevelEnabled(loggerName, definition.getVerboseLevel())) {
            return ERROR + " " + throwable.toString();
        }
        return ERROR;
    }

    @Override
    protected void logEmergencyOut(MethodInvocation invocation, OutLogDefinition definition, Throwable throwable, String loggerName) {
        loggerFacadeFactory.getLoggerFacade(loggerName).log(definition.getLevel(), ERROR);
    }
}
