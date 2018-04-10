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

package ru.tinkoff.eclair.printer;

import ru.tinkoff.eclair.printer.processor.PrinterPostProcessor;
import ru.tinkoff.eclair.printer.processor.PrinterPreProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: add tests
 *
 * @author Vyacheslav Klapatnyuk
 */
public abstract class Printer {

    private final List<PrinterPreProcessor> preProcessors = new ArrayList<>();
    private final List<PrinterPostProcessor> postProcessors = new ArrayList<>();

    public boolean supports(Class<?> clazz) {
        return true;
    }

    public Printer addPreProcessor(PrinterPreProcessor preProcessor) {
        preProcessors.add(preProcessor);
        return this;
    }

    public Printer addPostProcessor(PrinterPostProcessor postProcessor) {
        postProcessors.add(postProcessor);
        return this;
    }

    /**
     * @param input never {@code null}
     */
    public String print(Object input) {
        for (PrinterPreProcessor preProcessor : preProcessors) {
            input = preProcessor.process(input);
        }
        String string = serialize(input);
        for (PrinterPostProcessor postProcessor : postProcessors) {
            string = postProcessor.process(string);
        }
        return string;
    }

    protected abstract String serialize(Object input);
}
