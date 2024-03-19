package eu.europeana.set.web.config;

import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;

/**
 * Class for configuration of SpringDoc 
 */
@Configuration
@OpenAPIDefinition(servers = {
    @Server(url = "/", description = "Default Server URL")
 })
public class SpringDocConfig {

    private final BuildProperties buildProperties;

    /**
     * Initialize SpringDoc with API build information
     * @param buildProperties object for retrieving build information
     */
    public SpringDocConfig(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    /**
     * create OpenAPI bean with required information
     * @return the opeApi bean
     */
    @Bean
    public OpenAPI userServiceOpenAPI() {
        return new OpenAPI().info(new Info().title(buildProperties.getName())
                        .description(buildProperties.get("build.project.description"))
                        .version(buildProperties.get("build.version"))
                        .contact(new Contact().name("API team").url("https://api.europeana.eu").email("api@europeana.eu"))
                        .termsOfService("https://www.europeana.eu/en/rights/api-terms-of-use")
                        .license(new License().name("EUPL 1.2").url("https://www.eupl.eu")))
                .externalDocs(new ExternalDocumentation()
                        .description("Documentation")
                        .url("https://pro.europeana.eu/page/intro#general"));
    }

}
