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
public class LogPackFactory {

    /**
     * @param inLog  may be {@code null}
     * @param outLog may be {@code null}
     * @return Instantiated {@link InLog} or {@code null}
     */
    public static LogPack newInstance(Method method,
                                      List<String> parameterNames,
                                      InLog inLog,
                                      List<ArgLog> argLogs,
                                      OutLog outLog,
                                      Set<ErrorLog> errorLogs) {
        if (isNull(inLog) && argLogs.stream().allMatch(Objects::isNull) && isNull(outLog) && errorLogs.isEmpty()) {
            return null;
        }
        return LogPack.builder()
                .method(method)
                .parameterNames(parameterNames)
                .inLog(inLog)
                .argLogs(argLogs)
                .outLog(outLog)
                .errorLogs(errorLogs)
                .build();
    }
}
