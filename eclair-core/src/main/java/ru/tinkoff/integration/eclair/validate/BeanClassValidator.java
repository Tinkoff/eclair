package ru.tinkoff.integration.eclair.validate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.integration.eclair.core.AnnotationExtractor;

import java.util.stream.Stream;

@Component
public class BeanClassValidator implements Validator {

    private final BeanMethodValidator beanMethodValidator;

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    @Autowired
    public BeanClassValidator(BeanMethodValidator beanMethodValidator) {
        this.beanMethodValidator = beanMethodValidator;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return annotationExtractor.getCandidateMethods(clazz).stream()
                .anyMatch(method ->
                        annotationExtractor.hasAnyAnnotation(method) ||
                                Stream.of(method.getParameters()).anyMatch(annotationExtractor::hasAnyAnnotation)

                );
    }

    @Override
    public void validate(Object target, Errors errors) {
        annotationExtractor.getCandidateMethods((Class<?>) target)
                .forEach(method -> beanMethodValidator.validate(method, errors));
    }
}
