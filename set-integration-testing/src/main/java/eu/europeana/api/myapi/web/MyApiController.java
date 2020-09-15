package eu.europeana.api.myapi.web;

import org.springframework.http.MediaType;
import javax.validation.constraints.Pattern;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Example Rest Controller class with input validation
 */
@RestController
@Validated
public class MyApiController {

    private static final String MY_REGEX = "^[a-zA-Z0-9_]*$";
    private static final String INVALID_REQUEST_MESSAGE = "Invalid parameter.";

    @GetMapping(value = "/myApi/{someRequest}", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object handleMyApiRequest(
        @PathVariable(value = "someRequest")
            @Pattern(regexp = MY_REGEX, message = INVALID_REQUEST_MESSAGE) String someRequest) {
        return "It works!";
    }
}
