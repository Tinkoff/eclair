package ru.tinkoff.eclair.definition;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.Singular;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
@Builder
public class MdcPack {

    @NonNull
    private Method method;
    @NonNull
    @Singular
    private Set<MdcDefinition> methodDefinitions;
    @NonNull
    @Singular
    private List<Set<MdcDefinition>> parameterDefinitions;
}
