package eu.europeana.api.set.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import javax.annotation.PostConstruct;

/**
 * Container for all settings that we load from the set-integration-testing.properties file and optionally override from
 * set-integration-testing.user.properties file
 */
@Configuration
@PropertySource("classpath:set-integration-testing.properties")
@PropertySource(value = "classpath:set-integration-testing.user.properties", ignoreResourceNotFound = true)
public class SetSettings {

    private static final Logger LOG = LogManager.getLogger(SetSettings.class);

    @PostConstruct
    private void logImportantSettings() {
        LOG.info("Set integration testing API settings:");
    }
}
