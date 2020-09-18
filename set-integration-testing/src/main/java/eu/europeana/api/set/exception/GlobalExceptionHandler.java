package eu.europeana.api.set.exception;

import io.micrometer.core.instrument.util.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolationException;
import java.io.IOException;

/**
 * Global exception handler that catches all errors and logs the interesting ones
 */
@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOG = LogManager.getLogger(GlobalExceptionHandler.class);

    /**
     * Checks if we should log an error and rethrows it
     * @param e caught exception
     * @throws SetException rethrown exception
     */
    @ExceptionHandler(SetException.class)
    public void handleBaseException(SetException e) throws SetException {
        if (e.doLog()) {
            if (e.doLogStacktrace()) {
                LOG.error("Caught exception", e);
            } else {
                LOG.error("Caught exception: {}", e.getMessage());
            }
        }

        // We simply rethrow so Spring & Jackson will automatically return a json error. Note that this requires all exceptions
        // to have a ResponseStatus annotation, otherwise the exception will default to 500 status
        throw e;
    }

    /**
     * Make sure we return 400 instead of 500 response when input validation fails
     * @param e exception that is thrown
     * @param response the response that is sent back
     * @throws IOException when there's an exception sending back the response
     */
    @ExceptionHandler
    public void handleInputValidationError(ConstraintViolationException e, HttpServletResponse response) throws IOException {
        response.sendError(HttpStatus.BAD_REQUEST.value(), StringEscapeUtils.escapeJson(e.getMessage()));
    }
}
