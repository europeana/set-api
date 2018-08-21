package eu.europeana.set.web.exception.request;

import org.springframework.http.HttpStatus;

import eu.europeana.api.commons.web.exception.HttpException;

public class RequestBodyValidationException extends HttpException{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3364526076494279093L;
	
//	private String bodyValue;
	
//	public static String MESSAGE_PARSE_BODY = " Cannot parse body to user set! ";
	
	
	public RequestBodyValidationException(String i18nKey, String[] params){
		this(i18nKey, params, null);		
	}
	
	public RequestBodyValidationException(String i18nKey, String[] params, Throwable th){
		super(i18nKey, i18nKey, params, HttpStatus.BAD_REQUEST, th);		
	}
		
//	public String getBodyValue() {
//		return bodyValue;
//	}
//	protected void setBodyValue(String bodyValue) {
//		this.bodyValue = bodyValue;
//	}
}
