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

package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.definition.ErrorLog;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ErrorLogFactory {

    private static final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    public static ErrorLog newInstance(Log.error logError) {
        return new ErrorLog(
                AnnotationAttribute.LEVEL.extract(logError),
                logError.ifEnabled(),
                logError.verbose(),
                errorFilterFactory.buildErrorFilter(logError.ofType(), logError.exclude())
        );
    }
}
