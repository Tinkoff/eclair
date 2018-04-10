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
import ru.tinkoff.eclair.definition.MethodLog;

import java.util.function.Function;

import static java.util.Objects.nonNull;

/**
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelSensitiveLogger extends EclairLogger {

    static final Function<LogDefinition, LogLevel> expectedLevelResolver = ExpectedLevelResolver.getInstance();

    protected abstract boolean isLogEnabled(String loggerName, LogLevel level);

    /**
     * Could be overridden for lazy optimal check
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
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogOutNecessary(MethodInvocation invocation, MethodLog methodLog) {
        return super.isLogOutNecessary(invocation, methodLog) &&
                isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(methodLog.getOutLog()));
    }

    /**
     * Could be overridden for lazy optimal check
     */
    @Override
    protected boolean isLogErrorNecessary(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        ErrorLog errorLog = methodLog.findErrorLog(throwable.getClass());
        return nonNull(errorLog) && isLogEnabled(getLoggerName(invocation), expectedLevelResolver.apply(errorLog));
    }
}
