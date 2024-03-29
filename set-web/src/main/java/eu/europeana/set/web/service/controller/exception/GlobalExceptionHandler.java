package eu.europeana.set.web.service.controller.exception;

import javax.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.error.EuropeanaApiErrorResponse;
import eu.europeana.api.commons.web.exception.EuropeanaGlobalExceptionHandler;
import eu.europeana.set.web.config.BeanNames;
import eu.europeana.set.web.service.RequestPathMethodService;

/**
 * Controller for handling application exceptions
 */
@ControllerAdvice
public class GlobalExceptionHandler extends EuropeanaGlobalExceptionHandler {

  I18nService i18nService;

  /**
   * Constructor for the initialization of the Exception handler
   * 
   * @param requestPathMethodService builtin service for path method mapping
   * @param i18nService the internationalization service
   */
  @Autowired
  public GlobalExceptionHandler(RequestPathMethodService requestPathMethodService,
      @Qualifier(BeanNames.BEAN_I18N_SERVICE) I18nService i18nService) {
    this.requestPathMethodService = requestPathMethodService;
    this.i18nService = i18nService;
  }

  @Override
  protected I18nService getI18nService() {
    return i18nService;
  }

  /**
   * HttpMessageNotReadableException thrown when the request body is not parsable to the declared
   * input of the handler method
   * 
   * @param e the exception indicating the request message parsing error
   * @param httpRequest the request object
   * @return the api response
   */
  @ExceptionHandler
  public ResponseEntity<EuropeanaApiErrorResponse> handleRequestBodyNotParsableError(
      HttpMessageNotReadableException e, HttpServletRequest httpRequest) {
    HttpStatus responseStatus = HttpStatus.BAD_REQUEST;
    EuropeanaApiErrorResponse response =
        (new EuropeanaApiErrorResponse.Builder(httpRequest, e, stackTraceEnabled()))
            .setStatus(responseStatus.value()).setError(responseStatus.getReasonPhrase())
            .setMessage("Invalid request body: " + e.getMessage()).setSeeAlso(getSeeAlso()).build();

    return ResponseEntity.status(responseStatus).headers(createHttpHeaders(httpRequest))
        .body(response);
  }
}
