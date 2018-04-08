package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.definition.ParameterMdc;

/**
 * @author Viacheslav Klapatniuk
 */
public class ParameterMdcFactory {

    public static ParameterMdc newInstance(Mdc mdc) {
        return ParameterMdc.builder()
                .key(mdc.key())
                .expressionString(mdc.value())
                .global(mdc.global())
                .build();
    }
}
