package ru.tinkoff.eclair.definition;

import lombok.Getter;
import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;

import java.util.List;

import static java.util.Collections.unmodifiableList;
import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class InLogDefinition {

    private final LogLevel level;
    private final LogLevel ifEnabledLevel;
    private final LogLevel verboseLevel;
    private final List<ArgLogDefinition> argLogDefinitions;

    private InLogDefinition(Log.in logIn, List<ArgLogDefinition> argLogDefinitions) {
        this.level = AnnotationAttribute.LEVEL.extract(logIn);
        this.ifEnabledLevel = logIn.ifEnabled();
        this.verboseLevel = logIn.verbose();
        this.argLogDefinitions = unmodifiableList(argLogDefinitions);
    }

    /**
     * @param logIn may be {@code null}
     * @return Instantiated {@link InLogDefinition} or {@code null}
     */
    public static InLogDefinition newInstance(Log.in logIn, List<ArgLogDefinition> argLogDefinitions) {
        return isNull(logIn) ? null : new InLogDefinition(logIn, argLogDefinitions);
    }
}
