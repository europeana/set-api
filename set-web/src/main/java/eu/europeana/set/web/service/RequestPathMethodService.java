package eu.europeana.set.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.WebApplicationContext;
import eu.europeana.api.commons_sb3.error.ApiRequestPathMethodService;
/** This service is used to populate the Allow header in API responses. */
@Service
public class RequestPathMethodService extends ApiRequestPathMethodService {
  @Autowired
  public RequestPathMethodService(WebApplicationContext applicationContext) {
    super();
  }
}