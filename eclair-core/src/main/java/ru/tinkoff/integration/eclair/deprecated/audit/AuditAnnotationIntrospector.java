package ru.tinkoff.integration.eclair.deprecated.audit;

import com.fasterxml.jackson.databind.introspect.AnnotatedMember;
import com.fasterxml.jackson.databind.introspect.JacksonAnnotationIntrospector;

/**
 * TODO: refactor
 */
public class AuditAnnotationIntrospector extends JacksonAnnotationIntrospector {

    @Override
    public boolean hasIgnoreMarker(AnnotatedMember m) {
//        return nonNull(m.getAnnotation(Skip.class)) || super.hasIgnoreMarker(m);
        return false;
    }
}
