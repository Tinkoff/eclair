package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.definition.ErrorLog;
import ru.tinkoff.eclair.definition.InLog;
import ru.tinkoff.eclair.definition.LogPack;
import ru.tinkoff.eclair.definition.OutLog;

import java.lang.reflect.Method;
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
                                      InLog inLog,
                                      OutLog outLog,
                                      Set<ErrorLog> errorLogs) {
        if (isNull(inLog) && isNull(outLog) && errorLogs.isEmpty()) {
            return null;
        }
        return LogPack.builder()
                .method(method)
                .inLog(inLog)
                .outLog(outLog)
                .errorLogs(errorLogs)
                .build();
    }
}
