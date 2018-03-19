package ru.tinkoff.eclair.definition.factory;

import org.hamcrest.Matchers;
import org.junit.Test;
import ru.tinkoff.eclair.definition.ErrorLog;

import java.util.HashSet;
import java.util.Set;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.Assert.assertThat;

/**
 * @author Viacheslav Klapatniuk
 */
public class FilterTest {

    @Test(expected = UnsupportedOperationException.class)
    public void getIncludesImmutable() {
        // given
        Set<Class<? extends Throwable>> includes = new HashSet<>();
        // when
        ErrorLog.Filter filter = new ErrorLog.Filter(includes, emptySet());
        // then
        filter.getIncludes().add(NullPointerException.class);
    }

    @Test(expected = UnsupportedOperationException.class)
    public void getExcludesImmutable() {
        // given
        Set<Class<? extends Throwable>> excludes = new HashSet<>();
        // when
        ErrorLog.Filter filter = new ErrorLog.Filter(emptySet(), excludes);
        // then
        filter.getExcludes().add(NullPointerException.class);
    }

    @Test
    public void sortedByDistanceName() {
        // given
        Set<Class<? extends Throwable>> includes =
                new HashSet<>(asList(Error.class, Throwable.class, RuntimeException.class, Exception.class));
        Set<Class<? extends Throwable>> excludes =
                new HashSet<>(asList(IndexOutOfBoundsException.class, RuntimeException.class, NullPointerException.class));
        // when
        ErrorLog.Filter filter = new ErrorLog.Filter(includes, excludes);
        // then
        Set<Class<? extends Throwable>> includesResult = filter.getIncludes();
        assertThat(includesResult, hasSize(4));
        assertThat(includesResult, Matchers.<Class<?>>contains(Throwable.class, Error.class, Exception.class, RuntimeException.class));
        Set<Class<? extends Throwable>> excludesResult = filter.getExcludes();
        assertThat(excludesResult, hasSize(3));
        assertThat(excludesResult, Matchers.<Class<?>>contains(RuntimeException.class, IndexOutOfBoundsException.class, NullPointerException.class));
    }

    @Test
    public void hashCodeEquals() {
        // given
        ErrorLog.Filter filterA =
                new ErrorLog.Filter(new HashSet<>(asList(Exception.class, RuntimeException.class)), new HashSet<>(asList(Throwable.class, Exception.class)));
        ErrorLog.Filter filterB =
                new ErrorLog.Filter(new HashSet<>(asList(RuntimeException.class, Exception.class)), new HashSet<>(asList(Exception.class, Throwable.class)));
        // when
        Set<ErrorLog.Filter> filters = new HashSet<>(asList(filterA, filterB));
        // then
        assertThat(filters, hasSize(1));
    }
}
