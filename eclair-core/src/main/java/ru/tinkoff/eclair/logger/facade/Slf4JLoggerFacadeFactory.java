package ru.tinkoff.eclair.logger.facade;

/**
 * @author Viacheslav Klapatniuk
 */
public class Slf4JLoggerFacadeFactory implements LoggerFacadeFactory {

    @Override
    public LoggerFacade getLoggerFacade(String name) {
        return new Slf4JLoggerFacade(org.slf4j.LoggerFactory.getLogger(name));
    }
}
