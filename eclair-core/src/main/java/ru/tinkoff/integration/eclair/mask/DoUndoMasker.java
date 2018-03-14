package ru.tinkoff.integration.eclair.mask;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import ru.tinkoff.integration.eclair.format.printer.Printer;

import java.util.List;
import java.util.ListIterator;
import java.util.function.Supplier;

import static java.util.stream.Collectors.toList;

/**
 * TODO: offer more safe implementation
 */
public class DoUndoMasker implements Masker {

    private final MaskRegistry maskRegistry;
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    public DoUndoMasker(MaskRegistry maskRegistry) {
        this.maskRegistry = maskRegistry;
    }

    @Override
    public String mask(Object argument, List<String> maskExpressions, Supplier<Printer> printerSupplier) {
        if (maskExpressions.isEmpty()) {
            return printerSupplier.get().print(argument);
        }
        EvaluationContext evaluationContext = new StandardEvaluationContext(argument);
        List<Object> maskedValues = maskExpressions.stream()
                .map(maskExpression -> getValueAndApplyMask(evaluationContext, maskExpression))
                .collect(toList());
        String printedArgument = printerSupplier.get().print(argument);
        revert(argument, maskExpressions, maskedValues);
        return printedArgument;
    }

    private Object getValueAndApplyMask(EvaluationContext evaluationContext, String maskExpression) {
        Expression expression = expressionParser.parseExpression(maskExpression);
        Class<?> valueType = expression.getValueType(evaluationContext);
        Object mask = maskRegistry.find(valueType);
        Object value = expression.getValue(evaluationContext);
        expression.setValue(evaluationContext, mask);
        return value;
    }

    private void revert(Object argument, List<String> maskExpressions, List<Object> maskedValues) {
        if (maskExpressions.isEmpty()) {
            return;
        }
        EvaluationContext evaluationContext = new StandardEvaluationContext(argument);
        ListIterator<String> maskExpressionsIterator = maskExpressions.listIterator(maskExpressions.size());
        ListIterator<Object> maskedValuesIterator = maskedValues.listIterator(maskedValues.size());
        while (maskExpressionsIterator.hasPrevious()) {
            String maskExpression = maskExpressionsIterator.previous();
            Object maskedValue = maskedValuesIterator.previous();
            Expression expression = expressionParser.parseExpression(maskExpression);
            expression.setValue(evaluationContext, maskedValue);
        }
    }
}
