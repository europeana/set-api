package eu.europeana.set.web.exception.response;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import java.util.List;

public class UserSetNotFoundException extends EuropeanaI18nApiException {

	private static final long serialVersionUID = 3050674865876453650L;

	public UserSetNotFoundException(String i18nKey, List<String> i18nParams){
		this(null, i18nKey, i18nParams, HttpStatus.NOT_FOUND, null);
	}
	public UserSetNotFoundException(String message, String i18nKey, List<String> i18nParams){
		this(message, i18nKey, i18nParams, HttpStatus.NOT_FOUND, null);
	}
	
	public UserSetNotFoundException(String message, String i18nKey, List<String> i18nParams, HttpStatus status){
		this(message, i18nKey, i18nParams, status, null);
	}
	
	public UserSetNotFoundException(String message, String i18nKey, List<String> i18nParams, Throwable th){
		this(message, i18nKey, i18nParams, HttpStatus.NOT_FOUND, th);
	}
	
	public UserSetNotFoundException(String message, String i18nKey, List<String> i18nParams, HttpStatus status, Throwable th){
		super(message, "404_not_found", "User Set not found!", status,  i18nKey, i18nParams, th);
	}
}