# Eclair

Eclair - Java Spring library for AOP logging.

Provides annotations for declarative logging of annotated method execution.
Includes abstractions for annotations processing, simple implementation and Spring Boot starter with auto-configuration.

## Features

* events logging detected by Spring AOP: *beginning*, *ending* or *emergency ending* of method execution
* flexible filtering [`Throwable`](https://docs.oracle.com/javase/8/docs/api/java/lang/Throwable.html) types for logging
* configurable verbosity based on the enabled log level
* pre-defined printers to log arguments or method return value in different formats:
    * `JSON` (by Jackson)
    * `XML` (by JAXB)
* declarative defining (with *SpEL*) and erasing of *Mapped Diagnostic Context* (MDC) based on scopes
* multiple logger definition
* annotations validation during application context start
* ability to use meta-annotations (applied to other annotations) and annotated method overriding
* manual logging with invoker class detection is also available

## Annotations

Eclair logs annotated [`Method`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html)s and [`Parameter`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Parameter.html)s only.<br>
Implementation is based on standard Spring proxying with all its consequences and limitations.

### [`@Log`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java)

Annotated [`Method`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html) is able to log beginning and ending (except the emergency ending) of execution.<br>
Works the same as both [`@Log.in`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) and [`@Log.out`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) annotations with all matching attribute values.<br>
*Note: emergency ending of the method execution should be specified separately by [`@Log.error`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) annotation.*

Can be defined on [`Parameter`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Parameter.html) and specify logging settings for it.

Should have unique `logger` value per annotated element.<br>
[`Parameter`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Parameter.html)-level annotation has higher priority settings than [`Method`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html)-level with same `logger` value.

 Attribute  | Description
:-----------|:------------
`level`     | Expected level to log beginning and ending of method execution.
`ifEnabled` | Enables logging with `level` only if specified here level is enabled for the current `logger` too.<br> Ignored by default.
`verbose`   | If specified log-level is enabled for the current `logger` activates detailed logging.<br> For annotated [`Method`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Method.html) verbose log includes argument/return values.<br>For annotated [`Parameter`](https://docs.oracle.com/javase/8/docs/api/java/lang/reflect/Parameter.html) verbose log includes argument name.<br> *Note: it is assumed that `OFF` deactivates verbose logging of annotated element for any level.*
`printer`   | Determines [`Printer`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/printer/Printer.java) implementation by specified bean name (or alias).<br> The printer will be used to convert argument/return values from raw type to [`String`](https://docs.oracle.com/javase/8/docs/api/java/lang/String.html).<br> *Note: if not specified highest priority compatible printer or [`PrinterResolver#defaultPrinter`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/printer/resolver/PrinterResolver.java) will be used.*
`logger`    | Determines [`EclairLogger`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/logger/EclairLogger.java) implementation by specified bean name (or alias) which should process this annotation.<br> *Note: if not specified single candidate or `Primary` bean will be used for processing.*

> See also [`@Log.in`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java), [`@Log.out`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) and [`@Log.error`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) annotations and their specific attributes

### [`@Mdc`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Mdc.java)

Defines MDC (Mapped Diagnostic Context) entry. MDC is level-insensitive.<br>
Before method execution beginning, [`@Mdc`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Mdc.java) will be processed first and after ending cleared last.<br>
So annotations [`@Log`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) / [`@Log.in`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) / [`@Log.out`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) of the same method will be processed *inside* [`@Mdc`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Mdc.java) processing.

 Attribute | Description
:----------|:------------
`key`      | Key of the MDC entry.<br> If empty, it will be synthesized by code meta-data: annotated method or parameter name.<br> *Note: It is not always possible to obtain information about parameter names at runtime.<br> In that case, MDC keys will contain method name and parameter index.*
`value`    | Value of the MDC entry.<br> Can contain SpEL (Spring Expression Language) and invoke static methods or beans by id from the `ApplicationContext`.<br> If empty, it will be synthesized by code meta-data: annotated parameter value (or each parameter of annotated method).
`global`   | Key/value pair defined by this annotation automatically cleared after exit from the method by default.<br> `global` MDC is available within [`ThreadLocal`](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html) scope.

## Getting started

> Eclair compatible with Java 8, Spring Boot 1.5.0+.

Add this to your POM:
```xml
<dependency>
    <groupId>ru.tinkoff</groupId>
    <artifactId>eclair-spring-boot-starter</artifactId>
    <version>0.9.0</version>
</dependency>
```

## Usage examples

The examples assume that you are using a standard [`SimpleLogger`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/logger/SimpleLogger.java) and that you have the following configuration property:
```yaml
logging.pattern.console: %d{yyyy-MM-dd HH:mm:ss.SSS UTC} [%thread] %-5level [%X] %logger{35} %msg%n
```
All available log levels in order from the most common `TRACE` to the rarest `OFF`:
 * `OFF` deactivates logging completely
 * `ERROR` and `FATAL` are identical (`ERROR` is used everywhere)
```
TRACE > DEBUG > INFO > WARN > ERROR = FATAL > OFF
```
> Used Spring Boot log levels enum: `org.springframework.boot.logging.LogLevel`

### Declarative logging
The left table column shows the configured available logging level for the current method.<br>
The right column shows a log sample or the specified level.

#### Simplest
`DEBUG` level by default.
```java
@Log
void simple() {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.simple >`<br>`DEBUG [] r.t.eclair.example.Example.simple <`
 `INFO` .. `OFF`    | -

#### Influence of configured level to verbosity
```java
@Log(INFO)
boolean verbose(String s, Integer i, Double d) {
    return false;
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `INFO  [] r.t.eclair.example.Example.verbose > s="s", i=4, d=5.6`<br>`INFO  [] r.t.eclair.example.Example.verbose < false`
 `INFO`             | `INFO  [] r.t.eclair.example.Example.verbose >`<br>`INFO  [] r.t.eclair.example.Example.verbose <`
 `WARN` .. `OFF`    | -

#### Try to print arguments by [`Jaxb2Printer`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/printer/Jaxb2Printer.java) as `XML`
You can specify printer's bean name or alias.
```java
@Log(printer = "jaxb2Printer")
void verboseXml(Dto dto, Integer i) {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.xml > dto=<dto><i>4</i><s>k</s></dto>, i=7`<br>`DEBUG [] r.t.eclair.example.Example.xml <`
 `INFO` .. `OFF`    | -

#### Filter errors by type
Errors can be filtered multiple times by `ofType` and `exclude` attributes.<br>
By default `ofType` contains [`Throwable`](https://docs.oracle.com/javase/8/docs/api/java/lang/Throwable.html) and includes all subtypes.<br>
If the thrown exception matches any of [`@Log.error`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) filters it will be logged according to the settings of the corresponding annotation.  
##### Annotated method
```java
@Log.error(level = WARN, ofType = {NullPointerException.class, IndexOutOfBoundsException.class})
@Log.error(exclude = Error.class)
void filterErrors(Throwable throwable) throws Throwable {
    throw throwable;
}
```
##### Invocation statement
```java
filterErrors(new NullPointerException());
filterErrors(new Exception());
filterErrors(new Error());
```
##### Result log
 Enabled level      | Log sample
--------------------|------------
 `TRACE` .. `WARN`  | `WARN  [] r.t.e.example.Example.filterErrors ! java.lang.NullPointerException`<br>`java.lang.NullPointerException: null`<br>`	at ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..<br>`ERROR [] r.t.e.example.Example.filterErrors ! java.lang.Exception`<br>`java.lang.Exception: null`<br>`	at ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..
 `ERROR` `FATAL`    | `ERROR [] r.t.e.example.Example.filterErrors ! java.lang.Exception`<br>`java.lang.Exception: null`<br>`	at ru.tinkoff.eclair.example.ExampleTest.filterErrors(ExampleTest.java:0)`<br>..
 `OFF`              | -

#### Log annotated argument only
> Note: If method is not annotated, log string will have the highest level among annotated parameters.<br>
> Note: Parameter name printed for `TRACE` and `DEBUG` levels by default.
```java
void parameter(@Log(INFO) Dto dto, String s, Integer i) {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `INFO  [] r.t.e.example.Example.parameter > dto=Dto{i=0, s='u'}`
 `INFO`             | `INFO  [] r.t.e.example.Example.parameter > Dto{i=0, s='u'}`
 `WARN` .. `OFF`    | -

#### Multiple loggers in application
You can have several [`EclairLogger`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/logger/EclairLogger.java) implementations in your application context.<br>
This can be useful for logging various slices of information to different targets.
> If `logger` attribute not defined, single candidate or `@Primary` bean will be used.
```java
@Log
@Log(logger = "auditLogger")
void twoLoggers() {
}
```

### Declarative MDC management
MDC - Mapped Diagnostic Context

Key/value pair defined by annotation automatically cleared after exit from the method.<br>
`global` MDC is available within [`ThreadLocal`](https://docs.oracle.com/javase/8/docs/api/java/lang/ThreadLocal.html) scope.<br>
`value` attribute can contain SpEL expression and invoke static methods or beans by id from the application context.<br>
> Note: MDC is level-insensitive and printed every time.<br>
> Note: MDC does not guarantee order of elements when printing.

#### Common usage
Before method execution beginning, [`@Mdc`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Mdc.java) annotations will be processed first and after ending cleared last.<br>
So annotations [`@Log`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) / [`@Log.in`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) / [`@Log.out`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Log.java) of the same method will be processed *inside* [`@Mdc`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/annotation/Mdc.java) processing. 
```java
@Log
void outer() {
    self.mdc();
}

@Mdc(key = "static", value = "string")
@Mdc(key = "sum", value = "1 + 1", global = true)
@Mdc(key = "beanReference", value = "@jacksonPrinter.print(new ru.tinkoff.eclair.example.Dto())")
@Mdc(key = "staticMethod", value = "T(java.util.UUID).randomUUID()")
@Log
void mdc() {
    self.inner();
}

@Log.in
void inner() {
}
```
##### Log sample
```
DEBUG [] r.t.eclair.example.Example.outer >
DEBUG [beanReference={"i":0,"s":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.mdc >
DEBUG [beanReference={"i":0,"s":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.inner >
DEBUG [beanReference={"i":0,"s":null}, sum=2, static=string, staticMethod=01234567-89ab-cdef-ghij-klmnopqrstuv] r.t.eclair.example.Example.mdc <
DEBUG [sum=2] r.t.eclair.example.Example.outer <
```

#### MDC defined by argument
MDC can get access to annotated parameter value with SpEL as root object of evaluation context.
```java
@Log.in
void mdcByArgument(@Mdc(key = "dto", value = "#this")
                   @Mdc(key = "length", value = "s.length()") Dto dto) {
}
```
##### Log sample
```
DEBUG [length=8, dto=Dto{i=12, s='password'}] r.t.e.example.Example.mdcByArgument > dto=Dto{i=12, s='password'}
```

### Manual logging
Inject [`ManualLogger`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/logger/ManualLogger.java) implementation for manual logging.<br>
> If execution time is important to you, manual logging with [`ManualLogger`](https://github.com/TinkoffCreditSystems/eclair/blob/master/eclair-core/src/main/java/ru/tinkoff/eclair/logger/ManualLogger.java) is not recommended.

#### Manual
Expensive calculations may be wrapped into [`Supplier`](https://docs.oracle.com/javase/8/docs/api/java/util/function/Supplier.html) for lazy initialization.
```java
@Autowired
private ManualLogger logger;

@Log
void manual() {
    logger.info("Eager logging: {}", Math.PI);
    logger.debug("Lazy logging: {}", (Supplier) () -> Math.PI);
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.manual >`<br>`INFO  [] r.t.eclair.example.Example.manual - Eager logging: 3.141592653589793`<br>`DEBUG [] r.t.eclair.example.Example.manual - Lazy logging: 3.141592653589793`<br>`DEBUG [] r.t.eclair.example.Example.manual <`
 `INFO`             | `INFO  [] r.t.eclair.example.Example.manual - Eager logging: 3.141592653589793`
 `WARN` .. `OFF`    | -

## Release History

19.05.2023 - `0.9.0` SpringBoot 3.0 support<br>
21.01.2022 - `0.8.4` Make SimpleLogger more extendable<br>
07.05.2018 - `0.8.3` Corrected optional classes usage in auto-configuration<br>
06.05.2018 - `0.8.2` Published on Maven Central Repository<br>
25.04.2018 - `0.8.1` Removed Lombok dependency<br>
24.04.2018 - `0.8.0` Basic features

## License

```
Copyright 2018 Tinkoff Bank
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
