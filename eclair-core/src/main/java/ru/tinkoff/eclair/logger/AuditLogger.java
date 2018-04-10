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
import ru.tinkoff.eclair.core.LoggerNameBuilder;
import ru.tinkoff.eclair.definition.MethodLog;

import static java.util.Arrays.asList;

/**
 * TODO: implement
 *
 * @author Viacheslav Klapatniuk
 */
public class AuditLogger extends EclairLogger {

    private final LoggerNameBuilder loggerNameBuilder = LoggerNameBuilder.getInstance();

    @Override
    protected String getLoggerName(MethodInvocation invocation) {
        return loggerNameBuilder.build(invocation);
    }

    @Override
    protected void logIn(MethodInvocation invocation, MethodLog methodLog) {
        System.out.println(getLoggerName(invocation) + " " + asList(invocation.getArguments()));
    }

    @Override
    protected void logOut(MethodInvocation invocation, MethodLog methodLog, Object result) {
        System.out.println(getLoggerName(invocation) + " " + result);
    }

    @Override
    protected void logError(MethodInvocation invocation, MethodLog methodLog, Throwable throwable) {
        System.out.println(getLoggerName(invocation) + " " + throwable);
    }
}
