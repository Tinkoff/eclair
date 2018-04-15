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

package ru.tinkoff.eclair.core;

import lombok.Getter;
import lombok.Setter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = {
        ExpressionEvaluator.class,
        SpelExpressionParser.class,
        StandardEvaluationContext.class,
        ExpressionEvaluatorTest.TestConfiguration.class
})
public class ExpressionEvaluatorTest {

    @Autowired
    private ExpressionEvaluator expressionEvaluator;
    @Autowired
    private GenericApplicationContext applicationContext;
    @Autowired
    private SpelExpressionParser expressionParser;

    @Test
    public void evaluate() {
        // given
        String string = "1 + 1";
        // when
        Object result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is(2));
    }

    @Test
    public void evaluateNull() {
        // given
        String string = "null";
        // when
        Object result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateString() {
        // given
        String string = "'string'";
        // when
        Object result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("string"));
    }

    @Test
    public void evaluateAsIs() {
        // given
        String string = "string as is";
        // when
        Object result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("string as is"));
    }

    @Test
    public void evaluateWithArgument() {
        // given
        String string = "publicField.length()";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is(6));
    }

    @Test
    public void evaluateWithArgumentError() {
        // given
        String string = "privateField.length()";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("privateField.length()"));
    }

    @Test
    public void evaluateWithArgumentNull() {
        // given
        String string = "null";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithArgumentNullArgument() {
        // given
        String string = "field.length()";
        Argument argument = null;
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, nullValue());
    }

    @Test
    public void evaluateWithBeanReferencing() {
        // given
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(expressionParser, evaluationContext);

        String string = "@string.toString()";
        // when
        Object result = expressionEvaluator.evaluate(string);
        // then
        assertThat(result, is("bean string"));
    }

    @Test
    public void evaluateWithArgumentAndBeanReferencing() {
        // given
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(expressionParser, evaluationContext);

        String string = "@string.toString()";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        // then
        assertThat(result, is("bean string"));
    }

    @Test
    public void evaluateToString() {
        // given
        StandardEvaluationContext evaluationContext = new StandardEvaluationContext();
        evaluationContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        ExpressionEvaluator expressionEvaluator = new ExpressionEvaluator(expressionParser, evaluationContext);

        String string = "toString()";
        String string2 = "#this";
        String string3 = "#root";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
        Object result2 = expressionEvaluator.evaluate(string2, argument);
        Object result3 = expressionEvaluator.evaluate(string3, argument);
        // then
        assertThat(result, is("!"));
        assertThat(result2, is(argument));
        assertThat(result3, is(argument));
    }

    @Test
    public void evaluateChangeValue() {
        // given
        String string = "publicField = '123'";
        Argument argument = new Argument();
        // when
        Object result = expressionEvaluator.evaluate(string, argument);
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
