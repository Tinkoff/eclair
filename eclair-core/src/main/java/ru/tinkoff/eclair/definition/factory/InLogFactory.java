package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.core.AnnotationAttribute;
import ru.tinkoff.eclair.definition.ArgLog;
import ru.tinkoff.eclair.definition.InLog;

import java.util.List;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
public class InLogFactory {

    /**
     * @param logIn may be {@code null}
     * @return Instantiated {@link InLog} or {@code null}
     */
    public static InLog newInstance(Log.in logIn, List<ArgLog> argLogs) {
        if (isNull(logIn)) {
            return null;
        }
        return InLog.builder()
                .level(AnnotationAttribute.LEVEL.extract(logIn))
                .ifEnabledLevel(logIn.ifEnabled())
                .verboseLevel(logIn.verbose())
                .argLogs(argLogs)
                .build();
    }
}
