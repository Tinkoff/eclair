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

package ru.tinkoff.eclair.printer.processor;

import org.junit.Test;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlElementDecl;
import javax.xml.bind.annotation.XmlRegistry;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class JaxbElementWrapperTest {

    @Test
    public void processRootWithCache() {
        // given
        Jaxb2Marshaller marshaller = mock(Jaxb2Marshaller.class);
        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new Root();
        // when
        Object processed = jaxbElementWrapper.process(input);
        jaxbElementWrapper.process(input);
        // then
        verify(marshaller, never()).getContextPath();
        verify(marshaller, never()).getClassesToBeBound();
        assertThat(processed, is(input));
    }

    @Test
    public void processEmptyWithWrapperMethodCache() {
        // given
        Jaxb2Marshaller marshaller = mock(Jaxb2Marshaller.class);
        when(marshaller.getClassesToBeBound()).thenReturn(new Class[]{});
        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new Empty();
        // when
        Object processed = jaxbElementWrapper.process(input);
        jaxbElementWrapper.process(input);
        // then
        verify(marshaller).getContextPath();
        verify(marshaller).getClassesToBeBound();
        assertThat(processed, is(input));
    }

    @Test
    public void processEmptyWithRegistry() {
        // given
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Registry.class);
        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new Empty();
        // when
        Object processed = jaxbElementWrapper.process(input);
        // then
        assertTrue(processed instanceof JAXBElement);
        assertThat(((JAXBElement) processed).getValue(), is(input));
    }

    @Test
    public void processEmptySecondEmptyWithRegistryAndWrapperCache() {
        // given
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(Registry.class);
        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new Empty();
        Object input2 = new SecondEmpty();
        // when
        Object processed = jaxbElementWrapper.process(input);
        Object processed2 = jaxbElementWrapper.process(input2);
        // then
        assertTrue(processed instanceof JAXBElement);
        assertThat(((JAXBElement) processed).getValue(), is(input));

        assertTrue(processed2 instanceof JAXBElement);
        assertThat(((JAXBElement) processed2).getValue(), is(input2));

        Set<Map.Entry<Class<?>, Object>> wrapperCache = jaxbElementWrapper.getWrapperCache().entrySet();
        assertThat(wrapperCache, hasSize(1));
        Map.Entry<Class<?>, Object> wrapper = wrapperCache.iterator().next();
        assertEquals(Registry.class, wrapper.getKey());
        assertThat(wrapper.getValue(), notNullValue());
    }

    @Test
    public void processEmptyRegistry() {
        // given
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(EmptyRegistry.class);

        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new Empty();
        // when
        Object processed = jaxbElementWrapper.process(input);
        // then
        assertThat(processed, is(input));
    }

    @Test
    public void processJaxbElement() {
        // given
        Jaxb2Marshaller marshaller = mock(Jaxb2Marshaller.class);
        JaxbElementWrapper jaxbElementWrapper = new JaxbElementWrapper(marshaller);
        Object input = new JAXBElement<>(new QName("localPart"), Empty.class, new Empty());
        // when
        Object processed = jaxbElementWrapper.process(input);
        // then
        verify(marshaller, never()).getContextPath();
        verify(marshaller, never()).getClassesToBeBound();
        assertThat(processed, is(input));
    }

    @XmlRootElement
    private static class Root {
    }

    private static class Empty {
    }

    private static class SecondEmpty {
    }

    @XmlRegistry
    public static class Registry {

        @SuppressWarnings("unused")
        public Empty empty() {
            return new Empty();
        }

        @SuppressWarnings("unused")
        public SecondEmpty secondEmpty() {
            return new SecondEmpty();
        }

        @XmlElementDecl(name = "name")
        public JAXBElement<Empty> method(Empty parameter) {
            return new JAXBElement<>(new QName("localPart"), Empty.class, parameter);
        }

        @XmlElementDecl(name = "name")
        public JAXBElement<SecondEmpty> method(SecondEmpty parameter) {
            return new JAXBElement<>(new QName("localPart"), SecondEmpty.class, parameter);
        }
    }

    private static class EmptyRegistry {
    }
}
