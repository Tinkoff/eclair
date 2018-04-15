# Eclair
Eclair - Java Spring library for AOP logging.

Provides annotations for declarative logging of annotated method execution.
Includes abstractions for annotations processing, simple implementation and Spring Boot starter with auto-configuration.

## Features

* events logging detected by Spring AOP: *beginning*, *ending* or *emergency ending* of method execution
* flexible filtering `Throwable` types for logging
* configurable verbosity based on the enabled log level
* pre-defined printers to log arguments or method return value in different formats:
    * `JSON` (by Jackson)
    * `XML` (by JAXB)
* declarative defining (with *SpEL*) and erasing of *Mapped Diagnostic Context* (MDC) based on scopes
* multiple logger definition
* annotations validation during application context start
* ability to use meta-annotations (applied to other annotations) and annotated method overriding
* manual logging with invoker class detection is also available

## Getting started
Add this to your POM.
```xml
<dependency>
    <groupId>ru.tinkoff</groupId>
    <artifactId>eclair-spring-boot-starter</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage examples
The examples assume that you are using a standard `SimpleLogger` and that you have the following configuration property:
```yaml
logging.pattern.console: '%-5level [%X] %logger{35} %msg%n'
```
All available log levels in order from the most common `TRACE` to the rarest `OFF`:
 * `OFF` deactivates logging completely
 * `ERROR` and `FATAL` are identical (here `ERROR` used)
```
TRACE > DEBUG > INFO > WARN > ERROR = FATAL > OFF
```

### Declarative logging
The left table column shows the configured available logging level for the current method.<br>
The right column shows an sample log for the specified level.

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

#### With thrown exception
```java
@Log
void simpleError() {
    throw new RuntimeException();
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.e.example.Example.simpleError >`<br>`DEBUG [] r.t.e.example.Example.simpleError !`
 `INFO` .. `OFF`    | -

#### Explicit `INFO` level
```java
@Log(INFO)
void level() {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` .. `INFO`  | `INFO  [] r.t.eclair.example.Example.level >`<br>`INFO  [] r.t.eclair.example.Example.level <`
 `WARN` .. `OFF`    | -

#### Log as `INFO` if enabled `DEBUG` level
```java
@Log(level = INFO, ifEnabled = DEBUG)
void ifEnabled() {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `INFO  [] r.t.e.example.Example.ifEnabled >`<br>`INFO  [] r.t.e.example.Example.ifEnabled <`
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

#### Verbosity disabled
Arguments and return value not printed for any level.
```java
@Log(verbose = OFF)
boolean verboseDisabled(String s, Integer i, Double d) {
    return false;
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.e.e.Example.verboseDisabled >`<br>`DEBUG [] r.t.e.e.Example.verboseDisabled <`
 `INFO` .. `OFF`    | -

#### Try to print arguments by `JacksonPrinter` as `JSON`
You can specify printer's bean name or alias. Arguments and return values will be serialized with `#toString()` invocation by default. 
```java
@Log(printer = "jacksonPrinter")
void json(Dto dto, Integer i) {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.json > dto={"i":2,"s":"r"}, i=8`<br>`DEBUG [] r.t.eclair.example.Example.json <`
 `INFO` .. `OFF`    | -

#### Try to print arguments by `Jaxb2Printer` as `XML`
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

#### Separate `in` and `out` events 
Logging of `in` and `out` events could be declared separately with own settings.
```java
@Log.in(INFO)
@Log.out(TRACE)
void inOut(Dto dto, String s, Integer i) {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE`            | `INFO  [] r.t.eclair.example.Example.inOut > dto=Dto{i=3, s='m'}, s="s", i=3`<br>`TRACE [] r.t.eclair.example.Example.inOut <`
 `DEBUG`            | `INFO  [] r.t.eclair.example.Example.inOut > dto=Dto{i=3, s='m'}, s="s", i=3`
 `INFO`             | `INFO  [] r.t.eclair.example.Example.inOut >`
 `WARN` .. `OFF`    | -

#### Error
Errors logged on `ERROR` level by default. So it is visible for all levels except `OFF`. 
```java
@Log.error
void error() {
    throw new RuntimeException("Something strange happened");
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` .. `FATAL` | `ERROR [] r.t.eclair.example.Example.error ! java.lang.RuntimeException: Something strange happened`<br>`java.lang.RuntimeException: Something strange happened`<br>`	at ru.tinkoff.eclair.example.Example.error(Example.java:0)`<br>..
 `OFF`              | -

#### Warning on `DEBUG`
You may want to log minor error, if enabled `DEBUG` level. 
```java
@Log.error(level = WARN, ifEnabled = DEBUG)
void warningOnDebug() {
    throw new RuntimeException("Something strange happened, but it doesn't matter");
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `WARN  [] r.t.e.e.Example.warningOnDebug ! java.lang.RuntimeException: Something strange happened, but it doesn't matter`<br>`java.lang.RuntimeException: Something strange happened, but it doesn't matter`<br>`	at ru.tinkoff.eclair.example.Example.warningOnDebug(Example.java:0)`<br>..
 `INFO` .. `OFF`    | -

#### Filter errors by type
Errors could be filtered multiple times by `ofType` and `exclude` attributes.<br>
By default `ofType` contains `Throwable.class` and includes all subtypes.<br>
If the thrown exception matches any of `@Log.error` filters, it will be logged according to the settings of the corresponding annotation.  
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

#### The most specific error type
If thrown exception matches to several filters, the most specific parent type will be used.<br>
`IllegalArgumentException` is child of `Exception` and `RuntimeException` too, but `RuntimeException` is more specific, so `IllegalArgumentException` logged with `WARN` level. 
```java
@Log.error(level = ERROR, ofType = Exception.class)
@Log.error(level = WARN, ofType = RuntimeException.class)
void mostSpecific() {
    throw new IllegalArgumentException();
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` .. `WARN`  | `WARN  [] r.t.e.example.Example.mostSpecific ! java.lang.IllegalArgumentException`<br>`java.lang.IllegalArgumentException: null`<br>`	at ru.tinkoff.eclair.example.Example.mostSpecific(Example.java:0)`<br>..
 `ERROR` .. `OFF`   | -

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

#### Specific printer for each argument
Printer could have pre- and post-processors for manipulating with data before / after serialization.<br>
For example `maskJaxb2Printer` was configured with `XPathMasker` post-processor, so all elements matched `//s` expression masked by `********`.
```java
@Log.out(printer = "maskJaxb2Printer")
Dto printers(@Log(printer = "maskJaxb2Printer") Dto xml,
             @Log(printer = "jacksonPrinter") Dto json,
             Integer i) {
    return xml;
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE` `DEBUG`    | `DEBUG [] r.t.eclair.example.Example.printers > xml=<dto><i>5</i><s>********</s></dto>, json={"i":5,"s":"password"}`<br>`DEBUG [] r.t.eclair.example.Example.printers < <dto><i>5</i><s>********</s></dto>`
 `INFO` .. `OFF`    | -

#### Configured parameter levels
```java
@Log.in(INFO)
void parameterLevels(@Log(INFO) Double d,
                     @Log(DEBUG) String s,
                     @Log(TRACE) Integer i) {
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE`            | `INFO  [] r.t.e.e.Example.parameterLevels > d=9.4, s="v", i=7`
 `DEBUG`            | `INFO  [] r.t.e.e.Example.parameterLevels > d=9.4, s="v"`
 `INFO`             | `INFO  [] r.t.e.e.Example.parameterLevels > 9.4`
 `WARN` .. `OFF`    | -

#### Mix
```java
@Log.in(INFO)
@Log.out(level = TRACE, verbose = TRACE)
@Log.error(level = WARN, ofType = RuntimeException.class, exclude = NullPointerException.class)
@Log.error(level = ERROR, ofType = {Error.class, Exception.class})
Dto mix(@Log(printer = "jaxb2Printer") Dto xml,
        @Log(ifEnabled = TRACE, printer = "jacksonPrinter") Dto json,
        Integer i) {
    throw new IllegalArgumentException("Something strange happened");
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE`            | `INFO  [] r.t.eclair.example.Example.mix > xml=<dto><i>5</i><s>a</s></dto>, json={"i":7,"s":"b"}, i=1`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`	at ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..
 `DEBUG`            | `INFO  [] r.t.eclair.example.Example.mix > xml=<dto><i>5</i><s>a</s></dto>, i=1`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`	at ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..
 `INFO`             | `INFO  [] r.t.eclair.example.Example.mix >`<br>`WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`	at ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..
 `WARN`             | `WARN  [] r.t.eclair.example.Example.mix ! java.lang.IllegalArgumentException: Something strange happened`<br>`java.lang.IllegalArgumentException: Something strange happened`<br>`	at ru.tinkoff.eclair.example.Example.mix(Example.java:0)`<br>..
 `ERROR` .. `OFF`   | -

#### Meta-annotation possibilities
You can define your own custom annotations that are an amalgamation of many Eclair annotations combined into one annotation.
```java
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Log(level = INFO, printer = "xml")
@Log.error(ofType = RuntimeException.class)
@interface Audit {
}
```
So, if annotation above defined, following examples work absolutely identical.
```java
@Log(level = INFO, printer = "xml")
@Log.error(ofType = RuntimeException.class)
void listing() {
}

@Audit
void meta() {
}
```

#### Multiple loggers in application
You can have several `EclairLogger` implementations in your application context.<br>
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
`global` MDC is available within `ThreadLocal` scope.<br>
`value` attribute could contain SpEL expression and invoke static methods or beans by id from the application context.<br>
> Note: MDC is level-insensitive and printed every time.<br>
> Note: MDC does not guarantee order of elements when printing.

#### Common usage
Before method execution beginning, `@Mdc` annotations will be processed first and after ending cleared last.<br>
So annotations `@Log` / `@Log.in` / `@Log.out` of the same method will be processed *inside* `@Mdc` processing. 
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
MDC could get access to annotated parameter value with SpEL as root object of evaluation context.
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

#### MDC defined by default
If `key` or `value` of `@Mdc` annotation is empty, it will be synthesized by code meta-data:<br>
* if `@Mdc` defined above the method with empty `value`, each method's parameter will be saved as MDC entry
* if `@Mdc` defined above the parameter:
    * empty `key` will be replaced by annotated parameter name
    * empty `value` will be replaced by annotated parameter value
```java
@Mdc
@Log.in
void mdcByDefault(String first, Double second) {
}
```
##### Log sample
```
DEBUG [first=content, second=3.141592653589793] r.t.e.example.Example.mdcByDefault > first="content", second=3.141592653589793
```
> It is not always possible to obtain information about parameter names at runtime.<br>
In that case, MDC keys will contain method name and parameter index.
```
DEBUG [mdcByDefault[0]=content, mdcByDefault[1]=3.141592653589793] r.t.e.example.Example.mdcByDefault > first="content", second=3.141592653589793
```

### Manual logging
Inject `ManualLogger` implementation for manual logging.<br>
> If execution time is important to you, manual logging with `ManualLogger` is not recommended.

#### Manual
Expensive calculations may be wrapped into Java 8 `Supplier` interface for lazy initialization.
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

#### Manual level control
Complete example of manual logging with different level settings.
```java
@Autowired
private ManualLogger logger;

@Log
void manualLevel() {
    // log ERROR
    logger.error("ERROR");
    logger.errorIfWarnEnabled("ERROR if WARN enabled");
    logger.errorIfInfoEnabled("ERROR if INFO enabled");
    logger.errorIfDebugEnabled("ERROR if DEBUG enabled");
    logger.errorIfTraceEnabled("ERROR if TRACE enabled");
    // log WARN
    logger.warn("WARN");
    logger.warnIfInfoEnabled("WARN if INFO enabled");
    logger.warnIfDebugEnabled("WARN if DEBUG enabled");
    logger.warnIfTraceEnabled("WARN if TRACE enabled");
    // log INFO
    logger.info("INFO");
    logger.infoIfDebugEnabled("INFO if DEBUG enabled");
    logger.infoIfTraceEnabled("INFO if TRACE enabled");
    // log DEBUG
    logger.debug("DEBUG");
    logger.debugIfTraceEnabled("DEBUG if TRACE enabled");
    // log TRACE
    logger.trace("TRACE");
}
```
 Enabled level      | Log sample
--------------------|------------
 `TRACE`            | `DEBUG [] r.t.e.example.Example.manualLevel >`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if DEBUG enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if TRACE enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if DEBUG enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if TRACE enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if DEBUG enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if TRACE enabled`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG if TRACE enabled`<br>`TRACE [] r.t.e.example.Example.manualLevel - TRACE`<br>`DEBUG [] r.t.e.example.Example.manualLevel <`
 `DEBUG`            | `DEBUG [] r.t.e.example.Example.manualLevel >`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if DEBUG enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if DEBUG enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO if DEBUG enabled`<br>`DEBUG [] r.t.e.example.Example.manualLevel - DEBUG`<br>`DEBUG [] r.t.e.example.Example.manualLevel <`
 `INFO`             | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if INFO enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN if INFO enabled`<br>`INFO  [] r.t.e.example.Example.manualLevel - INFO`
 `WARN`             | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`<br>`ERROR [] r.t.e.example.Example.manualLevel - ERROR if WARN enabled`<br>`WARN  [] r.t.e.example.Example.manualLevel - WARN`
 `ERROR` `FATAL`    | `ERROR [] r.t.e.example.Example.manualLevel - ERROR`
 `OFF`              | -

## Release History

04.2018 - 1.0.0 Basic features

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