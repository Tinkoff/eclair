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

package ru.tinkoff.eclair.core;

import org.aopalliance.aop.Advice;
import org.aopalliance.intercept.MethodInterceptor;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
public class RelationResolverTest {

    @Test
    public void calculateInheritanceDistanceUnassignable() {
        // given
        Class<?> parent = Error.class;
        Class<?> child = RuntimeException.class;
        // when
        int distance = RelationResolver.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(-1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void calculateInheritanceDistanceAssignableInterfaces() {
        // given
        Class<?> parent = Advice.class;
        Class<?> child = MethodInterceptor.class;
        // when
        RelationResolver.calculateInheritanceDistance(parent, child);
        // then expected exception
    }

    @Test
    public void calculateInheritanceDistanceObjects() {
        // given
        Class<?> parent = Object.class;
        Class<?> child = Object.class;
        // when
        int distance = RelationResolver.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(0));
    }

    @Test
    public void calculateInheritanceDistanceEquals() {
        // given
        Class<?> parent = String.class;
        Class<?> child = String.class;
        // when
        int distance = RelationResolver.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(0));
    }

    @Test
    public void calculateInheritanceDistance() {
        // given
        Class<?> parent = Throwable.class;
        Class<?> child = ArrayIndexOutOfBoundsException.class;
        // when
        int distance = RelationResolver.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(4));
    }

    @Test
    public void calculateInheritanceDistanceReverse() {
        // given
        Class<?> parent = ArrayIndexOutOfBoundsException.class;
        Class<?> child = Throwable.class;
        // when
        int distance = RelationResolver.calculateInheritanceDistance(parent, child);
        // then
        assertThat(distance, is(-1));
    }

    @Test
    public void findMostSpecificAncestorEmpty() {
        // given
        Set<Class<?>> parents = Collections.emptySet();
        Class<?> child = Object.class;
        // when
        Class<?> ancestor = RelationResolver.findMostSpecificAncestor(parents, child);
        // then
        assertThat(ancestor, nullValue());
    }

    @Test
    public void findMostSpecificAncestorNotFound() {
        // given
        Set<Class<?>> parents = new HashSet<>(asList(String.class, Integer.class, Void.class));
        Class<?> child = Double.class;
        // when
        Class<?> ancestor = RelationResolver.findMostSpecificAncestor(parents, child);
        // then
        assertThat(ancestor, nullValue());
    }

    @Test(expected = IllegalArgumentException.class)
    public void findMostSpecificAncestorAssignableInterfaces() {
        // given
        Set<Class<?>> parents = Collections.singleton(Serializable.class);
        Class<?> child = Double.class;
        // when
        RelationResolver.findMostSpecificAncestor(parents, child);
        // then expected exception
    }

    @Test
    public void findMostSpecificAncestor() {
        // given
        Set<Class<?>> parents = new HashSet<>(asList(String.class, BigDecimal.class, Object.class, Number.class));
        Class<?> child = Number.class;
        // when
        Class<?> ancestor = RelationResolver.findMostSpecificAncestor(parents, child);
        // then
        assertEquals(Number.class, ancestor);
    }

    @Test
    public void reduceDescendantsEmpty() {
        // given
        List<Class<?>> classes = Collections.emptyList();
        // when
        Set<Class<?>> set = RelationResolver.reduceDescendants(classes);
        // then
        assertThat(set, is(empty()));
    }

    @Test
    public void reduceDescendants() {
        // given
        List<Class<?>> classes = asList(Number.class, BigDecimal.class, ArrayList.class, AbstractList.class);
        // when
        Set<Class<?>> reduced = RelationResolver.reduceDescendants(classes);
        // then
        assertThat(reduced, hasSize(2));
        @SuppressWarnings("unchecked")
        Matcher<Iterable<? extends Class<?>>> matcher = Matchers.containsInAnyOrder(Number.class, AbstractList.class);
        assertThat(reduced, matcher);
    }
}
