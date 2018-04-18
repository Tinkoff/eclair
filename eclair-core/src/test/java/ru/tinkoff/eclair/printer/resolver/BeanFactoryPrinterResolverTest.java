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

package ru.tinkoff.eclair.printer.resolver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.core.BeanFactoryHelperTest;
import ru.tinkoff.eclair.printer.Printer;

import java.util.List;

import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = BeanFactoryHelperTest.TestConfiguration.class)
public class BeanFactoryPrinterResolverTest {

    @Autowired
    private List<Printer> printers;
    @Autowired
    private ApplicationContext applicationContext;

    private BeanFactoryPrinterResolver printerResolver;

    @Before
    public void init() {
        printerResolver = new BeanFactoryPrinterResolver(applicationContext, printers);
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
        assertNull(printer);
    }

    @Test
    public void resolveByUnknownPrinterName() {
        // given
        String printerName = "unknown";
        Class<?> parameterType = String.class;
        // when
        Printer printer = printerResolver.resolve(printerName, parameterType);
        // then
        assertThat(printer.print("input"), is("0"));
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

    @Test
    public void resolveByPrinterNameAndParameterTypes() {
        // given
        String printerName = "two";
        Class<?>[] parameterTypes = new Class[]{String.class, Integer.class};
        // when
        List<Printer> printers = printerResolver.resolve(printerName, parameterTypes);
        // then
        assertThat(printers, hasSize(2));
        assertThat(printers.get(0).print("input"), is("2"));
        assertThat(printers.get(1).print("input"), is("1"));
    }

    @Test
    public void resolveByParameterTypes() {
        // given
        String printerName = "";
        Class<?>[] parameterTypes = new Class[]{String.class, Integer.class};
        // when
        List<Printer> printers = printerResolver.resolve(printerName, parameterTypes);
        // then
        assertThat(printers, hasSize(2));
        assertThat(printers.get(0).print("input"), is("0"));
        assertThat(printers.get(1).print("input"), is("1"));
    }

    @Test
    public void resolveByParameterTypesDefault() {
        // given
        String printerName = "";
        Class<?>[] parameterTypes = new Class[]{String.class, Integer.class, Throwable.class};
        // when
        List<Printer> printers = printerResolver.resolve(printerName, parameterTypes);
        // then
        assertThat(printers, hasSize(3));
        assertThat(printers.get(0).print("input"), is("0"));
        assertThat(printers.get(1).print("input"), is("1"));
        assertNull(printers.get(2));
    }
}
