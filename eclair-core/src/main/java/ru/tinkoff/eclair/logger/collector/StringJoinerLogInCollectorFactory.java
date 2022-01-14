package ru.tinkoff.eclair.logger.collector;

public class StringJoinerLogInCollectorFactory implements LogInCollectorFactory<String> {

    public static final StringJoinerLogInCollectorFactory INSTANCE = new StringJoinerLogInCollectorFactory();

    @Override
    public LogInCollector<String> create() {
        return new StringJoinerLogInCollector();
    }

}
