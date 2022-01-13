package ru.tinkoff.eclair.logger.collector;

import org.jetbrains.annotations.Nullable;

public class ToStringLogOutCollector implements LogOutCollector<String> {

    public static final ToStringLogOutCollector INSTANCE = new ToStringLogOutCollector();

    @Override
    public String collect(@Nullable String result) {
        return String.valueOf(result);
    }

}
