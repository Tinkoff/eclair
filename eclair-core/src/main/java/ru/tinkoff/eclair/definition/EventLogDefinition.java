package ru.tinkoff.eclair.definition;

import org.springframework.boot.logging.LogLevel;

/**
 * @author Viacheslav Klapatniuk
 */
public interface EventLogDefinition {

    LogLevel getLevel();

    LogLevel getIfEnabledLevel();

    LogLevel getVerboseLevel();
}
