/*
 * Copyright 2018 Tinkoff Bank
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.tinkoff.eclair.definition.factory;

import ru.tinkoff.eclair.definition.*;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static java.util.Objects.isNull;

/**
 * @author Vyacheslav Klapatnyuk
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
