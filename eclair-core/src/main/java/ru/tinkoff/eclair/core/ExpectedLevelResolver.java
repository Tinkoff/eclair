package ru.tinkoff.eclair.core;

import org.springframework.boot.logging.LogLevel;
import ru.tinkoff.eclair.definition.LogDefinition;

import java.util.function.Function;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ExpectedLevelResolver implements Function<LogDefinition, LogLevel> {

    private static final ExpectedLevelResolver instance = new ExpectedLevelResolver();

    public static ExpectedLevelResolver getInstance() {
        return instance;
    }

    @Override
    public LogLevel apply(LogDefinition definition) {
        return min(definition.getLevel(), definition.getIfEnabledLevel());
    }

    private LogLevel min(LogLevel level1, LogLevel level2) {
        return level1.ordinal() <= level2.ordinal() ? level1 : level2;
    }
}
