package ru.tinkoff.integration.eclair.deprecated.audit.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

@Component
public class XPathProcessor {

    private final MaskProcessor maskProcessor;
    private final DocumentTransformer documentTransformer;

    @Autowired
    public XPathProcessor(MaskProcessor maskProcessor, DocumentTransformer documentTransformer) {
        this.maskProcessor = maskProcessor;
        this.documentTransformer = documentTransformer;
    }

    public String process(String original, String[] mask) {
        Document document = documentTransformer.toDocument(original);
        XPath xPath = XPathFactory.newInstance().newXPath();
        maskProcessor.process(document, xPath, mask);
        return documentTransformer.toXml(document);
    }
}
