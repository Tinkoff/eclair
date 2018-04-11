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
import ru.tinkoff.eclair.core.PrinterResolver;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.printer.Printer;
import ru.tinkoff.eclair.printer.ToStringPrinter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.singletonList;
import static java.util.Collections.singletonMap;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.springframework.boot.logging.LogLevel.TRACE;
import static org.springframework.boot.logging.LogLevel.WARN;
import static org.springframework.core.annotation.AnnotationUtils.synthesizeAnnotation;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class InLogFactoryTest {

    @Test
    public void newInstance() {
        // given
        Log.in logIn = givenLogIn();
        ToStringPrinter printer = new ToStringPrinter();
        List<Printer> printers = singletonList(printer);
        // when
        InLog inLog = InLogFactory.newInstance(logIn, printers);
        // then
        assertThat(inLog.getLevel(), is(WARN));
        assertThat(inLog.getIfEnabledLevel(), is(WARN));
        assertThat(inLog.getVerboseLevel(), is(TRACE));
        assertThat(inLog.getPrinters(), hasSize(1));
        assertThat(inLog.getPrinters().get(0), is(printer));
    }

    private Log.in givenLogIn() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", WARN);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", TRACE);
        attributes.put("printer", "json");
        return synthesizeAnnotation(attributes, Log.in.class, null);
    }

    @Test
    public void newInstanceByValue() {
        // given
        Log.in logIn = givenLogInByValue();
        ToStringPrinter printer = new ToStringPrinter();
        List<Printer> printers = singletonList(printer);
        // when
        InLog inLog = InLogFactory.newInstance(logIn, printers);
        // then
        assertThat(inLog.getLevel(), is(WARN));
    }

    private Log.in givenLogInByValue() {
        return synthesizeAnnotation(singletonMap("value", WARN), Log.in.class, null);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void newInstancePrintersImmutable() {
        // given
        Log.in logIn = givenLogIn();
        ToStringPrinter printer = new ToStringPrinter();
        List<Printer> printers = singletonList(printer);
        // when
        InLog inLog = InLogFactory.newInstance(logIn, printers);
        inLog.getPrinters().add(PrinterResolver.defaultPrinter);
        // then expected exception
    }
}
