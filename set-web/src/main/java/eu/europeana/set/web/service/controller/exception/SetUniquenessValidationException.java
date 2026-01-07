package eu.europeana.set.web.service.controller.exception;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;

import java.util.List;

public class SetUniquenessValidationException extends EuropeanaI18nApiException {

  private static final long serialVersionUID = 3019030076630195124L;

  public SetUniquenessValidationException(String message, String i18nKey, List<String> i18nParams){
		this(message, i18nKey, i18nParams, null);
	}
	
	public SetUniquenessValidationException(String message, String i18nKey, List<String> i18nParams, Throwable th){
		this(message, i18nKey, i18nParams, HttpStatus.BAD_REQUEST, th);
	}
	
	public SetUniquenessValidationException(String message, String i18nKey, List<String> i18nParams, HttpStatus status, Throwable th){
		super(message, null, null, status, i18nKey, i18nParams, th);
	}
}