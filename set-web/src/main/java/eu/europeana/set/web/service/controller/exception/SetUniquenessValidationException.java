package eu.europeana.set.web.service.controller.exception;

import org.springframework.http.HttpStatus;

import eu.europeana.api.commons.web.exception.HttpException;

public class SetUniquenessValidationException extends HttpException{

  private static final long serialVersionUID = 3019030076630195124L;

  public SetUniquenessValidationException(String message, String i18nKey, String[] i18nParams){
		this(message, i18nKey, i18nParams, null);
	}
	
	public SetUniquenessValidationException(String message, String i18nKey, String[] i18nParams, Throwable th){
		this(message, i18nKey, i18nParams, HttpStatus.BAD_REQUEST, th);
	}
	
	public SetUniquenessValidationException(String message, String i18nKey, String[] i18nParams, HttpStatus status, Throwable th){
		super(message, i18nKey, i18nParams, status, th);
	}
}
