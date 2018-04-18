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

package ru.tinkoff.eclair.validate.log.single;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.exception.AnnotationUsageException;
import ru.tinkoff.eclair.printer.resolver.PrinterResolver;

import java.lang.reflect.Method;
import java.util.Set;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogErrorValidator extends LogValidator<Log.error> {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    public LogErrorValidator(PrinterResolver printerResolver) {
        super(printerResolver);
    }

    @Override
    public void validate(Method method, Log.error target) throws AnnotationUsageException {
        super.validate(method, target);

        Class<? extends Throwable>[] ofType = target.ofType();
        Class<? extends Throwable>[] exclude = target.exclude();
        ErrorLog.Filter filter = errorFilterFactory.buildErrorFilter(ofType, exclude);

        Set<Class<? extends Throwable>> includes = filter.getIncludes();
        if (includes.isEmpty()) {
            throw new AnnotationUsageException("Empty error set", method, target);
        }

        Set<Class<? extends Throwable>> excludes = filter.getExcludes();
        if (ofType.length > includes.size() || exclude.length > excludes.size()) {
            throw new AnnotationUsageException("Error set should be optimized", method, target);
        }
    }
}
