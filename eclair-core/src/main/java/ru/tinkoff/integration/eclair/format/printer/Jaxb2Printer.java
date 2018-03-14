package ru.tinkoff.integration.eclair.format.printer;

import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;

import static java.util.Objects.isNull;
import static org.springframework.core.annotation.AnnotationUtils.findAnnotation;

/**
 * @author Viacheslav Klapatniuk
 * xml -> throws
 */
public class Jaxb2Printer implements Printer {

    private final JaxbElementWrapper jaxbElementWrapper = JaxbElementWrapper.getInstance();
    private final Jaxb2Marshaller jaxb2Marshaller;

    public Jaxb2Printer(Jaxb2Marshaller jaxb2Marshaller) {
        this.jaxb2Marshaller = jaxb2Marshaller;
    }

    @Override
    public String print(Object input) {
        if (isNull(findAnnotation(input.getClass(), XmlRootElement.class))) {
            input = jaxbElementWrapper.wrap(jaxb2Marshaller, input);
        }
        StringWriter writer = new StringWriter();
        jaxb2Marshaller.marshal(input, new StreamResult(writer));
        return writer.toString();
    }
}
