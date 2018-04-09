package ru.tinkoff.eclair.logger.facade;

import java.util.logging.Logger;

/**
 * @author Viacheslav Klapatniuk
 */
public class JavaLoggerFacadeFactory implements LoggerFacadeFactory {

    @Override
    public LoggerFacade getLoggerFacade(String loggerName) {
        return new JavaLoggerFacade(Logger.getLogger(loggerName));
    }
}
