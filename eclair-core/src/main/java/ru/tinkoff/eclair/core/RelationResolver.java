package ru.tinkoff.eclair.core;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import static java.lang.String.format;

/**
 * @author Viacheslav Klapatniuk
 */
public final class RelationResolver {

    private RelationResolver() {
    }

    static Class<?> findMostSpecificAncestor(Set<Class<?>> parents, Class<?> child) {
        Class<?> nearest = null;
        int minDistance = Integer.MAX_VALUE;
        for (Class<?> parent : parents) {
            int distance = calculateInheritanceDistance(parent, child);
            if (distance >= 0 && distance < minDistance) {
                nearest = parent;
                minDistance = distance;
            }
        }
        return nearest;
    }

    public static int calculateInheritanceDistance(Class<?> parent, Class<?> child) {
        if (!parent.isAssignableFrom(child)) {
            return -1;
        }
        if (parent.isInterface() || child.isInterface()) {
            throw new IllegalArgumentException(format("Parent/Child classes could not be interfaces: %s, %s", parent, child));
        }
        int distance = 0;
        while (!parent.equals(child)) {
            distance++;
            child = child.getSuperclass();
        }
        return distance;
    }

    static <T> Set<Class<? extends T>> reduceDescendants(Iterable<Class<? extends T>> classes) {
        Set<Class<? extends T>> result = new HashSet<>();
        candidates:
        for (Class<? extends T> candidate : classes) {
            Iterator<Class<? extends T>> iterator = result.iterator();
            while (iterator.hasNext()) {
                Class<?> optimized = iterator.next();
                if (optimized.isAssignableFrom(candidate)) {
                    continue candidates;
                }
                if (candidate.isAssignableFrom(optimized)) {
                    iterator.remove();
                }
            }
            result.add(candidate);
        }
        return result;
    }
}
