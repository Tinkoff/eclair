package ru.tinkoff.eclair.logger.collector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LogOutCollector<T> {

    @NotNull
    T collect(@Nullable String result);

}
