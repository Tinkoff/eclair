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

import org.springframework.context.support.GenericApplicationContext;
import org.springframework.validation.Errors;
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.logger.EclairLogger;
import ru.tinkoff.eclair.validate.log.single.LogOutValidator;

import java.util.Map;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogOutsValidator extends LoggerSpecificLogAnnotationsValidator {

    private final LogOutValidator logOutValidator;

    public LogOutsValidator(GenericApplicationContext applicationContext,
                            Map<String, EclairLogger> loggers,
                            PrinterResolver printerResolver) {
        super(applicationContext, loggers);
        this.logOutValidator = new LogOutValidator(printerResolver);
    }

    @Override
    public void validate(Object target, Errors errors) {
        super.validate(target, errors);
        ((Iterable<?>) target).forEach(logOut -> logOutValidator.validate(logOut, errors));
    }
}
