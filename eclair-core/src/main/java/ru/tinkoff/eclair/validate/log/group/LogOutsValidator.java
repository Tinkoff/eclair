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

package ru.tinkoff.eclair.validate.log.group;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.log.single.LogValidator;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogOutsValidator extends GroupLogValidator<Log.out> {

    private final LogValidator<Log.out> logOutValidator;

    public LogOutsValidator(Map<String, Set<String>> loggerNames,
                            LogValidator<Log.out> logOutValidator) {
        super(loggerNames);
        this.logOutValidator = logOutValidator;
    }

    @Override
    public void validate(Method method, Set<Log.out> target) throws AnnotationUsageException {
        if (method.getReturnType().equals(Void.TYPE) || method.getReturnType().equals(Void.class)) {
            groupAnnotationsByLogger(method, target).forEach((key, logOuts) -> logOuts.forEach(logOut -> {
                if (!logOut.printer().isEmpty()) {
                    throw new AnnotationUsageException(method,
                            "Printer was defined for void method",
                            "Remove unnecessary printer parameter.");
                }
            }));
        } else {
            super.validate(method, target);
        }
        target.forEach(logOut -> logOutValidator.validate(method, logOut));
    }
}
