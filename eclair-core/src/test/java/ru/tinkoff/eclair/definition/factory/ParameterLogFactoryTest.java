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

import org.junit.Test;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.ParameterLog;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.Map;

import static java.util.Collections.singletonMap;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.*;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ParameterLogFactoryTest {

    @Test
    public void newInstance() {
        // given
        Log log = givenLog();
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog.getLevel(), is(INFO));
        assertThat(parameterLog.getIfEnabledLevel(), is(WARN));
        assertThat(parameterLog.getVerboseLevel(), is(ERROR));
        assertThat(parameterLog.getPrinter(), is(printer));
    }

    private Log givenLog() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", INFO);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", ERROR);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.class, null);
    }

    private Printer givenPrinter() {
        return new ToStringPrinter();
    }

    /**
     * TODO: Incorrect test, because annotation always synthesized correct (according to {@link org.springframework.core.annotation.AliasFor})
     * TODO: replace all 'byValue' tests by getAnnotation/findAnnotation tests, remove {@link AnnotationAttribute} if not necessary
     */
    @Test
    public void newInstanceByValue() {
        // given
        Log log = givenLogByValue();
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog.getLevel(), is(WARN));
    }

    private Log givenLogByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.class, null);
    }

    @Test(expected = NullPointerException.class)
    public void newInstanceNull() {
        // given
        Log log = null;
        Printer printer = givenPrinter();
        // when
        ParameterLog parameterLog = ParameterLogFactory.newInstance(log, printer);
        // then
        assertThat(parameterLog, nullValue());
    }
}
