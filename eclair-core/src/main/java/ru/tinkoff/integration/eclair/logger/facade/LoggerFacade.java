package ru.tinkoff.integration.eclair.logger.facade;

import org.springframework.boot.logging.LogLevel;

public interface LoggerFacade {

    void log(LogLevel level, String format, Object... arguments);
}
