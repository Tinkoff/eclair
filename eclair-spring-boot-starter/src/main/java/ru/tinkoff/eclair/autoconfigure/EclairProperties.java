package ru.tinkoff.eclair.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Viacheslav Klapatniuk
 */
@ConfigurationProperties(prefix = "eclair")
class EclairProperties {

    private boolean validate = true;

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
