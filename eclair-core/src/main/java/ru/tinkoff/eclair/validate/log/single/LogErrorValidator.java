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

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.ErrorFilterFactory;
import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.Set;

import static java.lang.String.format;

/**
 * @author Vyacheslav Klapatnyuk
 */
@Component
public class LogErrorValidator extends MethodTargetLogAnnotationValidator {

    private final ErrorFilterFactory errorFilterFactory = ErrorFilterFactory.getInstance();

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.error.class;
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);

        Log.error logError = (Log.error) target;

        Class<? extends Throwable>[] ofType = logError.ofType();
        Class<? extends Throwable>[] exclude = logError.exclude();
        ErrorLog.Filter filter = errorFilterFactory.buildErrorFilter(ofType, exclude);

        Set<Class<? extends Throwable>> includes = filter.getIncludes();
        if (includes.isEmpty()) {
            errors.reject("error.set.empty", "Empty error set defined by annotation: " + logError);
        } else {
            Set<Class<? extends Throwable>> excludes = filter.getExcludes();
            if (ofType.length > includes.size() || exclude.length > excludes.size()) {
                errors.reject("error.set.non.optimal",
                        format("Error set defined by annotation should be optimized: ofType=%s, exclude=%s", includes, excludes));
            }
        }
    }
}
