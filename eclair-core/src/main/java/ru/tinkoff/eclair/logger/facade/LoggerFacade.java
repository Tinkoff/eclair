package ru.tinkoff.eclair.logger.facade;

import org.springframework.boot.logging.LogLevel;

/**
 * @author Viacheslav Klapatniuk
 */
public interface LoggerFacade {

    void log(LogLevel level, String format, Object... arguments);
}
