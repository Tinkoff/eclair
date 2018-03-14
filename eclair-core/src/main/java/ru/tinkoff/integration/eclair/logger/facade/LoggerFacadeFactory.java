package ru.tinkoff.integration.eclair.logger.facade;

public interface LoggerFacadeFactory {

    LoggerFacade getLoggerFacade(String name);
}
