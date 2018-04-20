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
import ru.tinkoff.eclair.definition.method.MethodLog;

import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * Base Eclair logger class.
 * Defines the entry point for 'in'-, 'out'- or 'error'-events logging:
 * beginning, ending or emergency ending of method execution respectively.
 *
 * @author Vyacheslav Klapatnyuk
 */
public abstract class EclairLogger {

    /**
     * Prepares logger name according to method invocation.
     *
     * @param invocation current loggable method invocation
     * @return logger name
     */
    protected abstract String getLoggerName(MethodInvocation invocation);

    /**
     * Performs the logging of 'in'-event (beginning of method execution) if necessary.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     */
    public void logInIfNecessary(MethodInvocation invocation, MethodLog methodLog) {
        if (isLogInNecessary(invocation, methodLog)) {
            logIn(invocation, methodLog);
        }
    }

    /**
     * Determines whether to perform 'in'-event logging or not.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logIn(MethodInvocation, MethodLog)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    protected boolean isLogInNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getInLog()) || methodLog.getParameterLogs().stream().anyMatch(Objects::nonNull);
    }

    /**
     * Performs the logging of 'in'-event (beginning of method execution) in an implementation-defined format.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     */
    protected abstract void logIn(MethodInvocation invocation, MethodLog methodLog);

    /**
     * Performs the logging of 'out'-event (ending of method execution) if necessary.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param result     result of the loggable method invocation
     */
    public void logOutIfNecessary(MethodInvocation invocation, MethodLog methodLog, Object result) {
        if (isLogOutNecessary(invocation, methodLog)) {
            logOut(invocation, methodLog, result);
        }
    }

    /**
     * Determines whether to perform 'out'-event logging or not.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logOut(MethodInvocation, MethodLog, Object)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getOutLog());
    }

    /**
     * Performs the logging of 'out'-event (ending of method execution) in an implementation-defined format.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param result     result of the loggable method invocation
     */
    protected abstract void logOut(MethodInvocation invocation, MethodLog methodLog, Object result);

    /**
     * Performs the logging of 'error'-event (emergency ending of method execution) if necessary.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     */
    public void logErrorIfNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        if (isLogErrorNecessary(invocation, methodLog, throwable) || isLogOutNecessary(invocation, methodLog)) {
            logError(invocation, methodLog, throwable);
        }
    }

    /**
     * Determines whether to perform 'error'-event logging or not.
     * Note: Could be overridden by {@code true} for lazy optimal check within {@link #logError(MethodInvocation, MethodLog, Throwable)}.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     * @return {@code true} if logging is necessary, {@code false} otherwise
     */
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        return nonNull(methodLog.findErrorLog(throwable.getClass()));
    }

    /**
     * Performs the logging of 'error'-event (emergency ending of method execution) in an implementation-defined format.
     *
     * @param invocation current loggable method invocation
     * @param methodLog  definition of invoked method logging
     * @param throwable  thrown during the loggable method execution
     */
    protected abstract void logError(MethodInvocation invocation, MethodLog methodLog, Throwable throwable);
}
