package ru.tinkoff.eclair.logger.collector;

import java.util.StringJoiner;

public class StringJoinerLogInCollector implements LogInCollector<String> {

    private final StringJoiner joiner;

    public StringJoinerLogInCollector(StringJoiner joiner) {
        this.joiner = joiner;
    }

    public StringJoinerLogInCollector() {
        this(new StringJoiner(", "));
    }

    @Override
    public void addParameter(String parameterName, String value) {
        if (parameterName == null) {
            joiner.add(value);
        } else {
            joiner.add(parameterName + "=" + value);
        }
    }

    @Override
    public String collect() {
        return joiner.toString();
    }

}
