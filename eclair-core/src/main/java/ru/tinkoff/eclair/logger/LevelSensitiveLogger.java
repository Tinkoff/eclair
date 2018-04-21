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
import ru.tinkoff.eclair.core.ExpectedLevelResolver;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.method.MethodLog;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * Level-sensitive abstract child of base logger.
 * Considers current log level configuration among other rules when deciding whether to log or not.
 *
 * @author Vyacheslav Klapatnyuk
 */
public abstract class LevelSensitiveLogger extends EclairLogger {

    static final Function<LogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    /**
     * Determines if log level is enabled for logger specified by name.
     *
     * @param loggerName checkable logger name
     * @param level      checkable level
     * @return {@code true} if enabled, {@code false} otherwise
     */
    protected abstract boolean isLogEnabled(String loggerName, LogLevel level);

    /**
     * Determines whether to perform 'in'-event logging or not.
     * Considers current log level configuration among other rules.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logIn(MethodInvocation, MethodLog)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    @Override
    protected boolean isLogInNecessary(MethodInvocation invocation, MethodLog methodLog) {
        String loggerName = getLoggerName(invocation);
        if (nonNull(methodLog.getInLog())) {
            if (isLogEnabled(loggerName, expectedLevelResolver.apply(methodLog.getInLog()))) {
                return true;
            }
        }
        return methodLog.getParameterLogs().stream()
                .anyMatch(parameterLog -> nonNull(parameterLog) && isLogEnabled(loggerName, expectedLevelResolver.apply(parameterLog)));
    }

    /**
     * Determines whether to perform 'out'-event logging or not.
     * Considers current log level configuration among other rules.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logOut(MethodInvocation, MethodLog, Object)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return super.isLogOutNecessary(invocation, methodLog) &&
                isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(methodLog.getOutLog()));
    }

    /**
     * Determines whether to perform 'error'-event logging or not.
     * Considers current log level configuration among other rules.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logError(MethodInvocation, MethodLog, Throwable)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        ErrorLog errorLog = methodLog.findErrorLog(throwable.getClass());
        return nonNull(errorLog) && isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLog));
    }
}
