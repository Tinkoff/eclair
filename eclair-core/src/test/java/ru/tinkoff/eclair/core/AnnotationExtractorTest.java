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

import org.junit.Test;
import org.springframework.core.annotation.AnnotationUtils;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Logs;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.annotation.Mdcs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class AnnotationExtractorTest {

    private final AnnotationExtractor annotationExtractor = AnnotationExtractor.getInstance();

    @Test
    public void getCandidateMethodsFromObject() {
        // given
        Class<?> clazz = Object.class;
        // when
        Set<Method> methods = annotationExtractor.getCandidateMethods(clazz);
        // then
        assertThat(methods, is(empty()));
    }

    @Test
    public void getCandidateMethodsWithInheritanceAndGeneric() throws NoSuchMethodException {
        // given
        Method childMethod = Child.class.getMethod("method", NullPointerException.class);
        Method parentMethod = Parent.class.getDeclaredMethod("parentMethod");
        Method interfaceMethod = Interface.class.getMethod("interfaceMethod");
        // when
        Set<Method> methods = annotationExtractor.getCandidateMethods(Child.class);
        // then
        assertThat(methods, hasSize(3));
        assertThat(methods, containsInAnyOrder(childMethod, parentMethod, interfaceMethod));
    }

    @SuppressWarnings("unused")
    private interface Interface<T extends Throwable> {

        default void interfaceMethod() {
        }

        default void method(T input) {
        }
    }

    @SuppressWarnings("unused")
    private static class Parent<T extends RuntimeException> implements Interface<T> {

        void parentMethod() {
        }

        @Override
        public void method(T input) {
        }
    }

    private static class Child extends Parent<NullPointerException> {

        @Override
        public void method(NullPointerException input) {
        }
    }

    @Test
    public void hasAnyAnnotationDetectsLogs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logs");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLog() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("log", String.class);
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogIns() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logIns");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogIn() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logIn");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogOuts() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logOuts");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogOut() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logOut");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogErrors() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logErrors");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogError() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("logError");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsMdcs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("mdcs");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsMdc() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("mdc");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterLogs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("parameterLogs", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterLog() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("parameterLog", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterMdcs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("parameterMdcs", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterMdc() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("parameterMdc", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationNotDetectsLogOverParameter() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("log", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean parameterHas = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertFalse(parameterHas);
    }

    @Test
    public void hasAnyAnnotationNotDetectsParameterLogOverMethod() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("parameterLog", String.class);
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertFalse(has);
    }

    @Test
    public void hasAnyAnnotationDetectsNothing() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getDeclaredMethod("none", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean methodHas = annotationExtractor.hasAnyAnnotation(method);
        boolean parameterHas = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertFalse(methodHas);
        assertFalse(parameterHas);
    }

    @SuppressWarnings("unused")
    private static class ClassWithAnnotatedMethods {

        @Logs(@Log)
        void logs() {
        }

        @Log
        void log(String input) {
        }

        @Log.ins(@Log.in)
        void logIns() {
        }

        @Log.in
        void logIn() {
        }

        @Log.outs(@Log.out)
        void logOuts() {
        }

        @Log.out
        void logOut() {
        }

        @Log.errors(@Log.error)
        void logErrors() {
        }

        @Log.error
        void logError() {
        }

        @Mdcs(@Mdc)
        void mdcs() {
        }

        @Mdc
        void mdc() {
        }

        void parameterLogs(@Logs(@Log) String input) {
        }

        void parameterLog(@Log String input) {
        }

        void parameterMdcs(@Mdcs(@Mdc) String input) {
        }

        void parameterMdc(@Mdc String input) {
        }

        void none(String input) {
        }
    }

    @Test
    public void getLogs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        Set<Log> logs = annotationExtractor.getLogs(loggedOverriddenMethod);
        Set<Log> bridgedLogs = annotationExtractor.getLogs(overriddenMethod);
        Set<Log> overriddenLogIns = annotationExtractor.getLogs(overriddenNotGenericMethod);
        // then
        assertThat(logs, hasSize(1));
        assertThat(logs.iterator().next().level(), is(DEBUG));

        assertThat(bridgedLogs, hasSize(1));
        assertThat(bridgedLogs.iterator().next().level(), is(INFO));

        assertThat(overriddenLogIns, hasSize(1));
        assertThat(overriddenLogIns.iterator().next().level(), is(INFO));
    }

    @Test
    public void getLogIns() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        Set<Log.in> logIns = annotationExtractor.getLogIns(loggedOverriddenMethod);
        Set<Log.in> bridgedLogIns = annotationExtractor.getLogIns(overriddenMethod);
        Set<Log.in> overriddenLogIns = annotationExtractor.getLogIns(overriddenNotGenericMethod);
        // then
        assertThat(logIns, hasSize(1));
        assertThat(logIns.iterator().next().level(), is(DEBUG));

        assertThat(bridgedLogIns, hasSize(1));
        assertThat(bridgedLogIns.iterator().next().level(), is(INFO));

        assertThat(overriddenLogIns, hasSize(1));
        assertThat(overriddenLogIns.iterator().next().level(), is(INFO));
    }

    @Test
    public void getLogOuts() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        Set<Log.out> logOuts = annotationExtractor.getLogOuts(loggedOverriddenMethod);
        Set<Log.out> bridgedLogOuts = annotationExtractor.getLogOuts(overriddenMethod);
        Set<Log.out> overriddenLogOuts = annotationExtractor.getLogOuts(overriddenNotGenericMethod);
        // then
        assertThat(logOuts, hasSize(1));
        assertThat(logOuts.iterator().next().level(), is(DEBUG));

        assertThat(bridgedLogOuts, hasSize(1));
        assertThat(bridgedLogOuts.iterator().next().level(), is(INFO));

        assertThat(overriddenLogOuts, hasSize(1));
        assertThat(overriddenLogOuts.iterator().next().level(), is(INFO));
    }

    @Test
    public void getLogErrors() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        Set<Log.error> logErrors = annotationExtractor.getLogErrors(loggedOverriddenMethod);
        Set<Log.error> bridgedLogErrors = annotationExtractor.getLogErrors(overriddenMethod);
        Set<Log.error> overriddenLogErrors = annotationExtractor.getLogErrors(overriddenNotGenericMethod);
        // then
        assertThat(logErrors, hasSize(1));
        assertThat(logErrors.iterator().next().level(), is(DEBUG));

        assertThat(bridgedLogErrors, hasSize(1));
        assertThat(bridgedLogErrors.iterator().next().level(), is(INFO));

        assertThat(overriddenLogErrors, hasSize(1));
        assertThat(overriddenLogErrors.iterator().next().level(), is(INFO));
    }

    @Test
    public void getLogErrorsOrder() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getDeclaredMethod("errorsOrder");
        // when
        Set<Log.error> logErrors = annotationExtractor.getLogErrors(loggedOverriddenMethod);
        // then
        assertThat(logErrors, hasSize(5));
        Iterator<Log.error> logErrorsIterator = logErrors.iterator();
        assertThat(logErrorsIterator.next().level(), is(TRACE));
        assertThat(logErrorsIterator.next().level(), is(DEBUG));
        assertThat(logErrorsIterator.next().level(), is(INFO));
        assertThat(logErrorsIterator.next().level(), is(WARN));
        assertThat(logErrorsIterator.next().level(), is(ERROR));
    }

    @Test
    public void getMdcs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        Set<Mdc> mdcs = annotationExtractor.getMdcs(loggedOverriddenMethod);
        Set<Mdc> bridgedMdcs = annotationExtractor.getMdcs(overriddenMethod);
        Set<Mdc> overriddenMdcs = annotationExtractor.getMdcs(overriddenNotGenericMethod);
        // then
        assertThat(mdcs, hasSize(1));
        assertThat(mdcs.iterator().next().key(), isEmptyString());

        assertThat(bridgedMdcs, hasSize(1));
        assertThat(bridgedMdcs.iterator().next().key(), is("key"));

        assertThat(overriddenMdcs, hasSize(1));
        assertThat(overriddenMdcs.iterator().next().key(), is("key"));
    }

    @Test
    public void getParameterLogs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        List<Set<Log>> logs = annotationExtractor.getParameterLogs(loggedOverriddenMethod);
        List<Set<Log>> emptyLogs = annotationExtractor.getParameterLogs(overriddenMethod);
        List<Set<Log>> overriddenLogs = annotationExtractor.getParameterLogs(overriddenNotGenericMethod);
        // then
        assertThat(logs, hasSize(1));
        Set<Log> parameterLogs = logs.iterator().next();
        assertThat(parameterLogs, hasSize(1));
        assertThat(parameterLogs.iterator().next().level(), is(DEBUG));

        assertThat(emptyLogs, hasSize(1));
        Set<Log> emptyParameterLogs = emptyLogs.iterator().next();
        assertThat(emptyParameterLogs, is(empty()));

        assertThat(overriddenLogs, hasSize(1));
        Set<Log> overriddenParameterLogs = overriddenLogs.iterator().next();
        assertThat(overriddenParameterLogs, is(empty()));
    }

    @Test
    public void getParametersMdcs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        Method overriddenNotGenericMethod = AnnotatedChild.class.getMethod("overriddenNotGenericMethod", String.class);
        // when
        List<Set<Mdc>> parametersMdcs = annotationExtractor.getParametersMdcs(loggedOverriddenMethod);
        List<Set<Mdc>> emptyParametersMdcs = annotationExtractor.getParametersMdcs(overriddenMethod);
        List<Set<Mdc>> overriddenParametersMdcs = annotationExtractor.getParametersMdcs(overriddenNotGenericMethod);
        // then
        assertThat(parametersMdcs, hasSize(1));
        Set<Mdc> parameterMdcs = parametersMdcs.iterator().next();
        assertThat(parameterMdcs, hasSize(1));
        assertThat(parameterMdcs.iterator().next().key(), isEmptyString());

        assertThat(emptyParametersMdcs, hasSize(1));
        Set<Mdc> parameterEmptyParametersMdcs = emptyParametersMdcs.iterator().next();
        assertThat(parameterEmptyParametersMdcs, is(empty()));

        assertThat(overriddenParametersMdcs, hasSize(1));
        Set<Mdc> parameterOverriddenParametersMdcs = overriddenParametersMdcs.iterator().next();
        assertThat(parameterOverriddenParametersMdcs, is(empty()));
    }

    @SuppressWarnings("unused")
    private static class AnnotatedParent<T> {

        @Log(INFO)
        @Log.in(INFO)
        @Log.out(INFO)
        @Log.error(INFO)
        @Mdc(key = "key")
        public void loggedOverriddenMethod(@Log(INFO)
                                           @Mdc(key = "key") T input) {
        }

        @Log(INFO)
        @Log.in(INFO)
        @Log.out(INFO)
        @Log.error(INFO)
        @Mdc(key = "key")
        public void overriddenMethod(@Log(INFO)
                                     @Mdc(key = "key") T input) {
        }

        @Log(INFO)
        @Log.in(INFO)
        @Log.out(INFO)
        @Log.error(INFO)
        @Mdc(key = "key")
        public void overriddenNotGenericMethod(@Log(INFO)
                                               @Mdc(key = "key") String input) {
        }
    }

    @SuppressWarnings("unused")
    private static class AnnotatedChild extends AnnotatedParent<String> {

        @Log(DEBUG)
        @Log.in(DEBUG)
        @Log.out(DEBUG)
        @Log.error(DEBUG)
        @Mdc
        @Override
        public void loggedOverriddenMethod(@Log(DEBUG)
                                           @Mdc String input) {
        }

        @Override
        public void overriddenMethod(String input) {
        }

        @Override
        public void overriddenNotGenericMethod(String input) {
        }

        @Log.error(TRACE)
        @Log.error(DEBUG)
        @Log.error(INFO)
        @Log.error(WARN)
        @Log.error(ERROR)
        void errorsOrder() {
        }
    }

    @Test
    public void findLog() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getDeclaredMethod("log");
        Method loggerMethod = MultiLogger.class.getDeclaredMethod("loggerLog");
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        Log log = annotationExtractor.findLog(method, loggerNames);
        Log loggerLog = annotationExtractor.findLog(loggerMethod, loggerNames);
        Log notFoundLog = annotationExtractor.findLog(method, unknownLoggerNames);
        // then
        assertThat(log, notNullValue());
        assertThat(loggerLog, notNullValue());
        assertThat(notFoundLog, nullValue());
    }

    @Test
    public void findLogIn() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getDeclaredMethod("logIn");
        Method loggerMethod = MultiLogger.class.getDeclaredMethod("loggerLogIn");
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        Log.in logIn = annotationExtractor.findLogIn(method, loggerNames);
        Log.in loggerLogIn = annotationExtractor.findLogIn(loggerMethod, loggerNames);
        Log.in notFoundLogIn = annotationExtractor.findLogIn(method, unknownLoggerNames);
        // then
        assertThat(logIn, notNullValue());
        assertThat(loggerLogIn, notNullValue());
        assertThat(notFoundLogIn, nullValue());
    }

    @Test
    public void findLogOut() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getDeclaredMethod("logOut");
        Method loggerMethod = MultiLogger.class.getDeclaredMethod("loggerLogOut");
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        Log.out logOut = annotationExtractor.findLogOut(method, loggerNames);
        Log.out loggerLogOut = annotationExtractor.findLogOut(loggerMethod, loggerNames);
        Log.out notFoundLogOut = annotationExtractor.findLogOut(method, unknownLoggerNames);
        // then
        assertThat(logOut, notNullValue());
        assertThat(loggerLogOut, notNullValue());
        assertThat(notFoundLogOut, nullValue());
    }

    @Test
    public void findLogErrors() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getDeclaredMethod("logErrors");
        Method loggerMethod = MultiLogger.class.getDeclaredMethod("loggerLogErrors");
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        Set<Log.error> logErrors = annotationExtractor.findLogErrors(method, loggerNames);
        Set<Log.error> loggerLogErrors = annotationExtractor.findLogErrors(loggerMethod, loggerNames);
        Set<Log.error> notFoundLogErrors = annotationExtractor.findLogErrors(method, unknownLoggerNames);
        // then
        assertThat(logErrors, hasSize(1));
        assertThat(logErrors.iterator().next(), notNullValue());
        assertThat(loggerLogErrors, hasSize(1));
        assertThat(loggerLogErrors.iterator().next(), notNullValue());
        assertThat(notFoundLogErrors, is(empty()));
    }

    @Test
    public void findParameterLogs() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getDeclaredMethod("parameterLogs", String.class);
        Method loggerMethod = MultiLogger.class.getDeclaredMethod("loggerParameterLogs", String.class);
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        List<Log> parameterLogs = annotationExtractor.findParameterLogs(method, loggerNames);
        List<Log> loggerParameterLogs = annotationExtractor.findParameterLogs(loggerMethod, loggerNames);
        List<Log> notFoundParameterLogs = annotationExtractor.findParameterLogs(method, unknownLoggerNames);
        // then
        assertThat(parameterLogs, hasSize(1));
        assertThat(parameterLogs.get(0), notNullValue());
        assertThat(loggerParameterLogs, hasSize(1));
        assertThat(loggerParameterLogs.get(0), notNullValue());
        assertThat(notFoundParameterLogs, hasSize(1));
        assertThat(notFoundParameterLogs.get(0), nullValue());
    }

    @SuppressWarnings("unused")
    private static class MultiLogger {

        @Log
        void log() {
        }

        @Log.in
        void logIn() {
        }

        @Log.out
        void logOut() {
        }

        @Log.error
        void logErrors() {
        }

        void parameterLogs(@Log String input) {
        }

        @Log(logger = "logger")
        void loggerLog() {
        }

        @Log.in(logger = "logger")
        void loggerLogIn() {
        }

        @Log.out(logger = "logger")
        void loggerLogOut() {
        }

        @Log.error(logger = "logger")
        void loggerLogErrors() {
        }

        void loggerParameterLogs(@Log(logger = "logger") String input) {
        }
    }

    @Test
    public void synthesizeLogIn() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", ERROR);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", INFO);
        attributes.put("printer", "printer");
        attributes.put("logger", "logger");
        Log log = AnnotationUtils.synthesizeAnnotation(attributes, Log.class, null);
        // when
        Log.in logIn = annotationExtractor.synthesizeLogIn(log);
        // then
        assertThat(logIn.level(), is(ERROR));
        assertThat(logIn.ifEnabled(), is(WARN));
        assertThat(logIn.verbose(), is(INFO));
        assertThat(logIn.printer(), is("printer"));
        assertThat(logIn.logger(), is("logger"));
    }

    @Test
    public void synthesizeLogOut() {
        // given
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("level", ERROR);
        attributes.put("ifEnabled", WARN);
        attributes.put("verbose", INFO);
        attributes.put("printer", "printer");
        attributes.put("logger", "logger");
        Log log = AnnotationUtils.synthesizeAnnotation(attributes, Log.class, null);
        // when
        Log.out logOut = annotationExtractor.synthesizeLogOut(log);
        // then
        assertThat(logOut.level(), is(ERROR));
        assertThat(logOut.ifEnabled(), is(WARN));
        assertThat(logOut.verbose(), is(INFO));
        assertThat(logOut.printer(), is("printer"));
        assertThat(logOut.logger(), is("logger"));
    }

    @Test
    public void getMethodMetaLogs() throws NoSuchMethodException {
        // given
        Method method = MetaAnnotated.class.getMethod("method", int.class);
        // when
        Set<Log> logs = annotationExtractor.getLogs(method);
        // then
        assertThat(logs, hasSize(1));
    }

    @Test
    public void getParameterMetaLogs() throws NoSuchMethodException {
        // given
        Method method = MetaAnnotated.class.getMethod("method", int.class);
        // when
        List<Set<Log>> parameterLogs = annotationExtractor.getParameterLogs(method);
        // then
        assertThat(parameterLogs, hasSize(1));
        assertThat(parameterLogs.get(0), hasSize(1));
    }

    @SuppressWarnings("unused")
    private static class MetaAnnotated {

        @Base
        public void method(@Base int a) {
        }
    }

    @Target({ElementType.METHOD, ElementType.PARAMETER})
    @Retention(RetentionPolicy.RUNTIME)
    @Log
    private @interface Base {
    }
}
