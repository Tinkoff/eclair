package ru.tinkoff.eclair.logger.facade;

import org.slf4j.Logger;
import org.springframework.boot.logging.LogLevel;

/**
 * @author Viacheslav Klapatniuk
 */
public class Slf4JLoggerFacade implements LoggerFacade {

    private final Logger logger;

    public Slf4JLoggerFacade(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        switch (level) {
            case OFF:
                break;
            case FATAL:
            case ERROR:
                logger.error(format, arguments);
                break;
            case WARN:
                logger.warn(format, arguments);
                break;
            case INFO:
                logger.info(format, arguments);
                break;
            case DEBUG:
                logger.debug(format, arguments);
                break;
            case TRACE:
                logger.trace(format, arguments);
                break;
            default:
                throw new IllegalArgumentException("Unexpected logging level: " + level);
        }
    }
}
