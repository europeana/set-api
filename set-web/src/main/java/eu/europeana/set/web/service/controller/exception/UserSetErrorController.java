package eu.europeana.set.web.service.controller.exception;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import eu.europeana.api.commons.web.http.HttpHeaders;
import io.swagger.v3.oas.annotations.Hidden;

/**
 * Created by luthien on 2019-08-13.
 */
@RestController
@Hidden
public class UserSetErrorController extends AbstractErrorController {

    public UserSetErrorController(ErrorAttributes errorAttributes) {
        super(errorAttributes);
    }


    /**
     * handle /error requests
     * @param request the request object
     * @return ,app with default error attributes
     */
    @GetMapping(value = "/error", produces = {HttpHeaders.CONTENT_TYPE_JSON_UTF8, HttpHeaders.CONTENT_TYPE_JSONLD})
    public Map<String, Object> error(final HttpServletRequest request) {
        return this.getErrorAttributes(request, ErrorAttributeOptions.defaults());
    }
    
}
