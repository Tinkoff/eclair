package ru.tinkoff.eclair.logger.collector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ToStringLogOutCollector implements LogOutCollector<String> {

    public static final ToStringLogOutCollector INSTANCE = new ToStringLogOutCollector();

    @Override
    @NotNull
    public String collect(@Nullable String result) {
        return String.valueOf(result);
    }

}
