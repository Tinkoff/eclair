package ru.tinkoff.eclair.core;

import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.EvaluationException;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.ParseException;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public final class ExpressionEvaluator {

    private final ExpressionParser expressionParser = new SpelExpressionParser();
    private final BeanFactoryResolver beanFactoryResolver;
    private final StandardEvaluationContext defaultEvaluationContext;

    public ExpressionEvaluator(ApplicationContext applicationContext) {
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
        this.defaultEvaluationContext = new StandardEvaluationContext();
        this.defaultEvaluationContext.setBeanResolver(beanFactoryResolver);
    }

    public String evaluate(String literal) {
        return getValue(literal, defaultEvaluationContext);
    }

    public String evaluate(String literal, Object argument) {
        if (isNull(argument)) {
            return null;
        }
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(argument);
        evaluationContext.setBeanResolver(beanFactoryResolver);
        return getValue(literal, evaluationContext);
    }

    private String getValue(String literal, EvaluationContext evaluationContext) {
        try {
            Object value = expressionParser.parseExpression(literal).getValue(evaluationContext);
            return isNull(value) ? null : value.toString();
        } catch (EvaluationException | ParseException e) {
            return literal;
        }
    }
}
