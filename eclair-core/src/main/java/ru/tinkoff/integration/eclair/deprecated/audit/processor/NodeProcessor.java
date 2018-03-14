package ru.tinkoff.integration.eclair.deprecated.audit.processor;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import static java.lang.String.format;
import static java.util.Arrays.asList;

abstract class NodeProcessor {

    void process(Document document, XPath xPath, String... expressions) {
        asList(expressions).forEach(expression -> {
            XPathExpression xPathExpression = getXPathExpression(xPath, expression);
            NodeList nodeList = getNodeList(document, xPathExpression);
            processNodeList(nodeList);
        });
    }

    abstract void processNode(Node node);

    private void processNodeList(NodeList nodeList) {
        for (int index = 0; index < nodeList.getLength(); index++) {
            processNode(nodeList.item(index));
        }
    }

    private XPathExpression getXPathExpression(XPath xPath, String expression) {
        try {
            return xPath.compile(expression);
        } catch (XPathExpressionException e) {
            throw new RuntimeException(format("XPath expression could not be compiled: '%s'", expression), e);
        }
    }

    private NodeList getNodeList(Document document, XPathExpression xPathExpression) {
        try {
            return (NodeList) xPathExpression.evaluate(document, XPathConstants.NODESET);
        } catch (XPathExpressionException e) {
            throw new RuntimeException("XPath expression could not be evaluated");
        }
    }
}
