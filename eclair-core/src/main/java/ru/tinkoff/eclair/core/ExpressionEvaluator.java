package ru.tinkoff.eclair.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.*;
import org.springframework.expression.spel.SpelCompilerMode;
import org.springframework.expression.spel.SpelParserConfiguration;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import static java.util.Objects.isNull;

/**
 * @author Viacheslav Klapatniuk
 */
@Component
public final class ExpressionEvaluator {

    private static final Logger logger = LoggerFactory.getLogger(ExpressionEvaluator.class);

    private final ExpressionParser expressionParser;
    private final BeanFactoryResolver beanFactoryResolver;
    // TODO: config 'context' outside
    private final StandardEvaluationContext defaultEvaluationContext;

    public ExpressionEvaluator(ApplicationContext applicationContext) {
        SpelParserConfiguration configuration = new SpelParserConfiguration(SpelCompilerMode.IMMEDIATE, null);
        this.expressionParser = new SpelExpressionParser(configuration);
        this.beanFactoryResolver = new BeanFactoryResolver(applicationContext);
        this.defaultEvaluationContext = new StandardEvaluationContext();
        this.defaultEvaluationContext.setBeanResolver(beanFactoryResolver);
    }

    public String evaluate(String expressionString) {
        return getValue(expressionString, defaultEvaluationContext);
    }

    public String evaluate(String expressionString, Object argument) {
        if (isNull(argument)) {
            return null;
        }
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext(argument);
        evaluationContext.setBeanResolver(beanFactoryResolver);
        return getValue(expressionString, evaluationContext);
    }

    private String getValue(String expressionString, EvaluationContext evaluationContext) {
        // TODO: compile and cache compiled SpEL expression inside 'log definition'
        Expression expression;
        try {
            expression = expressionParser.parseExpression(expressionString);
        } catch (ParseException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Expression string could not be parsed: {}", expressionString, e);
            }
            return expressionString;
        }
        try {
            Object value = expression.getValue(evaluationContext);
            return isNull(value) ? null : value.toString();
        } catch (EvaluationException e) {
            if (logger.isDebugEnabled()) {
                logger.error("Expression string could not be evaluated: {}", expressionString, e);
            }
            return expressionString;
        }
    }
}
