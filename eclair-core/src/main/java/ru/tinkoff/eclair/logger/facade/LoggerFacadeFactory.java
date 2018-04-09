package ru.tinkoff.eclair.logger.facade;

/**
 * @author Viacheslav Klapatniuk
 */
public interface LoggerFacadeFactory {

    LoggerFacade getLoggerFacade(String loggerName);
}
