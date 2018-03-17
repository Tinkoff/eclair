package ru.tinkoff.eclair.logger;

import lombok.Setter;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.core.DefaultParameterNameDiscoverer;
import org.springframework.core.ParameterNameDiscoverer;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;

import java.util.List;
import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * @author Viacheslav Klapatniuk
 */
public class SimpleLogger extends LevelSensitiveLogger implements ManualLogger {

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
        String loggerName = loggerNameBuilder.buildByInvoker();
        return isLevelEnabled(loggerName, expectedLevel);
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        String loggerName = loggerNameBuilder.buildByInvoker();
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
    protected void logIn(MethodInvocation invocation, LogPack logPack) {
        InLog inLog = logPack.getInLog();
        String loggerName = getLoggerName(invocation);
        String message = IN + buildArgumentsClause(invocation, inLog, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(inLog.getLevel(), message);
    }

    @Override
    protected void logOut(MethodInvocation invocation, LogPack logPack, Object result) {
        OutLog outLog = logPack.getOutLog();
        String loggerName = getLoggerName(invocation);
        String message = OUT + buildResultClause(invocation, outLog, result, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(outLog.getLevel(), message);
    }

    @Override
    public void logError(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        ErrorLog errorLog = logPack.findErrorLog(throwable.getClass());
        String loggerName = getLoggerName(invocation);
        String message = ERROR + buildCauseClause(errorLog, throwable, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(errorLog.getLevel(), message, throwable);
    }

    @Override
    protected void logEmergencyOut(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        String loggerName = getLoggerName(invocation);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(logPack.getOutLog().getLevel(), ERROR);
    }

    private String buildArgumentsClause(MethodInvocation invocation, InLog inLog, String loggerName) {
        boolean verboseFound = false;
        StringBuilder builder = new StringBuilder();
        List<ArgLog> argLogs = inLog.getArgLogs();
        Object[] arguments = invocation.getArguments();
        String[] parameterNames = null;
        if (printParameterName) {
            parameterNames = parameterNameDiscoverer.getParameterNames(invocation.getMethod());
        }
        int length = arguments.length;
        for (int a = 0; a < length; a++) {
            ArgLog argLog = argLogs.get(a);
            if (nonNull(argLog) && isLevelEnabled(loggerName, argLog.getIfEnabledLevel())) {
                if (verboseFound) {
                    builder.append(", ");
                } else {
                    verboseFound = true;
                }
                if (printParameterName && nonNull(parameterNames)) {
                    builder.append(parameterNames[a]).append("=");
                }
                Object argument = arguments[a];
                if (isNull(argument)) {
                    builder.append((String) null);
                } else {
                    builder.append(argLog.getPrinter().print(argument));
                }
            }
        }
        if (!verboseFound && length == 0) {
            verboseFound = isLevelEnabled(loggerName, inLog.getVerboseLevel());
        }
        return verboseFound ? " " + builder : "";
    }

    private String buildResultClause(MethodInvocation invocation, OutLog outLog, Object result, String loggerName) {
        if (isLevelEnabled(loggerName, outLog.getVerboseLevel())) {
            if (nonNull(result)) {
                return " " + outLog.getPrinter().print(result);
            }
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType != void.class && returnType != Void.class) {
                return " null";
            }
        }
        return "";
    }

    private String buildCauseClause(ErrorLog errorLog, Throwable throwable, String loggerName) {
        if (isLevelEnabled(loggerName, errorLog.getVerboseLevel())) {
            return " " + throwable.toString();
        }
        return "";
    }
}
