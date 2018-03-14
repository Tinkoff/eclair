package ru.tinkoff.integration.eclair.core;

/**
 * @author Viacheslav Klapatniuk
 */
public final class ClassInvokerResolver {

    private static final ClassInvokerResolver instance = new ClassInvokerResolver();

    private ClassInvokerResolver() {
    }

    public static ClassInvokerResolver getInstance() {
        return instance;
    }

    public StackTraceElement resolve(Class<?> clazz) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int a = stackTrace.length - 2; a >= 0; a--) {
            if (stackTrace[a].getClassName().equals(clazz.getName())) {
                return stackTrace[a + 1];
            }
        }
        throw new IllegalArgumentException("Invalid stacktrace");
    }
}
