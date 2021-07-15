package eu.europeana.set.web.exception.request;

import eu.europeana.api.commons.web.exception.HttpException;
import org.springframework.http.HttpStatus;

public class BestBetsMismatchException extends HttpException {

    private static final long serialVersionUID = 3364526076494279093L;

    public BestBetsMismatchException(String i18nKey, String[] params){
        this(i18nKey, params, null);
    }

    public BestBetsMismatchException(String i18nKey, String[] params, Throwable th){
        super(i18nKey, i18nKey, params, HttpStatus.BAD_REQUEST, th);
    }
}
