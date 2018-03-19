package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
@Builder
public class MdcDefinition {

    @NonNull
    private String key;
    @NonNull
    private String value;
    @NonNull
    private boolean global;
}
