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

package ru.tinkoff.eclair.printer.processor;

import org.apache.commons.io.IOUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.xmlunit.matchers.CompareMatcher;
import org.xmlunit.matchers.EvaluateXPathMatcher;
import org.xmlunit.matchers.HasXPathMatcher;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

/**
 * @author Vyacheslav Klapatnyuk
 */
@RunWith(SpringRunner.class)
@ContextConfiguration(classes = XPathMaskerTest.class)
public class XPathMaskerTest {

    @Value("classpath:ru/tinkoff/eclair/printer/processor/single.xml")
    private Resource single;
    @Value("classpath:ru/tinkoff/eclair/printer/processor/multiple.xml")
    private Resource multiple;
    @Value("classpath:ru/tinkoff/eclair/printer/processor/none.xml")
    private Resource none;

    @Test
    public void replaceOneInSingleByNull() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("/container/user/password");
        String input = getResourceContent(single);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is("")));
    }

    @Test
    public void replaceOneInSingleByReplacement() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("/container/user/password");
        String replacement = "********";
        xPathMasker.setReplacement(replacement);
        String input = getResourceContent(single);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is(replacement)));
    }

    @Test
    public void replaceAllInSingleByNull() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("//password");
        String input = getResourceContent(single);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is("")));
    }

    @Test
    public void replaceAllInMultipleByNull() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("//password");
        String input = getResourceContent(multiple);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is("")));
        assertThat(actual, HasXPathMatcher.hasXPath("/container/payload/payload/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/payload/payload/password/text()", is("")));
    }

    @Test
    public void replaceAllInSingleByReplacement() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("//password");
        String replacement = "********";
        xPathMasker.setReplacement(replacement);
        String input = getResourceContent(single);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is(replacement)));
    }

    @Test
    public void replaceAllInMultipleByReplacement() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("//password");
        String replacement = "********";
        xPathMasker.setReplacement(replacement);
        String input = getResourceContent(multiple);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is(replacement)));
        assertThat(actual, HasXPathMatcher.hasXPath("/container/payload/payload/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/payload/payload/password/text()", is(replacement)));
    }

    @Test
    public void replaceSeveralInMultipleByReplacement() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("/container/user/password", "/container/payload/payload/password");
        String replacement = "********";
        xPathMasker.setReplacement(replacement);
        String input = getResourceContent(multiple);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, HasXPathMatcher.hasXPath("/container/user/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/user/password/text()", is(replacement)));
        assertThat(actual, HasXPathMatcher.hasXPath("/container/payload/payload/password"));
        assertThat(actual, EvaluateXPathMatcher.hasXPath("/container/payload/payload/password/text()", is(replacement)));
    }

    @Test
    public void replaceSeveralInNoneByNull() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker("/container/user/password", "/container/payload/payload/password");
        String input = getResourceContent(none);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, CompareMatcher.isSimilarTo(input));
    }

    @Test
    public void replaceNothing() throws IOException {
        // given
        XPathMasker xPathMasker = new XPathMasker();
        String input = getResourceContent(multiple);
        // when
        String actual = xPathMasker.process(input);
        // then
        assertThat(actual, is(input));
    }

    @Test(expected = IllegalArgumentException.class)
    public void replaceInInvalidXml() {
        // given
        XPathMasker xPathMasker = new XPathMasker("//password");
        String input = "Invalid XML";
        // when
        xPathMasker.process(input);
        // then expected exception
    }

    private String getResourceContent(Resource resource) throws IOException {
        try (InputStream stream = resource.getInputStream()) {
            return IOUtils.toString(stream, Charset.defaultCharset());
        }
    }
}
