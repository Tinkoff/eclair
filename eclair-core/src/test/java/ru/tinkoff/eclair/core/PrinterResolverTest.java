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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.printer.Printer;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        PrinterResolverTest.TestConfiguration.class
})
public class PrinterResolverTest {

    @Autowired
    private List<Printer> printers;
    @Autowired
    private ApplicationContext applicationContext;

    private PrinterResolver printerResolver;

    @Before
    public void init() {
        printerResolver = new PrinterResolver(applicationContext, printers);
    }

    @Test
    public void resolveOrdering() {
        // when
        Map<String, Printer> printers = printerResolver.getPrinters();
        // then
        Set<Map.Entry<String, Printer>> entries = printers.entrySet();
        assertThat(entries, hasSize(3));

        Iterator<Map.Entry<String, Printer>> iterator = entries.iterator();

        Map.Entry<String, Printer> entry = iterator.next();
        assertThat(entry.getKey(), is("one"));
        assertThat(entry.getValue(), is(applicationContext.getBean("one")));

        Map.Entry<String, Printer> entry1 = iterator.next();
        assertThat(entry1.getKey(), is("zero"));
        assertThat(entry1.getValue(), is(applicationContext.getBean("zero")));

        Map.Entry<String, Printer> entry2 = iterator.next();
        assertThat(entry2.getKey(), is("two"));
        assertThat(entry2.getValue(), is(applicationContext.getBean("two")));
    }

    @Test
    public void resolveAliases() {
        // when
        Map<String, String> aliases = printerResolver.getAliases();
        // then
        assertThat(aliases.entrySet(), hasSize(6));
        assertThat(aliases.get("zero1"), is("zero"));
        assertThat(aliases.get("zero2"), is("zero"));
        assertThat(aliases.get("one1"), is("one"));
        assertThat(aliases.get("one2"), is("one"));
        assertThat(aliases.get("two1"), is("two"));
        assertThat(aliases.get("two2"), is("two"));
    }

    @Test
    public void resolveByEmptyPrinterName() {
        // given
        String printerName = "";
        Class<?> parameterType = String.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("0"));
    }

    @Test
    public void resolveByEmptyPrinterNameAndUnexpectedType() {
        // given
        String printerName = "";
        Class<?> parameterType = Boolean.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("\"input\""));
    }

    @Test
    public void resolveByUnknownPrinterName() {
        // given
        String printerName = "unknown";
        Class<?> parameterType = String.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("\"input\""));
    }

    @Test
    public void resolveByAlias() {
        // given
        String printerName = "two2";
        Class<?> parameterType = String.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("2"));
    }

    @Test
    public void resolve() {
        // given
        String printerName = "two";
        Class<?> parameterType = String.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("2"));
    }

    @Configuration
    public static class TestConfiguration {

        @Bean({"zero", "zero1", "zero2"})
        @Order(1)
        public Printer zero() {
            return new Printer() {

                @Override
                public boolean supports(Class<?> clazz) {
                    return clazz == String.class;
                }

                @Override
                protected String serialize(Object input) {
                    return "0";
                }
            };
        }

        @Bean({"one", "one1", "one2"})
        @Order(0)
        public Printer one() {
            return new Printer() {

                @Override
                public boolean supports(Class<?> clazz) {
                    return clazz == Integer.class;
                }

                @Override
                protected String serialize(Object input) {
                    return "1";
                }
            };
        }

        @Bean({"two", "two1", "two2"})
        @Order(2)
        public Printer two() {
            return new Printer() {

                @Override
                public boolean supports(Class<?> clazz) {
                    return clazz == String.class;
                }

                @Override
                protected String serialize(Object input) {
                    return "2";
                }
            };
        }
    }
}
