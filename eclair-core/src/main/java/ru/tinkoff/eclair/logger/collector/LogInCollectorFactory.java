package ru.tinkoff.eclair.logger.collector;

public interface LogInCollectorFactory<T> {

    LogInCollector<T> create();

}
