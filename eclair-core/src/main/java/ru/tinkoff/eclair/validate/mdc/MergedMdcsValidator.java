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

package ru.tinkoff.eclair.validate.mdc;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.annotation.Mdc;

import java.util.Collection;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public class MergedMdcsValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return Collection.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        @SuppressWarnings("unchecked")
        Collection<Mdc> mdcs = (Collection<Mdc>) target;

        mdcs.stream().collect(groupingBy(Mdc::key))
                .entrySet().stream()
                // validate uniqueness of not empty MDC keys
                .filter(entry -> !entry.getKey().isEmpty())
                .filter(entry -> entry.getValue().size() > 1)
                .forEach(entry -> errors.reject("key.duplicate",
                        format("Annotations duplicated for not empty key '%s': %s", entry.getKey(), entry.getValue())));
    }
}
