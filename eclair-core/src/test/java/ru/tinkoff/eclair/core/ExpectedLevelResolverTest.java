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

import org.junit.Test;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.definition.LogDefinition;
import ru.tinkoff.eclair.definition.factory.OutLogFactory;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.*;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Viacheslav Klapatniuk
 */
public class ExpectedLevelResolverTest {

    private final ExpectedLevelResolver expectedLevelResolver = ExpectedLevelResolver.getInstance();

    @Test
    public void applyErrorWarn() {
        // given
        LogDefinition definition = givenLogDefinition(ERROR, WARN);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(WARN));
    }

    @Test
    public void applyTraceDebug() {
        // given
        LogDefinition definition = givenLogDefinition(TRACE, DEBUG);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(TRACE));
    }

    @Test
    public void applyInfoInfo() {
        // given
        LogDefinition definition = givenLogDefinition(INFO, INFO);
        // when
        LogLevel level = expectedLevelResolver.apply(definition);
        // then
        assertThat(level, is(INFO));
    }

    private LogDefinition givenLogDefinition(LogLevel level, LogLevel ifEnabled) {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", level);
        attributes.put("ifEnabled", ifEnabled);
        return OutLogFactory.newInstance(synthesizeAnnotation(attributes, Log.out.class, null), new ToStringPrinter());
    }
}
