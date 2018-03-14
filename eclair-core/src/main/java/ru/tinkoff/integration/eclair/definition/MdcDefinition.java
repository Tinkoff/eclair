package ru.tinkoff.integration.eclair.definition;

import lombok.Getter;
import ru.tinkoff.integration.eclair.annotation.Mdc;
import ru.tinkoff.integration.eclair.annotation.Scope;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class MdcDefinition {

    private final String key;
    private final String value;
    private final Scope scope;

    MdcDefinition(Mdc mdc) {
        this.key = mdc.key();
        this.value = mdc.value();
        this.scope = mdc.scope();
    }
}
