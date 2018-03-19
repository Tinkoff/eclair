package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.MdcDefinition;

/**
 * @author Viacheslav Klapatniuk
 */
public class MdcDefinitionFactory {

    public static MdcDefinition newInstance(Mdc mdc) {
        return MdcDefinition.builder()
                .key(mdc.key())
                .value(mdc.value())
                .global(mdc.global())
                .build();
    }
}
