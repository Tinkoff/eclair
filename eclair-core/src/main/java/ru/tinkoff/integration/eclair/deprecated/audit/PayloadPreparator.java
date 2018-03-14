package ru.tinkoff.integration.eclair.deprecated.audit;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.util.StdDateFormat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;
import org.springframework.stereotype.Component;
import ru.tinkoff.integration.eclair.deprecated.audit.processor.XPathProcessor;
import ru.tinkoff.integration.eclair.deprecated.audit.wrapper.ClassWrapper;
import ru.tinkoff.integration.eclair.deprecated.audit.wrapper.ObjectFactoryWrapper;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Locale;
import java.util.TimeZone;

import static java.util.Objects.nonNull;

/**
 * TODO: refactor
 */
@Component
class PayloadPreparator {

    private final ClassWrapper classWrapper;
    private final Jaxb2Marshaller marshaller;
    private final ObjectFactoryWrapper objectFactoryWrapper;
    private final XPathProcessor xPathProcessor;
    private final ObjectMapper objectMapper;

    @Autowired
    PayloadPreparator(ClassWrapper classWrapper,
                      Jaxb2Marshaller marshaller,
                      ObjectFactoryWrapper objectFactoryWrapper,
                      XPathProcessor xPathProcessor) {
        this.classWrapper = classWrapper;
        this.marshaller = marshaller;
        this.objectFactoryWrapper = objectFactoryWrapper;
        this.xPathProcessor = xPathProcessor;
        objectMapper = new ObjectMapper().setAnnotationIntrospector(new AuditAnnotationIntrospector());
    }

    String prepare(Object payload, Class<?> wrapper, String[] mask) {
        if (payload instanceof String) {
            return prepareString(payload);
        }
        if (payload instanceof byte[]) {
            return prepareBytes((byte[]) payload);
        }
        /*if (payload instanceof ResponseEntity) {
            Object body = ((ResponseEntity) payload).getBody();
            if (body instanceof byte[]) {
                return prepareBytes((byte[]) body);
            }
        }*/
        try {
            return prepareXml(payload, wrapper, mask);
        } catch (RuntimeException e) {
            return prepareJson(payload);
        }
    }

    private String prepareString(Object payload) {
        return (String) payload;
    }

    private String prepareBytes(byte[] payload) {
        return payload.length == 0 ? "[]" : "[...]";
    }

    private String prepareXml(Object payload, Class<?> wrapper, String[] mask) {
        Object wrappedPayload = wrapIfNecessary(payload, wrapper);
        String xml = marshal(wrappedPayload);
        return mask.length == 0 ? xml : xPathProcessor.process(xml, mask);
    }

    private Object wrapIfNecessary(Object payload, Class<?> wrapper) {
        if (nonNull(wrapper)) {
            return classWrapper.wrap(wrapper, payload);
        }
        if (payload.getClass().getAnnotation(XmlRootElement.class) != null) {
            return payload;
        }
        return objectFactoryWrapper.tryWrapByPayloadClassIteratively(payload);
    }

    private String marshal(Object jaxbObject) {
        StringWriter writer = new StringWriter();
        marshaller.marshal(jaxbObject, new StreamResult(writer));
        return writer.toString();
    }

    private String prepareJson(Object payload) {
        try {
            return objectMapper
                    .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                    .setDateFormat(StdDateFormat.getISO8601Format(TimeZone.getDefault(), Locale.getDefault()))
                    .writeValueAsString(payload);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
