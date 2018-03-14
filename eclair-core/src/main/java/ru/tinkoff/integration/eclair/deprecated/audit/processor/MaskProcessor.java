package ru.tinkoff.integration.eclair.deprecated.audit.processor;

import org.springframework.stereotype.Component;
import org.w3c.dom.Node;

@Component
class MaskProcessor extends NodeProcessor {

    @Override
    void processNode(Node node) {
        node.setTextContent("********");
    }
}
