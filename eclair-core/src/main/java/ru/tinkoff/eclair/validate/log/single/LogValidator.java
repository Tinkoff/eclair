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
import ru.tinkoff.eclair.core.PrinterResolver;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class LogValidator extends LogAnnotationValidator {

    public LogValidator(PrinterResolver printerResolver) {
        super(printerResolver);
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz == Log.class;
    }
}
