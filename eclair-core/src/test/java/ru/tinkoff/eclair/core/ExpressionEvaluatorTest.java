package ru.tinkoff.eclair.core;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class ExpressionEvaluatorTest {

    private final ExpressionEvaluator expressionEvaluator = ExpressionEvaluator.getInstance();

    @Test
    public void evaluate() {
        // given
        String literal = "1 + 1";
        // when
        String result = expressionEvaluator.evaluate(literal);
        // then
        assertThat(result, is("2"));
    }

    @Test
    public void evaluateNull() {
        // given
        String literal = "null";
        // when
        String result = expressionEvaluator.evaluate(literal);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateString() {
        // given
        String literal = "'string'";
        // when
        String result = expressionEvaluator.evaluate(literal);
        // then
        assertThat(result, is("string"));
    }

    @Test
    public void evaluateAsIs() {
        // given
        String literal = "string as is";
        // when
        String result = expressionEvaluator.evaluate(literal);
        // then
        assertThat(result, is("string as is"));
    }

    @Test
    public void evaluateWithArgument() {
        // given
        Argument argument = new Argument();
        String literal = "publicField.length()";
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, is("6"));
    }

    @Test
    public void evaluateWithArgumentError() {
        // given
        Argument argument = new Argument();
        String literal = "privateField.length()";
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, is("privateField.length()"));
    }

    @Test
    public void evaluateWithArgumentNull() {
        // given
        Argument argument = new Argument();
        String literal = "null";
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithArgumentNullArgument() {
        // given
        Argument argument = null;
        String literal = "field.length()";
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, nullValue());
    }

    @SuppressWarnings("unused")
    private static class Argument {

        private final String publicField = "public";
        private final String privateField = "private";

        public String getPublicField() {
            return publicField;
        }
    }
}
