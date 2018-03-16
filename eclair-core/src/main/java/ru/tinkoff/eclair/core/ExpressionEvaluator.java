package ru.tinkoff.eclair.core;

import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ExpressionEvaluator {

    private static final ExpressionEvaluator instance = new ExpressionEvaluator();

    private final ExpressionParser expressionParser = new SpelExpressionParser();

    private ExpressionEvaluator() {
    }

    public static ExpressionEvaluator getInstance() {
        return instance;
    }

    public String evaluate(String literal) {
        try {
            Object value = expressionParser.parseExpression(literal).getValue();
            return isNull(value) ? null : value.toString();
        } catch (EvaluationException | ParseException e) {
            return literal;
        }
    }

    public String evaluate(String literal, Object argument) {
        if (isNull(argument)) {
            return null;
        }
        try {
            Object value = expressionParser.parseExpression(literal).getValue(new StandardEvaluationContext(argument));
            return isNull(value) ? null : value.toString();
        } catch (EvaluationException | ParseException e) {
            return literal;
        }
    }
}
