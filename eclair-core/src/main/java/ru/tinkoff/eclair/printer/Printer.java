package ru.tinkoff.eclair.printer;

import ru.tinkoff.eclair.printer.processor.PrinterPostProcessor;
import ru.tinkoff.eclair.printer.processor.PrinterPreProcessor;

import java.util.ArrayList;
import java.util.List;

/**
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
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
