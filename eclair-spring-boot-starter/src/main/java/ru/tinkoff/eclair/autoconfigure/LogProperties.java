package ru.tinkoff.eclair.autoconfigure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.logging.LogLevel;

import static org.springframework.boot.logging.LogLevel.DEBUG;

/**
 * TODO: rename prefix
 *
 * @author Viacheslav Klapatniuk
 */
@ConfigurationProperties(prefix = "logging")
@Getter
@Setter
public class LogProperties {

    private LogLevel verboseLevel = DEBUG;
    private boolean validate = true;
}
