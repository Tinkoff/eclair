package ru.tinkoff.eclair.core;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import ru.tinkoff.eclair.example.Example;

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
        String string = "1 + 1";
        // when
        String result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("2"));
    }

    @Test
    public void evaluateNull() {
        // given
        String string = "null";
        // when
        String result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateString() {
        // given
        String string = "'string'";
        // when
        String result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("string"));
    }

    @Test
    public void evaluateAsIs() {
        // given
        String string = "string as is";
        // when
        String result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("string as is"));
    }

    @Test
    public void evaluateWithArgument() {
        // given
        String string = "publicField.length()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("6"));
    }

    @Test
    public void evaluateWithArgumentError() {
        // given
        String string = "privateField.length()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("privateField.length()"));
    }

    @Test
    public void evaluateWithArgumentNull() {
        // given
        String string = "null";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithArgumentNullArgument() {
        // given
        String string = "field.length()";
        Argument argument = null;
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithBeanReferencing() {
        // given
        String string = "@string.toString()";
        // when
        String result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("bean string"));
    }

    @Test
    public void evaluateWithArgumentAndBeanReferencing() {
        // given
        String string = "@string.toString()";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("bean string"));
    }

    /**
     * TODO: add to {@link Example}
     * TODO: replace expression by template
     */
    @Test
    public void evaluateToString() {
        // given
        String string = "toString()";
        String string2 = "#this";
        String string3 = "#root";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        String result2 = expressionEvaluator.evaluate(string2, argument);
        String result3 = expressionEvaluator.evaluate(string3, argument);
        // then
        assertThat(result, is("!"));
        assertThat(result2, is("!"));
        assertThat(result3, is("!"));
    }

    @Test
    public void evaluateChangeValue() {
        // given
        String string = "publicField = '123'";
        Argument argument = new Argument();
        // when
        String result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("123"));
        assertThat(argument.getPublicField(), is("123"));
    }

    @SuppressWarnings("unused")
    private static class Argument {

        private final String privateField = "private";

        @Getter
        @Setter
        private String publicField = "public";

        @Override
        public String toString() {
            return "!";
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
