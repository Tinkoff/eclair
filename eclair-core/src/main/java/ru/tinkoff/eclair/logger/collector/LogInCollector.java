package ru.tinkoff.eclair.logger.collector;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface LogInCollector<T> {

    void addParameter(@Nullable String parameterName, @Nullable String value);

    @NotNull
    T collect();

}
