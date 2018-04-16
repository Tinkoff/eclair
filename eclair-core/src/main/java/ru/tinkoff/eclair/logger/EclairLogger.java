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
import ru.tinkoff.eclair.definition.MethodLog;

import java.util.Objects;

import static java.util.Objects.nonNull;

/**
 * @author Vyacheslav Klapatnyuk
 */
public abstract class EclairLogger {

    protected abstract String getLoggerName(MethodInvocation invocation);

    public void logInIfNecessary(MethodInvocation invocation, MethodLog methodLog) {
        if (isLogInNecessary(invocation, methodLog)) {
            logIn(invocation, methodLog);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogInNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getInLog()) || methodLog.getParameterLogs().stream().anyMatch(Objects::nonNull);
    }

    protected abstract void logIn(MethodInvocation invocation, MethodLog methodLog);

    public void logOutIfNecessary(MethodInvocation invocation, MethodLog methodLog, Object result) {
        if (isLogOutNecessary(invocation, methodLog)) {
            logOut(invocation, methodLog, result);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return nonNull(methodLog.getOutLog());
    }

    protected abstract void logOut(MethodInvocation invocation, MethodLog methodLog, Object result);

    public void logErrorIfNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        if (isLogErrorNecessary(invocation, methodLog, throwable) || isLogOutNecessary(invocation, methodLog)) {
            logError(invocation, methodLog, throwable);
        }
    }

    /**
     * Could be overridden for lazy optimal check
     */
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        return nonNull(methodLog.findErrorLog(throwable.getClass()));
    }

    protected abstract void logError(MethodInvocation invocation, MethodLog methodLog, Throwable throwable);
}
