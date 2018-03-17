package ru.tinkoff.eclair.validate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import ru.tinkoff.eclair.core.AnnotationExtractor;

import java.util.stream.Stream;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
@RequiredArgsConstructor
public class BeanClassValidator implements Validator {

    private final BeanMethodValidator beanMethodValidator;

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

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
