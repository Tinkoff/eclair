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

import org.springframework.expression.*;
import org.springframework.expression.common.LiteralExpression;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.util.Objects.isNull;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class ExpressionEvaluator {

    private static final ParserContext parserContext = null;

    private final ExpressionParser expressionParser;
    private final EvaluationContext evaluationContext;

    private final Map<String, Expression> expressionCache = new ConcurrentHashMap<>();

    public ExpressionEvaluator(ExpressionParser expressionParser,
                               EvaluationContext evaluationContext) {
        this.expressionParser = expressionParser;
        this.evaluationContext = evaluationContext;
    }

    public Object evaluate(String expressionString) {
        try {
            return expressionCache.computeIfAbsent(expressionString, this::parse).getValue(evaluationContext);
        } catch (EvaluationException e) {
            return expressionString;
        }
    }

    public Object evaluate(String expressionString, Object rootObject) {
        if (isNull(rootObject)) {
            return null;
        }
        try {
            return expressionCache.computeIfAbsent(expressionString, this::parse).getValue(evaluationContext, rootObject);
        } catch (EvaluationException e) {
            return expressionString;
        }
    }

    private Expression parse(String expressionString) {
        try {
            return expressionParser.parseExpression(expressionString, parserContext);
        } catch (ParseException e) {
            return new LiteralExpression(expressionString);
        }
    }
}
