package ru.tinkoff.eclair.core;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ExpressionEvaluator.class,
        ExpressionEvaluatorTest.TestConfiguration.class
})
public class ExpressionEvaluatorTest {

    @Autowired
    private ExpressionEvaluator expressionEvaluator;

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
        String literal = "publicField.length()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, is("6"));
    }

    @Test
    public void evaluateWithArgumentError() {
        // given
        String literal = "privateField.length()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, is("privateField.length()"));
    }

    @Test
    public void evaluateWithArgumentNull() {
        // given
        String literal = "null";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithArgumentNullArgument() {
        // given
        String literal = "field.length()";
        Argument argument = null;
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithBeanReferencing() {
        // given
        String literal = "@string.toString()";
        // when
        String result = expressionEvaluator.evaluate(literal);
        // then
        assertThat(result, is("bean string"));
    }

    @Test
    public void evaluateWithArgumentAndBeanReferencing() {
        // given
        String literal = "@string.toString()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(literal, argument);
        // then
        assertThat(result, is("bean string"));
    }

    @SuppressWarnings("unused")
    private static class Argument {

        private final String publicField = "public";
        private final String privateField = "private";

        public String getPublicField() {
            return publicField;
        }
    }

    @Configuration
    static class TestConfiguration {

        @Bean
        public String string() {
            return "bean string";
        }
    }
}
