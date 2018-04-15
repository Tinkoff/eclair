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

import org.slf4j.MDC;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * @author Vyacheslav Klapatnyuk
 */
@SuppressWarnings("unused")
class Example {

    @Log
    public void simple() {
    }

    @Log
    public void simpleError() {
        throw new RuntimeException();
    }

    @Log(INFO)
    public void level() {
    }

    @Log(level = INFO, ifEnabled = DEBUG)
    public void ifEnabled() {
    }

    @Log(INFO)
    public boolean verbose(String s, Integer i, Double d) {
        return false;
    }

    @Log(verbose = OFF)
    public boolean verboseDisabled(String s, Integer i, Double d) {
        return false;
    }

    @Log(printer = "jacksonPrinter")
    public void json(Dto dto, Integer i) {
    }

    @Log(printer = "jaxb2Printer")
    public void xml(Dto dto, Integer i) {
    }

    @Log.in(INFO)
    @Log.out(TRACE)
    public void inOut(Dto dto, String s, Integer i) {
    }

    @Log.error
    public void error() {
        throw new RuntimeException("Something strange happened");
    }

    @Log.error(level = WARN, ifEnabled = DEBUG)
    public void warningOnDebug() {
        throw new RuntimeException("Something strange happened, but it doesn't matter");
    }

    @Log.error(level = WARN, ofType = {NullPointerException.class, IndexOutOfBoundsException.class})
    @Log.error(exclude = Error.class)
    public void filterErrors(Throwable throwable) throws Throwable {
        throw throwable;
    }

    @Log.error(level = ERROR, ofType = Exception.class)
    @Log.error(level = WARN, ofType = RuntimeException.class)
    public void mostSpecific() {
        throw new IllegalArgumentException();
    }

    public void parameter(@Log(INFO) Dto dto, String s, Integer i) {
    }

    @Log.out(printer = "maskJaxb2Printer")
    public Dto printers(@Log(printer = "maskJaxb2Printer") Dto xml,
                        @Log(printer = "jacksonPrinter") Dto json,
                        Integer i) {
        return xml;
    }

    @Log.in(INFO)
    public void parameterLevels(@Log(INFO) Double d,
                                @Log(DEBUG) String s,
                                @Log(TRACE) Integer i) {
    }

    @Log.in(INFO)
    @Log.out(level = TRACE, verbose = TRACE)
    @Log.error(level = WARN, ofType = RuntimeException.class, exclude = NullPointerException.class)
    @Log.error(level = ERROR, ofType = {Error.class, Exception.class})
    public Dto mix(@Log(printer = "jaxb2Printer") Dto xml,
                   @Log(ifEnabled = TRACE, printer = "jacksonPrinter") Dto json,
                   Integer i) {
        throw new IllegalArgumentException("Something strange happened");
    }

    // TODO: add example with bean referencing inside @Mdc
    // TODO: add example with static method invocation inside @Mdc
    // TODO: add example of print using 'Printer' bean name inside @Mdc

    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.OuterClass.method >
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.Example.mdcByMethod > Dto{i=0, s='null'}
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.InnerClass.method >
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.InnerClass.method <
     * ..
     * DEBUG [key=value, sum=2] ru.tinkoff.eclair.example.Example.mdcByMethod <
     * ..
     * DEBUG [sum=2] ru.tinkoff.eclair.example.OuterClass.method <
     */
    @Mdc(key = "key", value = "value")
    @Mdc(key = "sum", value = "1 + 1", global = true)
    @Log
    public void mdcByMethod(Dto dto) {
    }

    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.OuterClass.method >
     * ..
     * DEBUG [length=3, staticString=some string] ru.tinkoff.eclair.example.Example.mdcByArg > Dto{i=0, s='null'}
     * DEBUG [length=3, staticString=some string] ru.tinkoff.eclair.example.Example.mdcByArg <
     * ..
     * DEBUG [staticString=some string] ru.tinkoff.eclair.example.OuterClass.method <
     */
    @Log
    public void mdcByArg(@Mdc(key = "length", value = "s.length()")
                         @Mdc(key = "staticString", value = "some string", global = true) Dto dto) {
    }

    /**
     * DEBUG [parameterSKey=parameterSValue, parameterIKey=parameterIValue] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     */
    @Log.in
    public void mdc1(@Mdc(key = "parameterSKey", value = "parameterSValue") String s, @Mdc(key = "parameterIKey", value = "parameterIValue") Integer i) {
    }

    /**
     * DEBUG [parameterSKey=s, parameterIKey=0] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     */
    @Log.in
    public void mdc2(@Mdc(key = "parameterSKey") String s, @Mdc(key = "parameterIKey") Integer i) {
    }

    /**
     * DEBUG [s=parameterSValue, i=parameterIValue] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     * else if parameter names not enabled
     * DEBUG [mdc[0]=parameterSValue, mdc[1]=parameterIValue] ru.tinkoff.eclair.example.Example.mdc > "s", 0
     */
    @Log.in
    public void mdc3(@Mdc("parameterSValue") String s, @Mdc("parameterIValue") Integer i) {
    }

    /**
     * DEBUG [s=s, i=0] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     * else if parameter names not enabled
     * DEBUG [mdc[0]=s, mdc[1]=0] ru.tinkoff.eclair.example.Example.mdc > "s", 0
     */
    @Log.in
    public void mdc4(@Mdc String s, @Mdc Integer i) {
    }

    /**
     * DEBUG [methodKey=methodValue] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     */
    @Mdc(key = "methodKey", value = "methodValue")
    @Log.in
    public void mdc5(String s, Integer i) {
    }

    /**
     * DEBUG [methodKey[s]=s, methodKey[i]=0] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     * else if parameter names not enabled
     * DEBUG [methodKey[0]=s, methodKey[1]=0] ru.tinkoff.eclair.example.Example.mdc > "s", 0
     */
    @Mdc(key = "methodKey")
    @Log.in
    public void mdc6(String s, Integer i) {
    }

    /**
     * DEBUG [mdc=methodValue] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     */
    @Mdc("methodValue")
    @Log.in
    public void mdc7(String s, Integer i) {
    }

    /**
     * Equals to {@link #mdc4}
     * DEBUG [s=s, i=0] ru.tinkoff.eclair.example.Example.mdc > s="s", i=0
     * else if parameter names not enabled
     * DEBUG [mdc[0]=s, mdc[1]=0] ru.tinkoff.eclair.example.Example.mdc > "s", 0
     */
    @Mdc
    @Log.in
    public void mdc8(String s, Integer i) {
    }

    // TODO: add example for MDC with bean referencing

//    @Autowired
//    private ManualLogger logger;

    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] ru.tinkoff.eclair.example.Example.manualLogging >
     * DEBUG [key=value] ru.tinkoff.eclair.example.Example.manualLogging - Manual logging: 0.123
     * INFO  [key=value] ru.tinkoff.eclair.example.Example.manualLogging - Lazy manual logging: 0.456
     * DEBUG [key=value] ru.tinkoff.eclair.example.Example.manualLogging <
     */
    @Log
    public void manualLogging() {
        MDC.put("key", "value");
//        logger.debug("Manual logging: {}", new Random().nextDouble());
//        logger.info("Lazy manual logging: {}", (Supplier) () -> new Random().nextDouble());
    }

    /**
     * if logger level <= DEBUG
     *
     * DEBUG [] r.t.eclair.example.Example.manualLevelLogging >
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - false
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - false
     * DEBUG [] r.t.eclair.example.Example.manualLevelLogging - debug
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - info
     * INFO  [] r.t.eclair.example.Example.manualLevelLogging - infoIfDebugEnabled
     * WARN  [] r.t.eclair.example.Example.manualLevelLogging - warn
     * WARN  [] r.t.eclair.example.Example.manualLevelLogging - warnIfInfoEnabled
     * WARN  [] r.t.eclair.example.Example.manualLevelLogging - warnIfDebugEnabled
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging - error
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfWarnEnabled
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfInfoEnabled
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfDebugEnabled
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging - log
     * ERROR [] r.t.eclair.example.Example.manualLevelLogging <
     */
    @Log
    public void manualLevelLogging() {
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - false
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - true
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - false
        /*logger.info(Boolean.toString(logger.isTraceLogEnabled()));
        logger.info(Boolean.toString(logger.isDebugLogEnabled()));
        logger.info(Boolean.toString(logger.isInfoLogEnabled()));
        logger.info(Boolean.toString(logger.isWarnLogEnabled()));
        logger.info(Boolean.toString(logger.isErrorLogEnabled()));
        logger.info(Boolean.toString(logger.isLogEnabled(TRACE)));

        // nothing to log
        logger.trace("trace");

        // DEBUG [] r.t.eclair.example.Example.manualLevelLogging - debug
        // nothing to log
        logger.debug("debug");
        logger.debugIfTraceEnabled("debugIfTraceEnabled");

        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - info
        // INFO  [] r.t.eclair.example.Example.manualLevelLogging - infoIfDebugEnabled
        // nothing to log
        logger.info("info");
        logger.infoIfDebugEnabled("infoIfDebugEnabled");
        logger.infoIfTraceEnabled("infoIfTraceEnabled");

        // WARN  [] r.t.eclair.example.Example.manualLevelLogging - warn
        // WARN  [] r.t.eclair.example.Example.manualLevelLogging - warnIfInfoEnabled
        // WARN  [] r.t.eclair.example.Example.manualLevelLogging - warnIfDebugEnabled
        // nothing to log
        logger.warn("warn");
        logger.warnIfInfoEnabled("warnIfInfoEnabled");
        logger.warnIfDebugEnabled("warnIfDebugEnabled");
        logger.warnIfTraceEnabled("warnIfTraceEnabled");

        // ERROR [] r.t.eclair.example.Example.manualLevelLogging - error
        // ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfWarnEnabled
        // ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfInfoEnabled
        // ERROR [] r.t.eclair.example.Example.manualLevelLogging - errorIfDebugEnabled
        // nothing to log
        logger.error("error");
        logger.errorIfWarnEnabled("errorIfWarnEnabled");
        logger.errorIfInfoEnabled("errorIfInfoEnabled");
        logger.errorIfDebugEnabled("errorIfDebugEnabled");
        logger.errorIfTraceEnabled("errorIfTraceEnabled");

        // ERROR [] r.t.eclair.example.Example.manualLevelLogging - log
        logger.log(ERROR, "log");*/
    }

    /**
     * equals to next
     */
    @Log.in(level = INFO, ifEnabled = OFF, verbose = ERROR, printer = "json")
    @Log.out(level = INFO, ifEnabled = OFF, verbose = ERROR, printer = "json")
    public void inAndOut() {
    }

    @Log(level = INFO, ifEnabled = OFF, verbose = ERROR, printer = "json")
    public void logEqualsToInAndOut() {
    }

    /**
     * equals to next
     */
    @Log.in(level = INFO, verbose = TRACE)
    @Log(level = TRACE, verbose = OFF)
    public void inAndLog() {
    }

    @Log.in(level = INFO, verbose = TRACE)
    @Log.out(level = TRACE, verbose = OFF)
    public void inAndOutEqualsToInAndLog() {
    }

    /**
     * equals to next
     */
    @Log(level = TRACE, verbose = OFF)
    @Log.out(level = INFO, verbose = TRACE)
    public void logAndOut() {
    }

    @Log.in(level = TRACE, verbose = OFF)
    @Log.out(level = INFO, verbose = TRACE)
    public void inAndOutEqualsToLogAndOut() {
    }

    /**
     * if logger level = ERROR or WARN
     * (nothing to log)
     *
     * else if logger level = INFO or DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.priority >
     *
     * else if logger level = TRACE
     * INFO  [] ru.tinkoff.eclair.example.Example.priority >
     * TRACE [] ru.tinkoff.eclair.example.Example.priority <
     */
    @Log.in(level = INFO, verbose = TRACE)
    @Log(level = TRACE, verbose = OFF)
    public void priority() {
    }

    // TODO: add example for {@link LogError} priority (looking for the nearest child)

    // TODO: add example for {@link LogError} ordering (in order of appearance)

    // TODO: add example for meta-annotation usage

    /**
     * TODO: add javadoc
     */
//    @Log(logger = "simpleLogger")
//    @Log(logger = "auditLogger")
    public void logMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
//    @Log.in(logger = "simpleLogger")
//    @Log.in(logger = "auditLogger")
    public void logInMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
//    @Log.out(logger = "simpleLogger")
//    @Log.out(logger = "auditLogger")
    public void logOutMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
//    @Log.error(logger = "simpleLogger")
//    @Log.error(logger = "auditLogger")
    public void logErrorMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
    public void parameterLogMultiLogger(/*@Log(logger = "simpleLogger")
                                  @Log(logger = "auditLogger")*/ Dto dto) {
    }

    /**
     * TODO: add javadoc
     */
    @Log
    @Log
    public void duplicatedAnnotation() {
    }

    // TODO: for several loggers with Orders
    // TODO: for several loggers without Orders

    /**
     * (nothing to log)
     */
    @Log.in(OFF)
    public void offLevel() {
    }

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.offLevelWithArg > a=1
     */
    @Log.in(OFF)
    public void offLevelWithArg(@Log int a) {
    }

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.offLevelWithArg >
     */
    @Log.in
    public void argOffLevel(@Log(OFF) int a) {
    }

    /**
     * (nothing to log)
     */
    @Log.in(OFF)
    public void offLevel(@Log(OFF) int a) {
    }
}
