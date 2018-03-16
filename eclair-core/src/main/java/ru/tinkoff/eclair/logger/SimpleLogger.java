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
import ru.tinkoff.eclair.logger.facade.LoggerFacade;
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

    public String getLoggerName(MethodInvocation invocation) {
        return loggerNameBuilder.build(invocation);
    }

    @Override
    public boolean isLevelEnabled(String loggerName, LogLevel expectedLevel) {
        LogLevel actualLevel = loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel();
        return expectedLevel.ordinal() >= actualLevel.ordinal() && actualLevel != OFF;
    }

    @Override
    public void logIn(MethodInvocation invocation, String loggerName, InLogDefinition inLogDefinition) {
        LogLevel expectedLevel = inLogDefinition.getLevel();
        String message = IN + buildInArgsClause(invocation, inLogDefinition);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(expectedLevel, message);
    }

    private String buildInArgsClause(MethodInvocation invocation, InLogDefinition inLogDefinition) {
        String loggerName = loggerNameBuilder.build(invocation);
        boolean verboseFound = false;

        StringBuilder builder = new StringBuilder();
        List<ArgLogDefinition> argLogDefinitions = inLogDefinition.getArgLogDefinitions();
        Object[] arguments = invocation.getArguments();
        String[] parameterNames = null;
        if (printParameterName) {
            parameterNames = parameterNameDiscoverer.getParameterNames(invocation.getMethod());
        }
        int length = arguments.length;
        for (int a = 0; a < length; a++) {
            ArgLogDefinition argLogDefinition = argLogDefinitions.get(a);
            if (nonNull(argLogDefinition) && isVerboseArg(inLogDefinition.getVerboseLevel(), argLogDefinition, loggerName)) {
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
            verboseFound = isLevelEnabled(loggerName, inLogDefinition.getVerboseLevel());
        }

        return verboseFound ? format(" %s", builder.toString()) : "";
    }


    private boolean isVerboseArg(LogLevel verboseLevel, ArgLogDefinition argLogDefinition, String loggerName) {
        LogLevel expectedLevel = isNull(argLogDefinition) ? verboseLevel : argLogDefinition.getIfEnabledLevel();
        return isLevelEnabled(loggerName, expectedLevel);
    }

    @Override
    public void logOut(MethodInvocation invocation, String loggerName, Object result, OutLogDefinition outLogDefinition, boolean emergency) {
        LogLevel expectedLevel = outLogDefinition.getLevel();
        String message = buildOutMessage(invocation, result, outLogDefinition, emergency);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(expectedLevel, message);
    }

    private String buildOutMessage(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition, boolean emergency) {
        if (emergency) {
            return ERROR;
        }
        return OUT + buildOutArgClause(invocation, result, outLogDefinition);
    }

    private String buildOutArgClause(MethodInvocation invocation, Object result, OutLogDefinition outLogDefinition) {
        String loggerName = loggerNameBuilder.build(invocation);
        if (isVerboseArg(outLogDefinition.getVerboseLevel(), null, loggerName)) {
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
    public void logError(MethodInvocation invocation, String loggerName, Throwable throwable, ErrorLogDefinition errorLogDefinition) {
        LogLevel expectedLevel = errorLogDefinition.getLevel();
        String message = ERROR + " " + throwable.toString();
        LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerName);
        loggerFacade.log(expectedLevel, message, throwable);
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
}
