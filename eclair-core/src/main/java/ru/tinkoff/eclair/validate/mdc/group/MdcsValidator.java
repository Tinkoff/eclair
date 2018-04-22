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

package ru.tinkoff.eclair.validate.mdc.group;

import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.validate.AnnotationUsageException;
import ru.tinkoff.eclair.validate.AnnotationUsageValidator;

import java.lang.reflect.Method;
import java.util.Collection;

import static java.lang.String.format;
import static java.util.stream.Collectors.groupingBy;

/**
 * TODO: validate uniqueness of empty 'key' attribute on method
 * TODO: validate uniqueness of empty 'value' attribute on method
 * TODO: validate empty expression string on method without parameters
 *
 * @author Vyacheslav Klapatnyuk
 */
public class MdcsValidator implements AnnotationUsageValidator<Collection<Mdc>> {

    @Override
    public void validate(Method method, Collection<Mdc> target) throws AnnotationUsageException {
        target.stream().collect(groupingBy(Mdc::key))
                .entrySet().stream()
                .filter(entry -> entry.getValue().size() > 1)
                .findFirst()
                .ifPresent(entry -> {
                    throw new AnnotationUsageException(method,
                            format("%s annotations with 'key = %s' on the annotated element", entry.getValue().size(), entry.getKey()),
                            "Use unique 'key' per annotated element");
                });
    }
}
