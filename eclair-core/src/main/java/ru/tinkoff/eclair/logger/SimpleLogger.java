package ru.tinkoff.eclair.logger;

import lombok.AccessLevel;
import lombok.Getter;
import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.*;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.Slf4JLoggerFacadeFactory;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

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

    private static final Printer defaultPrinter = new ToStringPrinter();

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Getter(AccessLevel.PACKAGE)
    private final LoggerFacadeFactory loggerFacadeFactory;
    private final LoggingSystem loggingSystem;

    public SimpleLogger() {
        this(new Slf4JLoggerFacadeFactory());
    }

    public SimpleLogger(LoggerFacadeFactory loggerFacadeFactory) {
        this(loggerFacadeFactory, LoggingSystem.get(SimpleLogger.class.getClassLoader()));
    }

    SimpleLogger(LoggerFacadeFactory loggerFacadeFactory, LoggingSystem loggingSystem) {
        this.loggerFacadeFactory = loggerFacadeFactory;
        this.loggingSystem = loggingSystem;
    }

    @Override
    public boolean isLogEnabled(LogLevel level) {
        return level != OFF && isLogEnabled(loggerNameBuilder.buildByInvoker(), level);
    }

    @Override
    public void log(LogLevel level, LogLevel ifEnabledLevel, String format, Object... arguments) {
        String loggerName = loggerNameBuilder.buildByInvoker();
        if (isLogEnabled(loggerName, ifEnabledLevel)) {
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
    protected boolean isLogEnabled(String loggerName, LogLevel level) {
        return level != OFF &&
                level.ordinal() >= loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel().ordinal();
    }

    /**
     * Lazy check
     *
     * @see SimpleLogger#logIn(org.aopalliance.intercept.MethodInvocation, ru.tinkoff.eclair.definition.LogPack)
     */
    @Override
    protected boolean isLogInNecessary(MethodInvocation invocation, LogPack logPack) {
        return true;
    }

    @Override
    protected void logIn(MethodInvocation invocation, LogPack logPack) {
        String loggerName = getLoggerName(invocation);
        LogLevel level = null;

        // initialize 'inLog' attributes
        InLog inLog = logPack.getInLog();
        boolean isInLogLogEnabled = false;
        boolean isInLogVerboseLogEnabled = false;
        if (nonNull(inLog)) {
            LogLevel inLogLevel = inLog.getLevel();
            isInLogLogEnabled = (inLogLevel != OFF) && isLogEnabled(loggerName, expectedLevelResolver.apply(inLog));
            if (isInLogLogEnabled) {
                level = inLogLevel;
                isInLogVerboseLogEnabled = isLogEnabled(loggerName, inLog.getVerboseLevel());
            }
        }

        StringBuilder builder = new StringBuilder();
        boolean isArgLogVerboseFound = false;
        Object[] arguments = invocation.getArguments();
        for (int a = 0; a < arguments.length; a++) {
            ArgLog argLog = logPack.getArgLogs().get(a);
            boolean isArgLogDefined = nonNull(argLog);

            // filter argument
            if (isArgLogDefined) {
                LogLevel argLogLevel = argLog.getLevel();
                if (argLogLevel == OFF || !isLogEnabled(loggerName, expectedLevelResolver.apply(argLog))) {
                    continue;
                }
                if (!isInLogLogEnabled) {
                    if (isNull(level) || argLogLevel.ordinal() > level.ordinal()) {
                        level = argLogLevel;
                    }
                }
            } else if (!isInLogVerboseLogEnabled) {
                continue;
            }

            // print delimiter
            if (isArgLogVerboseFound) {
                builder.append(", ");
            } else {
                builder.append(" ");
                isArgLogVerboseFound = true;
            }

            // print parameter name
            if (!isArgLogDefined || isLogEnabled(loggerName, argLog.getVerboseLevel())) {
                String parameterName = logPack.getParameterNames().get(a);
                if (nonNull(parameterName)) {
                    builder.append(parameterName).append("=");
                }
            }

            // print parameter value
            Object argument = arguments[a];
            if (isNull(argument)) {
                builder.append((String) null);
            } else if (isArgLogDefined) {
                builder.append(printArgument(argLog.getPrinter(), argument));
            } else {
                builder.append(printArgument(inLog.getPrinter(), argument));
            }
        }

        if (isInLogLogEnabled || isArgLogVerboseFound) {
            String message = IN + builder.toString();
            loggerFacadeFactory.getLoggerFacade(loggerName).log(level, message);
        }
    }

    /**
     * Lazy check
     *
     * @see SimpleLogger#logOut(org.aopalliance.intercept.MethodInvocation, ru.tinkoff.eclair.definition.LogPack, java.lang.Object)
     * @see SimpleLogger#logError(org.aopalliance.intercept.MethodInvocation, ru.tinkoff.eclair.definition.LogPack, java.lang.Throwable)
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, LogPack logPack) {
        return true;
    }

    @Override
    protected void logOut(MethodInvocation invocation, LogPack logPack, Object result) {
        OutLog outLog = logPack.getOutLog();
        if (isNull(outLog)) {
            return;
        }
        String loggerName = getLoggerName(invocation);
        if (!isLogEnabled(loggerName, expectedLevelResolver.apply(logPack.getOutLog()))) {
            return;
        }
        String message = OUT + buildResultClause(invocation, outLog, result, loggerName);
        loggerFacadeFactory.getLoggerFacade(loggerName).log(outLog.getLevel(), message);
    }

    private String buildResultClause(MethodInvocation invocation, OutLog outLog, Object result, String loggerName) {
        if (isLogEnabled(loggerName, outLog.getVerboseLevel())) {
            if (nonNull(result)) {
                return " " + printArgument(outLog.getPrinter(), result);
            }
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType != void.class && returnType != Void.class) {
                return " null";
            }
        }
        return "";
    }

    /**
     * Lazy check
     *
     * @see SimpleLogger#logError(org.aopalliance.intercept.MethodInvocation, ru.tinkoff.eclair.definition.LogPack, java.lang.Throwable)
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        return true;
    }

    @Override
    public void logError(MethodInvocation invocation, LogPack logPack, Throwable throwable) {
        ErrorLog errorLog = logPack.findErrorLog(throwable.getClass());
        if (nonNull(errorLog)) {
            String loggerName = getLoggerName(invocation);
            if (isLogEnabled(loggerName, expectedLevelResolver.apply(errorLog))) {
                String message = ERROR + buildCauseClause(errorLog, throwable, loggerName);
                loggerFacadeFactory.getLoggerFacade(loggerName).log(errorLog.getLevel(), message, throwable);
            }
        } else {
            OutLog outLog = logPack.getOutLog();
            if (nonNull(outLog)) {
                String loggerName = getLoggerName(invocation);
                if (isLogEnabled(loggerName, expectedLevelResolver.apply(outLog))) {
                    loggerFacadeFactory.getLoggerFacade(loggerName).log(outLog.getLevel(), ERROR);
                }
            }
        }
    }

    private String buildCauseClause(ErrorLog errorLog, Throwable throwable, String loggerName) {
        if (isLogEnabled(loggerName, errorLog.getVerboseLevel())) {
            return " " + throwable.toString();
        }
        return "";
    }

    private String printArgument(Printer printer, Object argument) {
        try {
            return printer.print(argument);
        } catch (Exception e) {
            return defaultPrinter.print(argument);
        }
    }
}
