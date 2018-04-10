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

import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

import static java.util.Arrays.asList;

/**
 * TODO: add tests
 *
 * @author Viacheslav Klapatniuk
 */
public class XPathMasker implements PrinterPostProcessor {

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private final List<String> xPathExpressions;

    @Setter
    private String replacement;

    public XPathMasker(String... xPathExpressions) {
        this.xPathExpressions = asList(xPathExpressions);
    }

    @Override
    public String process(String string) {
        if (xPathExpressions.isEmpty()) {
            return string;
        }
        InputStream stream = new ByteArrayInputStream(string.getBytes());
        try {
            Document document = documentBuilderFactory.newDocumentBuilder().parse(stream);
            XPath xPath = xPathfactory.newXPath();
            for (String xPathExpression : xPathExpressions) {
                NodeList nodeList = (NodeList) xPath.compile(xPathExpression).evaluate(document, XPathConstants.NODESET);
                for (int a = 0; a < nodeList.getLength(); a++) {
                    nodeList.item(a).setTextContent(replacement);
                }
            }
            StringWriter writer = new StringWriter();
            transformerFactory.newTransformer().transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
