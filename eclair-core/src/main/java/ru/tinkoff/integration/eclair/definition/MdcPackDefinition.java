package ru.tinkoff.integration.eclair.definition;

import lombok.Getter;
import ru.tinkoff.integration.eclair.annotation.Mdc;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableSet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * @author Viacheslav Klapatniuk
 */
@Getter
public class MdcPackDefinition {

    private final Method method;
    private final Set<MdcDefinition> methodDefinitions;
    private final List<Set<MdcDefinition>> argumentDefinitions;

    private MdcPackDefinition(Method method,
                              Set<Mdc> methodMdcs,
                              List<Set<Mdc>> argumentMdcs) {
        this.method = method;
        this.methodDefinitions = unmodifiableSet(methodMdcs.stream().map(MdcDefinition::new).collect(toSet()));
        this.argumentDefinitions = unmodifiableList(argumentMdcs.stream()
                .map(mdcs -> unmodifiableSet(mdcs.stream().map(MdcDefinition::new).collect(toSet())))
                .collect(toList()));
    }

    public static MdcPackDefinition newInstance(Method method, Set<Mdc> methodMdcs, List<Set<Mdc>> argumentMdcs) {
        return methodMdcs.isEmpty() && argumentMdcs.stream().allMatch(Collection::isEmpty) ?
                null : new MdcPackDefinition(method, methodMdcs, argumentMdcs);
    }
}
