package ru.tinkoff.integration.eclair.core;

import org.junit.Test;
import ru.tinkoff.integration.eclair.annotation.Log;
import ru.tinkoff.integration.eclair.annotation.Logs;
import ru.tinkoff.integration.eclair.annotation.Mdc;
import ru.tinkoff.integration.eclair.annotation.Mdcs;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.singleton;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;
import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Viacheslav Klapatniuk
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
        Method parentMethod = Parent.class.getMethod("parentMethod");
        Method interfaceMethod = Interface.class.getMethod("interfaceMethod");
        // when
        Set<Method> methods = annotationExtractor.getCandidateMethods(Child.class);
        // then
        assertThat(methods, hasSize(3));
        assertThat(methods, containsInAnyOrder(childMethod, parentMethod, interfaceMethod));
    }

    private interface Interface<T extends Throwable> {

        default void interfaceMethod() {
        }

        default void method(T input) {
        }
    }

    private static class Parent<T extends RuntimeException> implements Interface<T> {

        public void parentMethod() {
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
        Method method = ClassWithAnnotatedMethods.class.getMethod("logs");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLog() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("log", String.class);
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogIns() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logIns");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogIn() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logIn");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogOuts() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logOuts");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogOut() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logOut");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogErrors() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logErrors");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogError() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logError");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsMdcs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("mdcs");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsMdc() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("mdc");
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogArgs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logArgs", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsLogArg() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logArg", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterMdcs() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("parameterMdcs", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationDetectsParameterMdc() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("parameterMdc", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertTrue(has);
    }

    @Test
    public void hasAnyAnnotationNotDetectsLogOverParameter() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("log", String.class);
        Parameter parameter = method.getParameters()[0];
        // when
        boolean parameterHas = annotationExtractor.hasAnyAnnotation(parameter);
        // then
        assertFalse(parameterHas);
    }

    @Test
    public void hasAnyAnnotationNotDetectsLogArgOverMethod() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("logArg", String.class);
        // when
        boolean has = annotationExtractor.hasAnyAnnotation(method);
        // then
        assertFalse(has);
    }

    @Test
    public void hasAnyAnnotationDetectsNothing() throws NoSuchMethodException {
        // given
        Method method = ClassWithAnnotatedMethods.class.getMethod("none", String.class);
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
        public void logs() {
        }

        @Log
        public void log(String input) {
        }

        @Log.ins(@Log.in)
        public void logIns() {
        }

        @Log.in
        public void logIn() {
        }

        @Log.outs(@Log.out)
        public void logOuts() {
        }

        @Log.out
        public void logOut() {
        }

        @Log.errors(@Log.error)
        public void logErrors() {
        }

        @Log.error
        public void logError() {
        }

        @Mdcs(@Mdc(key = "", value = ""))
        public void mdcs() {
        }

        @Mdc(key = "", value = "")
        public void mdc() {
        }

        public void logArgs(@Log.args(@Log.arg) String input) {
        }

        public void logArg(@Log.arg String input) {
        }

        public void parameterMdcs(@Mdcs(@Mdc(key = "", value = "")) String input) {
        }

        public void parameterMdc(@Mdc(key = "", value = "") String input) {
        }

        public void none(String input) {
        }
    }

    @Test
    public void getLogs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        Set<Log> logs = annotationExtractor.getLogs(loggedOverriddenMethod);
        Set<Log> emptyLogs = annotationExtractor.getLogs(overriddenMethod);
        // then
        assertThat(logs, hasSize(1));
        assertThat(logs.iterator().next().level(), is(DEBUG));
        assertThat(emptyLogs, is(empty()));
    }

    @Test
    public void getLogIns() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        Set<Log.in> logIns = annotationExtractor.getLogIns(loggedOverriddenMethod);
        Set<Log.in> emptyLogIns = annotationExtractor.getLogIns(overriddenMethod);
        // then
        assertThat(logIns, hasSize(1));
        assertThat(logIns.iterator().next().level(), is(DEBUG));
        assertThat(emptyLogIns, is(empty()));
    }

    @Test
    public void getLogOut() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        Set<Log.out> logOuts = annotationExtractor.getLogOuts(loggedOverriddenMethod);
        Set<Log.out> emptyLogOuts = annotationExtractor.getLogOuts(overriddenMethod);
        // then
        assertThat(logOuts, hasSize(1));
        assertThat(logOuts.iterator().next().level(), is(DEBUG));
        assertThat(emptyLogOuts, is(empty()));
    }

    @Test
    public void getLogErrors() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        Set<Log.error> logErrors = annotationExtractor.getLogErrors(loggedOverriddenMethod);
        Set<Log.error> emptyLogErrors = annotationExtractor.getLogErrors(overriddenMethod);
        // then
        assertThat(logErrors, hasSize(1));
        assertThat(logErrors.iterator().next().level(), is(DEBUG));
        assertThat(emptyLogErrors, is(empty()));
    }

    @Test
    public void getLogErrorsOrder() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("errorsOrder");
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
    public void getLogMdcs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        Set<Mdc> mdcs = annotationExtractor.getMdcs(loggedOverriddenMethod);
        Set<Mdc> emptyMdcs = annotationExtractor.getMdcs(overriddenMethod);
        // then
        assertThat(mdcs, hasSize(1));
        assertThat(mdcs.iterator().next().key(), isEmptyString());
        assertThat(emptyMdcs, is(empty()));
    }

    @Test
    public void getLogArgs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        List<Set<Log.arg>> logArgs = annotationExtractor.getLogArgs(loggedOverriddenMethod);
        List<Set<Log.arg>> emptyLogArgs = annotationExtractor.getLogArgs(overriddenMethod);
        // then
        assertThat(logArgs, hasSize(1));
        Set<Log.arg> parameterLogArgs = logArgs.iterator().next();
        assertThat(parameterLogArgs, hasSize(1));
        assertThat(parameterLogArgs.iterator().next().ifEnabled(), is(DEBUG));

        assertThat(emptyLogArgs, hasSize(1));
        Set<Log.arg> parameterEmptyLogArgs = emptyLogArgs.iterator().next();
        assertThat(parameterEmptyLogArgs, is(empty()));
    }

    @Test
    public void getParametersMdcs() throws NoSuchMethodException {
        // given
        Method loggedOverriddenMethod = AnnotatedChild.class.getMethod("loggedOverriddenMethod", String.class);
        Method overriddenMethod = AnnotatedChild.class.getMethod("overriddenMethod", String.class);
        // when
        List<Set<Mdc>> parametersMdcs = annotationExtractor.getParametersMdcs(loggedOverriddenMethod);
        List<Set<Mdc>> emptyParametersMdcs = annotationExtractor.getParametersMdcs(overriddenMethod);
        // then
        assertThat(parametersMdcs, hasSize(1));
        Set<Mdc> parameterMdcs = parametersMdcs.iterator().next();
        assertThat(parameterMdcs, hasSize(1));
        assertThat(parameterMdcs.iterator().next().key(), isEmptyString());

        assertThat(emptyParametersMdcs, hasSize(1));
        Set<Mdc> parameterEmptyParametersMdcs = emptyParametersMdcs.iterator().next();
        assertThat(parameterEmptyParametersMdcs, is(empty()));
    }

    @SuppressWarnings("unused")
    private static class AnnotatedParent<T> {

        @Log(INFO)
        @Log.in(INFO)
        @Log.out(INFO)
        @Log.error(INFO)
        @Mdc(key = "key", value = "")
        public void loggedOverriddenMethod(@Log.arg(INFO)
                                           @Mdc(key = "key", value = "") T input) {
        }

        @Log(INFO)
        @Log.in(INFO)
        @Log.out(INFO)
        @Log.error(INFO)
        @Mdc(key = "key", value = "")
        public void overriddenMethod(@Log.arg(INFO)
                                     @Mdc(key = "key", value = "") T input) {
        }
    }

    @SuppressWarnings("unused")
    private static class AnnotatedChild extends AnnotatedParent<String> {

        @Log(DEBUG)
        @Log.in(DEBUG)
        @Log.out(DEBUG)
        @Log.error(DEBUG)
        @Mdc(key = "", value = "")
        @Override
        public void loggedOverriddenMethod(@Log.arg(DEBUG)
                                           @Mdc(key = "", value = "") String input) {
        }

        @Override
        public void overriddenMethod(String input) {
        }

        @Log.error(TRACE)
        @Log.error(DEBUG)
        @Log.error(INFO)
        @Log.error(WARN)
        @Log.error(ERROR)
        public void errorsOrder() {
        }
    }

    @Test
    public void findLog() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getMethod("log");
        Method loggerMethod = MultiLogger.class.getMethod("loggerLog");
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
        Method method = MultiLogger.class.getMethod("logIn");
        Method loggerMethod = MultiLogger.class.getMethod("loggerLogIn");
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
        Method method = MultiLogger.class.getMethod("logOut");
        Method loggerMethod = MultiLogger.class.getMethod("loggerLogOut");
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
        Method method = MultiLogger.class.getMethod("logErrors");
        Method loggerMethod = MultiLogger.class.getMethod("loggerLogErrors");
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
    public void findLogArgs() throws NoSuchMethodException {
        // given
        Method method = MultiLogger.class.getMethod("logArgs", String.class);
        Method loggerMethod = MultiLogger.class.getMethod("loggerLogArgs", String.class);
        Set<String> loggerNames = new HashSet<>(asList("", "logger"));
        Set<String> unknownLoggerNames = singleton("b");
        // when
        List<Log.arg> logArgs = annotationExtractor.findLogArgs(method, loggerNames);
        List<Log.arg> loggerLogArgs = annotationExtractor.findLogArgs(loggerMethod, loggerNames);
        List<Log.arg> notFoundLogArgs = annotationExtractor.findLogArgs(method, unknownLoggerNames);
        // then
        assertThat(logArgs, hasSize(1));
        assertThat(logArgs.get(0), notNullValue());
        assertThat(loggerLogArgs, hasSize(1));
        assertThat(loggerLogArgs.get(0), notNullValue());
        assertThat(notFoundLogArgs, hasSize(1));
        assertThat(notFoundLogArgs.get(0), nullValue());
    }

    @SuppressWarnings("unused")
    private static class MultiLogger {

        @Log
        public void log() {
        }

        @Log.in
        public void logIn() {
        }

        @Log.out
        public void logOut() {
        }

        @Log.error
        public void logErrors() {
        }

        public void logArgs(@Log.arg String input) {
        }

        @Log(logger = "logger")
        public void loggerLog() {
        }

        @Log.in(logger = "logger")
        public void loggerLogIn() {
        }

        @Log.out(logger = "logger")
        public void loggerLogOut() {
        }

        @Log.error(logger = "logger")
        public void loggerLogErrors() {
        }

        public void loggerLogArgs(@Log.arg(logger = "logger") String input) {
        }
    }
}
