package eu.europeana.set.web.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.stereotype.Service;
import eu.europeana.api.commons_sb3.error.AbstractRequestPathMethodService;
/** This service is used to populate the Allow header in API responses. */
@Service
public class RequestPathMethodService extends AbstractRequestPathMethodService {
  @Autowired
  public RequestPathMethodService(WebApplicationContext applicationContext) {
    super(applicationContext);
  }
}