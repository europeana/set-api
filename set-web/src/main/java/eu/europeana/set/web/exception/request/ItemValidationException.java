package eu.europeana.set.web.exception.request;

import org.springframework.http.HttpStatus;

import eu.europeana.api.commons.web.exception.HttpException;

public class ItemValidationException extends HttpException{

  private static final long serialVersionUID = -7048717251116426084L;

  public ItemValidationException(String i18nKey, String[] params){
	this(i18nKey, params, null);		
  }
  
  public ItemValidationException(String i18nKey, String[] params, Throwable th){
	super(i18nKey, i18nKey, params, HttpStatus.BAD_REQUEST, th);		
  }
}
