package ru.tinkoff.eclair.logger.facade;

/**
 * @author Viacheslav Klapatniuk
 */
public class JavaLoggerFacadeFactory implements LoggerFacadeFactory {

    @Override
    public LoggerFacade getLoggerFacade(String name) {
        return new JavaLoggerFacade(java.util.logging.Logger.getLogger(name));
    }
}
