package ru.tinkoff.eclair.printer.processor;

import lombok.Setter;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
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
 * @author Viacheslav Klapatniuk
 */
public class XPathMasker implements PrinterPostProcessor {

    private final DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
    private final XPathFactory xPathfactory = XPathFactory.newInstance();
    private final TransformerFactory transformerFactory = TransformerFactory.newInstance();

    private final List<String> expressions;

    @Setter
    private String textContent;

    public XPathMasker(String... expressions) {
        this.expressions = asList(expressions);
    }

    @Override
    public String process(String string) {
        if (expressions.isEmpty()) {
            return string;
        }
        InputStream stream = new ByteArrayInputStream(string.getBytes());
        try {
            Document document = documentBuilderFactory.newDocumentBuilder().parse(stream);
            XPath xPath = xPathfactory.newXPath();
            for (String expression : expressions) {
                ((Node) xPath.compile(expression).evaluate(document, XPathConstants.NODE)).setTextContent(textContent);
            }
            StringWriter writer = new StringWriter();
            transformerFactory.newTransformer().transform(new DOMSource(document), new StreamResult(writer));
            return writer.getBuffer().toString();
        } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException | TransformerException e) {
            throw new RuntimeException(e);
        }
    }
}
