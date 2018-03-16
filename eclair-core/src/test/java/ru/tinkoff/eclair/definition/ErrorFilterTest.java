package ru.tinkoff.eclair.definition;

import org.hamcrest.Matchers;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class ErrorFilterTest {

    @Test(expected = UnsupportedOperationException.class)
    public void getIncludesImmutable() {
        // given
        Set<Class<? extends Throwable>> includes = new HashSet<>();
        // when
        ErrorFilter errorFilter = new ErrorFilter(includes, emptySet());
        // then
        errorFilter.getIncludes().add(NullPointerException.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getExcludesImmutable() {
        // given
        Set<Class<? extends Throwable>> excludes = new HashSet<>();
        // when
        ErrorFilter errorFilter = new ErrorFilter(emptySet(), excludes);
        // then
        errorFilter.getExcludes().add(NullPointerException.class);
    }

    @Test
    public void sortedByDistanceName() {
        // given
        Set<Class<? extends Throwable>> includes =
                new HashSet<>(asList(Error.class, Throwable.class, RuntimeException.class, Exception.class));
        Set<Class<? extends Throwable>> excludes =
                new HashSet<>(asList(IndexOutOfBoundsException.class, RuntimeException.class, NullPointerException.class));
        // when
        ErrorFilter errorFilter = new ErrorFilter(includes, excludes);
        // then
        Set<Class<? extends Throwable>> includesResult = errorFilter.getIncludes();
        assertThat(includesResult, hasSize(4));
        assertThat(includesResult, Matchers.<Class<?>>contains(Throwable.class, Error.class, Exception.class, RuntimeException.class));
        Set<Class<? extends Throwable>> excludesResult = errorFilter.getExcludes();
        assertThat(excludesResult, hasSize(3));
        assertThat(excludesResult, Matchers.<Class<?>>contains(RuntimeException.class, IndexOutOfBoundsException.class, NullPointerException.class));
    }

    @Test
    public void hashCodeEquals() {
        // given
        ErrorFilter errorFilterA =
                new ErrorFilter(new HashSet<>(asList(Exception.class, RuntimeException.class)), new HashSet<>(asList(Throwable.class, Exception.class)));
        ErrorFilter errorFilterB =
                new ErrorFilter(new HashSet<>(asList(RuntimeException.class, Exception.class)), new HashSet<>(asList(Exception.class, Throwable.class)));
        // when
        Set<ErrorFilter> errorFilters = new HashSet<>(asList(errorFilterA, errorFilterB));
        // then
        assertThat(errorFilters, hasSize(1));
    }
}
