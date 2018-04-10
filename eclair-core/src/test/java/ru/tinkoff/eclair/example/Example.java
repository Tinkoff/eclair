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
import org.springframework.beans.factory.annotation.Autowired;
import ru.tinkoff.eclair.annotation.Log;
import ru.tinkoff.eclair.annotation.Mdc;
import ru.tinkoff.eclair.logger.Dto;
import ru.tinkoff.eclair.logger.ManualLogger;

import java.util.Random;
import java.util.function.Supplier;

import static org.springframework.boot.logging.LogLevel.*;

/**
 * Estimated configuration:
 * logging.pattern.console: '%-5level [%X] %logger{50} %msg%n'
 *
 * @author Vyacheslav Klapatnyuk
 */
@SuppressWarnings("unused")
public class Example {

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.simple >
     * DEBUG [] ru.tinkoff.eclair.example.Example.simple <
     */
    @Log
    public void simple() {
    }

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.simpleWithError >
     * DEBUG [] ru.tinkoff.eclair.example.Example.simpleWithError !
     */
    @Log
    public void simpleWithError() {
        throw new RuntimeException();
    }

    /**
     * if logger level = INFO
     * INFO  [] ru.tinkoff.eclair.example.Example.level >
     * INFO  [] ru.tinkoff.eclair.example.Example.level <
     */
    @Log(INFO)
    public void level() {
    }

    /**
     * if logger level = INFO
     * (nothing to log)
     *
     * if logger level = DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.levelIfEnabled >
     * INFO  [] ru.tinkoff.eclair.example.Example.levelIfEnabled <
     *
     * if logger level = TRACE
     * INFO  [] ru.tinkoff.eclair.example.Example.levelIfEnabled >
     * INFO  [] ru.tinkoff.eclair.example.Example.levelIfEnabled <
     */
    @Log(level = INFO, ifEnabled = DEBUG)
    public void levelIfEnabled() {
    }

    /**
     * if logger level = INFO
     * INFO  [] ru.tinkoff.eclair.example.Example.levelWithError >
     * INFO  [] ru.tinkoff.eclair.example.Example.levelWithError !
     */
    @Log(INFO)
    public void levelWithError() {
        throw new RuntimeException();
    }

    /**
     * if logger level = DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.verboseLevel > s="a", i=0, d=0.0
     * INFO  [] ru.tinkoff.eclair.example.Example.verboseLevel < false
     *
     * else if logger level = INFO
     * INFO  [] ru.tinkoff.eclair.example.Example.verboseLevel >
     * INFO  [] ru.tinkoff.eclair.example.Example.verboseLevel <
     */
    @Log(INFO)
    public Boolean verboseLevel(String s, Integer i, Double d) {
        return false;
    }

    /**
     * if logger level = TRACE
     * TRACE [] ru.tinkoff.eclair.example.Example.verboseLowest > s="a", i=0, d=0.0
     * TRACE [] ru.tinkoff.eclair.example.Example.verboseLowest < false
     *
     * for any other logger level
     * TRACE [] ru.tinkoff.eclair.example.Example.verboseLowest >
     * TRACE [] ru.tinkoff.eclair.example.Example.verboseLowest <
     */
    @Log(level = TRACE, verbose = TRACE)
    public Boolean verboseLowest(String s, Integer i, Double d) {
        return false;
    }

    /**
     * for any logger level
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseHighest > s="a", i=0, d=0.0
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseHighest < false
     */
    @Log(verbose = OFF)
    public Boolean verboseHighest(String s, Integer i, Double d) {
        return false;
    }

    /**
     * if logger level <= DEBUG
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseWithError > s="a", i=0, d=0.0
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseWithError !
     */
    @Log
    public Boolean verboseWithError(String s, Integer i, Double d) {
        throw new RuntimeException();
    }

    /**
     * if logger level <= DEBUG
     * DEBUG [] r.t.eclair.example.Example.verboseDtoToString > dto=Dto{i=0, s='null'}, i=0
     * DEBUG [] r.t.eclair.example.Example.verboseDtoToString <
     */
    @Log
    public void verboseDtoToString(Dto dto, Integer i) {
    }

    /**
     * if logger level <= DEBUG
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseDtoToJson > dto={"i":0,"s":null}, i=0
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseDtoToJson <
     */
    @Log(printer = "json")
    public void verboseDtoToJson(Dto dto, Integer i) {
    }

    /**
     * if logger level <= DEBUG
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseDtoToXml > dto=<dto><i>0</i></dto>, i=0
     * DEBUG [] ru.tinkoff.eclair.example.Example.verboseDtoToXml <
     */
    @Log(printer = "xml")
    public void verboseDtoToXml(Dto dto, Integer i) {
    }

    /**
     * if logger level <= DEBUG && applied auto-configuration
     *
     * has overridden toString()
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString > dto=Dto{i=0, s='null'}, i=0
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString <
     *
     * or else "xml"
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString > dto=<dto><i>0</i></dto>, i=0
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString <
     *
     * or else "json"
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString > dto={"i":0,"s":null}, i=0
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString <
     *
     * or else Object.toString()
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString > dto=ru.tinkoff.eclair.logger.Dto@6b884d57, i=0
     * DEBUG [] r.t.e.e.Example.verboseDtoToXmlOrJsonOrString <
     */
    @Log
    public void verboseDtoToXmlOrJsonOrString(Dto dto, Integer i) {
    }

    // TODO: add example for @Log.error#verbose()

    /**
     * if logger level <= DEBUG
     * DEBUG [] ru.tinkoff.eclair.example.Example.simpleInEvent >
     */
    @Log.in
    public void simpleInEvent() {
    }

    /**
     * if logger level <= DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.inEvent > dto={"i":0,"s":null}, s=null, i=0
     */
    @Log.in(level = INFO, printer = "json")
    public void inEvent(Dto dto, String s, Integer i) {
    }

    /**
     * if logger level <= DEBUG
     * DEBUG [] ru.tinkoff.eclair.example.Example.simpleOutEvent <
     */
    @Log.out
    public void simpleOutEvent() {
    }

    /**
     * if logger level <= DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.outEvent <
     */
    @Log.out(INFO)
    public void outEvent(Dto dto, String s, Integer i) {
    }

    /**
     * ERROR [] ru.tinkoff.eclair.example.Example.simpleErrorEvent ! java.lang.RuntimeException: message
     * java.lang.RuntimeException: message
     *     at ru.tinkoff.eclair.example.Example.simpleErrorEvent(Example.java:167)
     */
    @Log.error
    public void simpleErrorEvent() {
        throw new RuntimeException("message");
    }

    /**
     * WARN  [] ru.tinkoff.eclair.example.Example.levelErrorEvent ! java.lang.RuntimeException: message
     * java.lang.RuntimeException: message
     *     at ru.tinkoff.eclair.example.Example.levelErrorEvent(Example.java:167)
     */
    @Log.error(WARN)
    public void levelErrorEvent() {
        throw new RuntimeException("message");
    }

    /**
     * if logger level = DEBUG
     * WARN  [] r.t.e.example.Example.levelIfEnabledErrorEvent ! java.lang.RuntimeException: message
     * java.lang.RuntimeException: message
     *     at r.t.e.example.Example.levelIfEnabledErrorEvent(Example.java:167)
     *
     * if logger level = INFO
     * (nothing to log)
     */
    @Log.error(ifEnabled = DEBUG)
    public void levelIfEnabledErrorEvent() {
        throw new RuntimeException("message");
    }

    /**
     * ERROR [] r.tinkoff.eclair.example.Example.verboseErrorEvent ! java.lang.RuntimeException: runtimeException
     * java.lang.RuntimeException: runtimeException
     *     at r.tinkoff.eclair.example.Example.verboseErrorEvent(Example.java:167)
     *
     * or else
     * WARN  [] r.tinkoff.eclair.example.Example.verboseErrorEvent ! java.lang.NullPointerException: nullPointerException
     * java.lang.NullPointerException: nullPointerException
     *     at r.tinkoff.eclair.example.Example.verboseErrorEvent(Example.java:167)
     *
     * or else
     * (nothing to log)
     */
    @Log.error(ofType = RuntimeException.class)
    @Log.error(level = WARN, ofType = NullPointerException.class)
    public void verboseErrorEvent() throws Exception {
        if (new Random().nextBoolean()) {
            throw new RuntimeException("runtimeException");
        }
        if (new Random().nextBoolean()) {
            throw new NullPointerException("nullPointerException");
        }
        throw new Exception();
    }

    /**
     * ERROR [] r.t.e.e.Example.verboseWithExcludesErrorEvent ! java.lang.RuntimeException: runtimeException
     * java.lang.RuntimeException: runtimeException
     *     at r.t.e.e.Example.verboseWithExcludesErrorEvent(Example.java:167)
     *
     * or else
     * (nothing to log)
     */
    @Log.error(ofType = RuntimeException.class, exclude = {IllegalArgumentException.class, NullPointerException.class})
    public void verboseWithExcludesErrorEvent() {
        if (new Random().nextBoolean()) {
            throw new RuntimeException("runtimeException");
        }
        throw new IllegalArgumentException("message");
    }

    /**
     * DEBUG [] ru.tinkoff.eclair.example.Example.verbose > dto=Dto{i=0, s='null'}
     */
    public void verbose(@Log Dto dto, String s, Integer i) {
    }

    /**
     * INFO  [] ru.tinkoff.eclair.example.Example.levelVerbose > dto=Dto{i=0, s='null'}
     */
    public void levelVerbose(@Log(INFO) Dto dto, String s, Integer i) {
    }

    /**
     * DEBUG [] r.t.e.example.Example.verboseToVariousPrinters > xmlDto=<dto><i>0</i></dto>, jsonDto={"i":0,"s":null}
     */
    public void verboseToVariousPrinters(@Log(printer = "xml") Dto xmlDto,
                                         @Log(printer = "json") Dto jsonDto,
                                         Integer i) {
    }

    /**
     * if logger level > INFO
     * (nothing to log)
     *
     * else if logger level = INFO
     * INFO  [] r.t.eclair.example.Example.inEventWithLevelVerbose > d=0.0
     *
     * else if logger level = DEBUG
     * INFO  [] r.t.eclair.example.Example.inEventWithLevelVerbose > d=0.0, s="s"
     *
     * else if logger level = TRACE
     * INFO  [] r.t.eclair.example.Example.inEventWithLevelVerbose > d=0.0, s="s", i=0
     */
    @Log.in(INFO)
    public void inEventWithLevelVerbose(@Log(INFO) Double d,
                                        @Log(DEBUG) String s,
                                        @Log(TRACE) Integer i) {
    }

    // TODO: add example with several mask expressions
    // TODO: add example of return value masking

    /**
     * if logger level = TRACE
     * INFO  [] ru.tinkoff.eclair.example.Example.mix > xmlDto=<dto><i>0</i></dto>, jsonDto={"i":0,"s":null}
     * WARN  [] ru.tinkoff.eclair.example.Example.mix ! java.lang.IllegalArgumentException: message
     * java.lang.IllegalArgumentException: message
     *     at ru.tinkoff.eclair.example.Example.mix(Example.java:245)
     *
     * else if logger level = DEBUG
     * INFO  [] ru.tinkoff.eclair.example.Example.mix > xmlDto=<dto><i>0</i></dto>
     * WARN  [] ru.tinkoff.eclair.example.Example.mix ! java.lang.IllegalArgumentException: message
     * java.lang.IllegalArgumentException: message
     *     at ru.tinkoff.eclair.example.Example.mix(Example.java:245)
     *
     * else if logger level = INFO
     * INFO  [] ru.tinkoff.eclair.example.Example.mix >
     * WARN  [] ru.tinkoff.eclair.example.Example.mix ! java.lang.IllegalArgumentException: message
     * java.lang.IllegalArgumentException: message
     *     at ru.tinkoff.eclair.example.Example.mix(Example.java:245)
     *
     * else if logger level = WARN
     * WARN  [] ru.tinkoff.eclair.example.Example.mix ! java.lang.IllegalArgumentException: message
     * java.lang.IllegalArgumentException: message
     *     at ru.tinkoff.eclair.example.Example.mix(Example.java:245)
     *
     * else if logger level = ERROR
     * (nothing to log)
     */
    @Log.in(INFO)
    @Log.out(level = TRACE, verbose = TRACE)
    @Log.error(level = WARN, ofType = RuntimeException.class, exclude = NullPointerException.class)
    @Log.error(level = ERROR, ofType = {Error.class, Exception.class})
    public Dto mix(@Log(printer = "xml") Dto xmlDto,
                   @Log(ifEnabled = TRACE, printer = "json") Dto jsonDto,
                   Integer i) {
        throw new IllegalArgumentException("message");
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

    /*if (annotatedElement.isParameter) {
        // for annotated parameter
        if (key.hasText) {
            if (value.hasText) {
                // key and value
                MDC.put(key, spel(value)); // 1
            } else {
                // only key
                MDC.put(key, parameter.value); // 2
            }
        } else if (value.hasText) {
            // only value
            MDC.put(parameter.name, spel(value)); // 3
        } else {
            // nothing
            MDC.put(parameter.name, parameter.value); // 4
        }
    } else if (annotatedElement.isMethod) {
        // for annotated method
        if (key.hasText) {
            if (value.hasText) {
                // key and value
                MDC.put(key, spel(value)); // 5
            } else {
                // only key
                MDC.put(key[parameter0.name], parameter0.value); // 6
                ..
                MDC.put(key[parameterN.name], parameterN.value);
            }
        } else if (value.hasText) {
            // only value
            MDC.put(method.name, spel(value)); // 7
        } else {
            // nothing
            MDC.put(method.name[parameter0.name], parameter0.value); // 8
            ..
            MDC.put(method.name[parameterN.name], parameterN.value);
        }
    }*/

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

    @Autowired
    private ManualLogger logger;

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
        logger.debug("Manual logging: {}", new Random().nextDouble());
        logger.info("Lazy manual logging: {}", (Supplier) () -> new Random().nextDouble());
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
        logger.info(Boolean.toString(logger.isTraceLogEnabled()));
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
        logger.log(ERROR, "log");
    }

    /**
     * equals to next
     */
    @Log.in(level = INFO, ifEnabled = WARN, verbose = ERROR, printer = "json")
    @Log.out(level = INFO, ifEnabled = WARN, verbose = ERROR, printer = "json")
    public void inAndOut() {
    }

    @Log(level = INFO, ifEnabled = WARN, verbose = ERROR, printer = "json")
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
    @Log(logger = "simpleLogger")
    @Log(logger = "auditLogger")
    public void logMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
    @Log.in(logger = "simpleLogger")
    @Log.in(logger = "auditLogger")
    public void logInMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
    @Log.out(logger = "simpleLogger")
    @Log.out(logger = "auditLogger")
    public void logOutMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
    @Log.error(logger = "simpleLogger")
    @Log.error(logger = "auditLogger")
    public void logErrorMultiLogger() {
    }

    /**
     * TODO: add javadoc
     */
    public void parameterLogMultiLogger(@Log(logger = "simpleLogger")
                                  @Log(logger = "auditLogger") Dto dto) {
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
