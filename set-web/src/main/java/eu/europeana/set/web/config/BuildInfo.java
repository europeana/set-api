package eu.europeana.set.web.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

@Configuration
@PropertySource("classpath:build.properties")
public class BuildInfo {

    @Value("${info.app.name}")
    private String appName;

    @Value("${info.app.version}")
    private String appVersion;

    @Value("${info.app.description}")
    private String appDescription;

    @Value("${info.build.number}")
    private String buildNumber;

    @Value("${info.build.date}")
    private String timestamp;


    public String getAppName() {
        return appName;
    }

    public String getAppDescription() {
        return appDescription;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getBuildNumber() {
        return buildNumber;
    }

    public String getBuildTimestamp() {
        return timestamp;
    }

    public ZonedDateTime getBuildDateTime() {
        return ZonedDateTime.parse(timestamp, DateTimeFormatter.ISO_ZONED_DATE_TIME);
    }
}
