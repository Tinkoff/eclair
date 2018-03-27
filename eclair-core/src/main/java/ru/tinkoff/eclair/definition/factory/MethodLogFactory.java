package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.definition.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
public class MethodLogFactory {

    /**
     * @param inLog  may be {@code null}
     * @param outLog may be {@code null}
     * @return Instantiated {@link InLog} or {@code null}
     */
    public static MethodLog newInstance(Method method,
                                        List<String> parameterNames,
                                        InLog inLog,
                                        List<ParameterLog> parameterLogs,
                                        OutLog outLog,
                                        Set<ErrorLog> errorLogs) {
        if (isNull(inLog) && parameterLogs.stream().allMatch(Objects::isNull) && isNull(outLog) && errorLogs.isEmpty()) {
            return null;
        }
        return MethodLog.builder()
                .method(method)
                .parameterNames(parameterNames)
                .inLog(inLog)
                .parameterLogs(parameterLogs)
                .outLog(outLog)
                .errorLogs(errorLogs)
                .build();
    }
}
