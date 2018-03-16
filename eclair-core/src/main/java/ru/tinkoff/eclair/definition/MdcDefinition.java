package ru.tinkoff.eclair.definition;

import lombok.Getter;
import ru.tinkoff.eclair.annotation.Mdc;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class MdcDefinition {

    private final String key;
    private final String value;
    private final boolean global;

    MdcDefinition(Mdc mdc) {
        this.key = mdc.key();
        this.value = mdc.value();
        this.global = mdc.global();
    }
}
