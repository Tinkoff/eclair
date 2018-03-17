package ru.tinkoff.eclair.core;

import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.definition.EventLogDefinition;

import java.util.function.Function;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ExpectedLevelResolver implements Function<EventLogDefinition, LogLevel> {

    private static final ExpectedLevelResolver instance = new ExpectedLevelResolver();

    public static ExpectedLevelResolver getInstance() {
        return instance;
    }

    @Override
    public LogLevel apply(EventLogDefinition definition) {
        if (definition.getLevel().ordinal() <= definition.getIfEnabledLevel().ordinal()) {
            return definition.getLevel();
        }
        return definition.getIfEnabledLevel();
    }
}
