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

package ru.tinkoff.eclair.printer;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.springframework.oxm.MarshallingFailureException;
import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.*;
import javax.xml.namespace.QName;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

/**
 * @author Viacheslav Klapatniuk
 */
public class Jaxb2PrinterTest {

    @Test
    public void serializeRoot() {
        // given
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Root.class);
        Jaxb2Printer jaxb2Printer = new Jaxb2Printer(jaxb2Marshaller);
        Root root = new Root();
        root.setValue("value");
        // when
        String xml = jaxb2Printer.serialize(root);
        // then
        assertThat(xml, is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><someName><value>value</value></someName>"));
    }

    @Test(expected = XmlMappingException.class)
    public void serializeXmlNotRoot() {
        // given
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Root.class);
        Jaxb2Printer jaxb2Printer = new Jaxb2Printer(jaxb2Marshaller);
        NotRoot notRoot = new NotRoot();
        notRoot.setValue("value");
        // when
        jaxb2Printer.serialize(notRoot);
        // then expected exception
    }

    @Test(expected = MarshallingFailureException.class)
    public void serializeException() {
        // given
        Jaxb2Marshaller jaxb2Marshaller = mock(Jaxb2Marshaller.class);
        doThrow(new MarshallingFailureException("")).when(jaxb2Marshaller).marshal(any(), any());
        Jaxb2Printer jaxb2Printer = new Jaxb2Printer(jaxb2Marshaller);
        Root root = new Root();
        root.setValue("value");
        // when
        jaxb2Printer.serialize(root);
        // then expected exception
    }

    @Test(expected = MarshallingFailureException.class)
    public void serializeXmlEmpty() {
        // given
        Jaxb2Marshaller jaxb2Marshaller = mock(Jaxb2Marshaller.class);
        doThrow(new MarshallingFailureException("")).when(jaxb2Marshaller).marshal(any(), any());
        Jaxb2Printer jaxb2Printer = new Jaxb2Printer(jaxb2Marshaller);
        Empty empty = new Empty();
        empty.setValue("value");
        // when
        jaxb2Printer.serialize(empty);
        // then expected exception
    }

    @Test
    public void serializeJaxbElement() {
        // given
        Jaxb2Marshaller jaxb2Marshaller = new Jaxb2Marshaller();
        jaxb2Marshaller.setClassesToBeBound(Empty.class);
        Jaxb2Printer jaxb2Printer = new Jaxb2Printer(jaxb2Marshaller);
        Empty empty = new Empty();
        empty.setValue("value");
        JAXBElement<Empty> jaxbElement = new JAXBElement<>(new QName("localPart"), Empty.class, empty);
        // when
        String xml = jaxb2Printer.serialize(jaxbElement);
        // then
        assertThat(xml, is("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><localPart><value>value</value></localPart>"));
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = "value")
    @XmlRootElement(name = "someName")
    public static class Root {

        @XmlElement
        @Getter
        @Setter
        private String value;
    }

    @XmlAccessorType(XmlAccessType.FIELD)
    @XmlType(name = "", propOrder = "value")
    public static class NotRoot {

        @XmlElement
        @Getter
        @Setter
        private String value;
    }

    public static class Empty {

        @Getter
        @Setter
        private String value;
    }
}
