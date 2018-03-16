package ru.tinkoff.eclair.logger.facade;

import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;
import org.springframework.boot.logging.LogLevel;

import java.util.EnumMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * TODO: test this case
 */
public class JavaLoggerFacade implements LoggerFacade {

    private static final Map<LogLevel, Level> LEVELS = new EnumMap<>(LogLevel.class);

    private final Logger logger;

    static {
        LEVELS.put(LogLevel.TRACE, Level.FINEST);
        LEVELS.put(LogLevel.DEBUG, Level.FINE);
        LEVELS.put(LogLevel.INFO, Level.INFO);
        LEVELS.put(LogLevel.WARN, Level.WARNING);
        LEVELS.put(LogLevel.ERROR, Level.SEVERE);
        LEVELS.put(LogLevel.FATAL, Level.SEVERE);
        LEVELS.put(LogLevel.OFF, Level.OFF);
    }

    JavaLoggerFacade(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void log(LogLevel level, String format, Object... arguments) {
        if (LEVELS.containsKey(level)) {
            FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
            String message = formattingTuple.getMessage();
            Throwable throwable = formattingTuple.getThrowable();
            logger.log(LEVELS.get(level), message, throwable);
            return;
        }
        throw new IllegalArgumentException("Unexpected logging level: " + level);
    }
}
