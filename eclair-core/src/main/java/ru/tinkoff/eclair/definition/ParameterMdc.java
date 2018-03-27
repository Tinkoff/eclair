package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
@Builder
public class ParameterMdc {

    @NonNull
    private String key;

    @NonNull
    private String value;

    private boolean global;
}
