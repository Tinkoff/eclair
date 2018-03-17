package ru.tinkoff.eclair.definition;

import org.springframework.boot.logging.LogLevel;

/**
 * @author Viacheslav Klapatniuk
 */
public interface LogDefinition {

    LogLevel getLevel();

    LogLevel getIfEnabledLevel();

    LogLevel getVerboseLevel();
}
