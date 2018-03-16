package ru.tinkoff.eclair.logger;

import org.springframework.boot.logging.LogLevel;

/**
 * @author Viacheslav Klapatniuk
 */
public abstract class LevelInsensitiveLogger extends EclairLogger {

    @Override
    public boolean isLevelEnabled(String loggerName, LogLevel expectedLevel) {
        return true;
    }
}
