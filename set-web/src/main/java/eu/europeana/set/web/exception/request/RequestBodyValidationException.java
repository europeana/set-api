package eu.europeana.set.web.exception.request;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import java.util.List;

public class RequestBodyValidationException extends EuropeanaI18nApiException {

	private static final long serialVersionUID = 3364526076494279093L;

	public RequestBodyValidationException(String i18nKey, List<String> params){
		this(i18nKey, params, null);		
	}
	
	public RequestBodyValidationException(String i18nKey, List<String> params, Throwable th){
		super(null, "400_param_invalid", "An invalid parameter was sent in the request", HttpStatus.BAD_REQUEST, i18nKey, params, th);
	}
}