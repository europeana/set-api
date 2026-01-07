package eu.europeana.set.web.exception.request;

import eu.europeana.api.commons_sb3.error.EuropeanaI18nApiException;
import org.springframework.http.HttpStatus;
import java.util.List;

public class ItemValidationException extends EuropeanaI18nApiException {

  private static final long serialVersionUID = -7048717251116426084L;

  public ItemValidationException(String i18nKey, List<String> params){
      super(null, null, "Items have invalid format", HttpStatus.BAD_REQUEST, i18nKey, params);
  }

    public ItemValidationException(String code, String error, String i18nKey, List<String> params){
        super(null, code, error, HttpStatus.BAD_REQUEST, i18nKey, params);
  }
  
  public ItemValidationException(String i18nKey, List<String> params, Throwable th){
	super(null, null, null, HttpStatus.BAD_REQUEST, i18nKey, params, th);
  }
}