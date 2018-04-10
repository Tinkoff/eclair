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

import org.springframework.core.annotation.AnnotationAwareOrderComparator;
import ru.tinkoff.eclair.logger.EclairLogger;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toMap;

/**
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
 */
public class LoggerMapSorter {

    public Map<String, EclairLogger> sort(Map<String, EclairLogger> input) {
        return input.entrySet().stream()
                .sorted(comparing(Map.Entry::getValue, AnnotationAwareOrderComparator.INSTANCE))
                .collect(toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (logger, logger2) -> logger,
                        LinkedHashMap::new
                ));
    }
}
