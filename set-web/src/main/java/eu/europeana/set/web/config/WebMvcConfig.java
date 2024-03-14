package eu.europeana.set.web.config;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.servlet.config.annotation.ContentNegotiationConfigurer;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import eu.europeana.api.commons.web.http.HttpHeaders;

/**
 * Setup CORS for all requests and setup default Content-type
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

  List<MediaType> supportedMediaTypes = new ArrayList<MediaType>();
  Map<String, MediaType> mediaTypesMaping = new HashMap<String, MediaType>();
  
  private static final MediaType APPLICATION_JSONLD = new MediaType("application", "ld+json");
  private static final String EXTENSION_JSONLD = "jsonld";

  /**
   * Setup CORS for all GET, HEAD and OPTIONS, requests.
   */
  @Override
  public void addCorsMappings(CorsRegistry registry) {
    registry.addMapping("/v3/api-docs").allowedOrigins("*").allowedMethods("GET")
    .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
    .allowCredentials(false).maxAge(600L); // in seconds

    registry.addMapping("/v3/api-docs/**").allowedOrigins("*").allowedMethods("GET")
    .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
    .allowCredentials(false).maxAge(600L); // in seconds


    registry.addMapping("/actuator/**").allowedOrigins("*").allowedMethods("GET")
    .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)
    .allowCredentials(false).maxAge(600L); // in seconds

    // create method
    // delete user's sets by admin, delete by user's sets by 
    registry.addMapping("/set/").allowedOrigins("*").allowedMethods("POST", "DELETE")
        .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ALLOW, HttpHeaders.LINK, HttpHeaders.ETAG, HttpHeaders.VARY,
            HttpHeaders.CACHE_CONTROL, HttpHeaders.PREFERENCE_APPLIED)
        .allowCredentials(false).maxAge(600L); // in seconds

    // get, delete, update
    registry.addMapping("/set/*").allowedOrigins("*").allowedMethods("GET", "PUT", "DELETE")
        .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ALLOW,
            HttpHeaders.LINK, HttpHeaders.ETAG, HttpHeaders.VARY, HttpHeaders.PREFERENCE_APPLIED)
        .allowCredentials(false).maxAge(600L); // in seconds

    //lock/unlock
    registry.addMapping("/set/admin/lock").allowedOrigins("*").allowedMethods("POST", "DELETE")
    .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ALLOW)
    .allowCredentials(false).maxAge(600L); // in seconds

    
    // publish/unpublish
    registry.addMapping("/set/*/*").allowedOrigins("*").allowedMethods("PUT")
        .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ALLOW, 
            HttpHeaders.ETAG, HttpHeaders.VARY, HttpHeaders.PREFERENCE_APPLIED)
        .allowCredentials(false).maxAge(600L); // in seconds

    //add,remove,exists item in set
    registry.addMapping("/set/*/*/*").allowedOrigins("*").allowedMethods("GET", "HEAD", "PUT", "DELETE")
    .exposedHeaders(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, HttpHeaders.ALLOW, HttpHeaders.PREFERENCE_APPLIED)
    .allowCredentials(false).maxAge(600L); // in seconds
  }

  /*
   * Enable content negotiation via path extension (as long as Spring supports it) and set default
   * content type in case we receive a request without an extension or Accept header
   */
  @Override
  public void configureContentNegotiation(ContentNegotiationConfigurer configurer) {
    // Enable content negotiation via path extension. Note that this is deprecated with Spring
    // 5.2.4,
    // (see also https://github.com/spring-projects/spring-framework/issues/24179), so it may not
    // work in future
    // releases
    configurer.favorPathExtension(true);
    
    // use registered extensions instead of defaults
    configurer.useRegisteredExtensionsOnly(true);

    configurer.mediaTypes(getMediaTypesMapping());

    
    // use application/ld+json if no Content-Type is specified
    //configurer.defaultContentType(MediaType.valueOf(HttpHeaders.CONTENT_TYPE_JSONLD));
    configurer.defaultContentType(MediaType.valueOf(HttpHeaders.CONTENT_TYPE_JSON_UTF8),
        MediaType.valueOf(HttpHeaders.CONTENT_TYPE_JSONLD));

    configurer.mediaTypes(getMediaTypesMapping());
  }

  private Map<String, MediaType> getMediaTypesMapping() {
    if (mediaTypesMaping.isEmpty()) {
      for (MediaType mediaType : supportedMediaTypes) {
        if(APPLICATION_JSONLD.equals(mediaType)){
          mediaTypesMaping.put(EXTENSION_JSONLD, mediaType);
        }
        mediaTypesMaping.put(mediaType.getSubtype(), mediaType);
      }
    }

    return mediaTypesMaping;
  }

  @Bean
  public HttpMessageConverter<String> getStringHttpMessageConverter() {
    StringHttpMessageConverter stringConverter =
        new StringHttpMessageConverter(StandardCharsets.UTF_8);
    stringConverter.setWriteAcceptCharset(false);
    stringConverter.setSupportedMediaTypes(getSupportedMediaTypes());
    return stringConverter;
  }

  private List<MediaType> getSupportedMediaTypes() {
    if (supportedMediaTypes.isEmpty()) {
      supportedMediaTypes.add(APPLICATION_JSONLD);
      supportedMediaTypes.add(MediaType.APPLICATION_JSON);
      supportedMediaTypes.add(MediaType.APPLICATION_XML);
      supportedMediaTypes.add(MediaType.TEXT_PLAIN);
      supportedMediaTypes.add(MediaType.TEXT_HTML);
    }
    return supportedMediaTypes;
  }

}
