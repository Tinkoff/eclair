package ru.tinkoff.eclair.printer;

import org.springframework.oxm.XmlMappingException;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

/**
 * @author Viacheslav Klapatniuk
 */
public class Jaxb2Printer extends Printer {

    private final Jaxb2Marshaller jaxb2Marshaller;

    public Jaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Override
    protected String serialize(Object input) throws XmlMappingException {
        StringWriter writer = new StringWriter();
        jaxb2Marshaller.marshal(input, new StreamResult(writer));
        return writer.toString();
    }
}
