package ru.tinkoff.integration.eclair.deprecated.audit.wrapper;

import java.util.Iterator;

class PackageNameIterator implements Iterator<String> {

    private String next;

    PackageNameIterator(Class<?> payloadClass) {
        next = payloadClass.getPackage().getName();
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    @Override
    public String next() {
        String result = next;
        int lastIndexOfDot = next.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            next = null;
        } else {
            next = next.substring(0, lastIndexOfDot);
        }
        return result;
    }
}
