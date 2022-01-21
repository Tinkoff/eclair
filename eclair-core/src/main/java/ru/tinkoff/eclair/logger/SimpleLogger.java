/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.logger;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.OutLog;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.definition.method.MethodLog;
import ru.tinkoff.eclair.logger.collector.*;
import ru.tinkoff.eclair.logger.facade.LoggerFacade;
import ru.tinkoff.eclair.logger.facade.LoggerFacadeFactory;
import ru.tinkoff.eclair.logger.facade.Slf4JLoggerFacadeFactory;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import java.util.function.Supplier;

import static java.util.Objects.isNull;
import static java.util.Objects.nonNull;
import static org.springframework.boot.logging.LogLevel.OFF;

/**
 * Standard {@link EclairLogger} implementation for AOP and manual level-specific logging.
 * Performs logging into target defined by {@link #loggerFacadeFactory}.
 * Determines context-specific configuration using {@link #loggingSystem}.
 *
 * @author Vyacheslav Klapatnyuk
 * @see LoggerFacadeFactory
 * @see LoggingSystem
 */
public class SimpleLogger extends LevelSensitiveLogger implements ManualLogger {

    /**
     * Token indicating 'in'-event (beginning of method execution) in the log.
     */
    private static final String IN = ">";
    /**
     * Token indicating 'out'-event (ending of method execution) in the log.
     */
    private static final String OUT = "<";
    /**
     * Token indicating 'error'-event (emergency ending of method execution) in the log.
     */
    private static final String ERROR = "!";
    /**
     * Token indicating manual logging event.
     */
    private static final String MANUAL = "-";

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    private final LoggerFacadeFactory loggerFacadeFactory;
    private final LoggingSystem loggingSystem;
    private final LogInCollectorFactory<?> logInCollectorFactory;
    private final LogOutCollector<?> logOutCollector;

    public SimpleLogger() {
        this(new Slf4JLoggerFacadeFactory(), LoggingSystem.get(SimpleLogger.class.getClassLoader()));
    }

    public SimpleLogger(LoggerFacadeFactory loggerFacadeFactory, LoggingSystem loggingSystem) {
        this(loggerFacadeFactory, loggingSystem, StringJoinerLogInCollectorFactory.INSTANCE,
                ToStringLogOutCollector.INSTANCE);
    }

    public SimpleLogger(LoggerFacadeFactory loggerFacadeFactory, LoggingSystem loggingSystem,
                        LogInCollectorFactory<?> logInCollectorFactory, LogOutCollector<?> logOutCollector) {
        this.loggerFacadeFactory = loggerFacadeFactory;
        this.loggingSystem = loggingSystem;
        this.logInCollectorFactory = logInCollectorFactory;
        this.logOutCollector = logOutCollector;
    }

    /**
     * Determines if specified log level is enabled for logger by current invocation context.
     * Note: Uses information about current {@link StackTraceElement}, so not recommended if high execution speed is important.
     *
     * @param level checkable level
     * @return {@code true} if enabled, {@code false} otherwise
     */
    @Override
    public boolean isLogEnabled(LogLevel level) {
        return level != OFF && isLogEnabled(loggerNameBuilder.buildByInvoker(), level);
    }

    /**
     * Manual logging method.
     *
     * @param level          expected log level
     * @param ifEnabledLevel lowest enabled level that allows logging
     * @param format         format string with Slf4J syntax
     * @param arguments      arguments for substituting into the format string
     */
    @Override
    public void log(LogLevel level, LogLevel ifEnabledLevel, String format, Object... arguments) {
        String loggerName = loggerNameBuilder.buildByInvoker();
        if (isLogEnabled(loggerName, level) && isLogEnabled(loggerName, ifEnabledLevel)) {
            String message = MANUAL + " " + format;
            Object[] unwrappedArguments = unwrapArguments(arguments);
            loggerFacadeFactory.getLoggerFacade(loggerName).log(level, message, unwrappedArguments);
        }
    }

    /**
     * Unwraps Java 8 {@link Supplier}s if necessary.
     *
     * @param arguments raw argument array (may contain {@link Supplier}s)
     * @return unwrapped argument array ready for logging
     */
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

    /**
     * Prepares logger name according to method invocation.
     *
     * @param invocation current loggable method invocation
     * @return logger name
     */
    @Override
    protected String getLoggerName(MethodInvocation invocation) {
        return loggerNameBuilder.build(invocation);
    }

    /**
     * Determines if log level is enabled for logger specified by name.
     *
     * @param loggerName checkable logger name
     * @param level      checkable level
     * @return {@code true} if enabled, {@code false} otherwise
     */
    @Override
    protected boolean isLogEnabled(String loggerName, LogLevel level) {
        return level != OFF &&
                level.ordinal() >= loggingSystem.getLoggerConfiguration(loggerName).getEffectiveLevel().ordinal();
    }

    /**
     * Always returns {@code true} for lazy optimal check within {@link #logIn(MethodInvocation, MethodLog)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return always {@code true}
     */
    @Override
    protected boolean isLogInNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return true;
    }

    /**
     * Performs the logging of 'in'-event (beginning of method execution).
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     */
    @Override
    protected void logIn(MethodInvocation invocation, MethodLog methodLog) {
        String loggerName = getLoggerName(invocation);
        LogLevel level = null;

        // initialize 'inLog' attributes
        InLog inLog = methodLog.getInLog();
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

        boolean isParameterLogVerboseFound = false;
        boolean isParameterLogSkippedFound = false;
        Object[] arguments = invocation.getArguments();
        LogInCollector<?> logInCollector = logInCollectorFactory.create();
        for (int a = 0; a < arguments.length; a++) {
            ParameterLog parameterLog = methodLog.getParameterLogs().get(a);
            boolean isParameterLogDefined = nonNull(parameterLog);

            // filter argument
            if (isParameterLogDefined) {
                LogLevel parameterLogLevel = parameterLog.getLevel();
                if (parameterLogLevel == OFF || !isLogEnabled(loggerName, expectedLevelResolver.apply(parameterLog))) {
                    isParameterLogSkippedFound = true;
                    continue;
                }
                if (!isInLogLogEnabled) {
                    if (isNull(level) || parameterLogLevel.ordinal() > level.ordinal()) {
                        level = parameterLogLevel;
                    }
                }
            } else if (!isInLogVerboseLogEnabled) {
                isParameterLogSkippedFound = true;
                continue;
            }

            isParameterLogVerboseFound = true;

            // get parameter name
            String parameterName = null;
            if (!isParameterLogDefined || isLogEnabled(loggerName, parameterLog.getVerboseLevel())) {
                parameterName = methodLog.getParameterNames().get(a);
                if (isNull(parameterName) && isParameterLogSkippedFound) {
                    parameterName = Integer.toString(a);
                }
            }

            // get parameter value
            Object argument = arguments[a];
            String parameterValue;
            if (isNull(argument)) {
                parameterValue = null;
            } else if (isParameterLogDefined) {
                parameterValue = printArgument(parameterLog.getPrinter(), argument);
            } else {
                parameterValue = printArgument(inLog.getPrinters().get(a), argument);
            }

            logInCollector.addParameter(parameterName, parameterValue);
        }

        if (isInLogLogEnabled || isParameterLogVerboseFound) {
            LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerName);
            Object collected = logInCollector.collect();
            if (collected instanceof CharSequence) {
                String collectedString = collected.toString();
                String message = collectedString.isEmpty() ? IN : IN + " " + collectedString;
                loggerFacade.log(level, message);
            } else {
                loggerFacade.log(level, IN, collected);
            }
        }
    }

    /**
     * Always returns {@code true} for lazy optimal check within {@link #logOut(MethodInvocation, MethodLog, Object)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return always {@code true}
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return true;
    }

    /**
     * Performs the logging of 'out'-event (ending of method execution).
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param result     result of the loggable method invocation
     */
    @Override
    protected void logOut(MethodInvocation invocation, MethodLog methodLog, Object result) {
        OutLog outLog = methodLog.getOutLog();
        if (isNull(outLog)) {
            return;
        }
        String loggerName = getLoggerName(invocation);
        if (!isLogEnabled(loggerName, expectedLevelResolver.apply(outLog))) {
            return;
        }

        Object collected = logOutCollector.collect(buildResultClause(invocation, outLog, result, loggerName));
        LoggerFacade loggerFacade = loggerFacadeFactory.getLoggerFacade(loggerName);
        if (collected instanceof CharSequence) {
            String collectedString = collected.toString();
            String message = collectedString.isEmpty() ? OUT : OUT + " " + collectedString;
            loggerFacade.log(outLog.getLevel(), message);
        } else {
            loggerFacade.log(outLog.getLevel(), OUT, collected);
        }
    }

    private String buildResultClause(MethodInvocation invocation, OutLog outLog, Object result, String loggerName) {
        if (isLogEnabled(loggerName, outLog.getVerboseLevel())) {
            if (nonNull(result)) {
                return printArgument(outLog.getPrinter(), result);
            }
            Class<?> returnType = invocation.getMethod().getReturnType();
            if (returnType != void.class && returnType != Void.class) {
                return null;
            }
        }
        return "";
    }

    /**
     * Always returns {@code true} for lazy optimal check within {@link #logError(MethodInvocation, MethodLog, Throwable)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     * @return always {@code true}
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        return true;
    }

    /**
     * Performs the logging of 'error'-event (emergency ending of method execution).
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     */
    @Override
    public void logError(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        ErrorLog errorLog = methodLog.findErrorLog(throwable.getClass());
        if (nonNull(errorLog)) {
            String loggerName = getLoggerName(invocation);
            if (isLogEnabled(loggerName, expectedLevelResolver.apply(errorLog))) {
                String message = ERROR + buildCauseClause(errorLog, throwable, loggerName);
                loggerFacadeFactory.getLoggerFacade(loggerName).log(errorLog.getLevel(), message, throwable);
            }
        } else {
            OutLog outLog = methodLog.getOutLog();
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
            return PrinterResolver.defaultPrinter.print(argument);
        }
    }

    LoggerFacadeFactory getLoggerFacadeFactory() {
        return loggerFacadeFactory;
    }
}
