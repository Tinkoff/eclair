package ru.tinkoff.eclair.logger.facade;

import org.slf4j.LoggerFactory;

/**
 * @author Viacheslav Klapatniuk
 */
public class Slf4JLoggerFacadeFactory implements LoggerFacadeFactory {

    @Override
    public LoggerFacade getLoggerFacade(String name) {
        return new Slf4JLoggerFacade(LoggerFactory.getLogger(name));
    }
}
