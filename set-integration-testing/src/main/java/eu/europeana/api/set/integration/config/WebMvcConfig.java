package eu.europeana.api.set.integration.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Setup CORS for all requests and setup default Content-type
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    /**
     * Setup CORS for all requests.
     * Note that this doesn't work for the Swagger endpoint
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // be careful when modifying this that we continue to support CORS for error responses
        registry.addMapping("/**").allowedOrigins("*").maxAge(1000L);
    }

    /**
     * Enable content negotiation via path extension (as long as Spring supports it) and set default content type in
     * case we get request without an extension or Accept header
     */
    @Override
    public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
        // Enable content negotiation via path extension. Note that this is deprecated with Spring 5.2.4,
        // (see also https://github.com/spring-projects/spring-framework/issues/24179), so it may not work in future
        // releases
        configurer.favorPathExtension(true);

        // set json as default answer, even if no accept header or extension was provided
        configurer.defaultContentType(MediaType.valueOf(MediaType.APPLICATION_JSON_VALUE), MediaType.APPLICATION_JSON);
    }

}
