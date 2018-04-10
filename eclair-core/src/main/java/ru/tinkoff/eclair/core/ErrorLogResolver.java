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

package ru.tinkoff.eclair.core;

import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.Set;

/**
 * @author Vyacheslav Klapatnyuk
 */
public final class ErrorLogResolver {

    private static final ErrorLogResolver instance = new ErrorLogResolver();

    private ErrorLogResolver() {
    }

    public static ErrorLogResolver getInstance() {
        return instance;
    }

    public ErrorLog resolve(Set<ErrorLog> errorLogs, Class<?> causeClass) {
        int minDistance = -1;
        ErrorLog result = null;
        for (ErrorLog errorLog : errorLogs) {
            if (noneMatchExclude(errorLog, causeClass)) {
                int distance = minInheritanceDistance(errorLog, causeClass);
                if (distance >= 0 && (minDistance < 0 || distance < minDistance)) {
                    minDistance = distance;
                    result = errorLog;
                }
            }
        }
        return result;
    }

    private boolean noneMatchExclude(ErrorLog errorLog, Class<?> causeClass) {
        for (Class<? extends Throwable> exclude : errorLog.getExcludes()) {
            if (exclude.isAssignableFrom(causeClass)) {
                return false;
            }
        }
        return true;
    }

    private int minInheritanceDistance(ErrorLog errorLog, Class<?> causeClass) {
        int result = -1;
        for (Class<? extends Throwable> include : errorLog.getIncludes()) {
            int distance = RelationResolver.calculateInheritanceDistance(include, causeClass);
            if (distance >= 0 && (result < 0 || distance < result)) {
                result = distance;
            }
        }
        return result;
    }
}
