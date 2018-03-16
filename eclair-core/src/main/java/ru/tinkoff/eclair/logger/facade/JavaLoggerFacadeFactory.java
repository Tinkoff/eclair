package ru.tinkoff.eclair.logger.facade;

import java.util.logging.Logger;

/**
 * @author Viacheslav Klapatniuk
 */
public class JavaLoggerFacadeFactory implements LoggerFacadeFactory {

    @Override
    public LoggerFacade getLoggerFacade(String name) {
        return new JavaLoggerFacade(Logger.getLogger(name));
    }
}
