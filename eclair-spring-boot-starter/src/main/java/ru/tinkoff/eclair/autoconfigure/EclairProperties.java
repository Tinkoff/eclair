package ru.tinkoff.eclair.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * TODO: remove manual JSON
 *
 * @author Viacheslav Klapatniuk
 */
@ConfigurationProperties(prefix = "eclair")
public class EclairProperties {

    private boolean validate = true;

    public boolean isValidate() {
        return validate;
    }

    public void setValidate(boolean validate) {
        this.validate = validate;
    }
}
