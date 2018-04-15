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

package ru.tinkoff.eclair.example;

import ch.qos.logback.classic.PatternLayout;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Viacheslav Klapatnyuk
 */
abstract class SampleBuilder {

    private static final Pattern stackTraceElement = Pattern.compile("^(\\s*at \\S+\\(\\S+\\.java:)\\d+(\\))$");
    private static final Pattern uuid = Pattern.compile("(?i)^(.*)[\\da-z]{8}-[\\da-z]{4}-[\\da-z]{4}-[\\da-z]{4}-[\\da-z]{12}(.*)$");

    private static final String STACK_TRACE_ELEMENT_REPLACEMENT = "0";
    private static final String UUID_REPLACEMENT = "c118fe51-a7da-48ec-b53a-a6a5871d9ae6";

    PatternLayout patternLayout;

    void setPatternLayout(PatternLayout patternLayout) {
        this.patternLayout = patternLayout;
    }

    /**
     * Replace line numbers by fixed {@link ExampleTableBuilder#STACK_TRACE_ELEMENT_REPLACEMENT}.
     * Before:
     * `	at ru.tinkoff.eclair.example.Example.error(Example.java:74)`
     * `	at ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:250)`
     * After:
     * `	at ru.tinkoff.eclair.example.Example.error(Example.java:0)`
     * `	at ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`
     */
    String maskStackTraceElement(String line) {
        Matcher matcher = stackTraceElement.matcher(line);
        return matcher.matches() ? matcher.replaceAll("$1" + STACK_TRACE_ELEMENT_REPLACEMENT + "$2") : line;
    }

    /**
     * Replace UUID by fixed {@link ExampleTableBuilder#UUID_REPLACEMENT}.
     * Before:
     * `DEBUG [staticMethod=79383d64-3c70-4f7d-ae22-ad0d357bcbca] r.t.eclair.example.Example.mdc >`
     * After:
     * `DEBUG [staticMethod=c118fe51-a7da-48ec-b53a-a6a5871d9ae6] r.t.eclair.example.Example.mdc >`
     */
    String maskUuid(String line) {
        Matcher matcher = uuid.matcher(line);
        return matcher.matches() ? matcher.replaceAll("$1" + UUID_REPLACEMENT + "$2") : line;
    }
}
