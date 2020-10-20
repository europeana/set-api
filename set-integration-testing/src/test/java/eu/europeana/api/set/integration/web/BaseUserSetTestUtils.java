package eu.europeana.api.set.integration.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import eu.europeana.api.commons.exception.ApiKeyExtractionException;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.impl.UserSetServiceImpl;

/**
 * This is a base test class for UserSet testing, which contains base supporting
 * functionality, such as JWT token generation.
 * 
 * @author Roman Graf on 23-09-2020.
 */
public abstract class BaseUserSetTestUtils {

    static final String BASE_URL = "/set/";

    public static String getToken() {
	EuropeanaOauthClient oauthClient = new EuropeanaOauthClient();
	return oauthClient.getOauthToken();
    }

    /**
     * This method extracts JSON content from a file
     * 
     * @param resource
     * @return JSON string
     * @throws IOException
     */
    protected String getJsonStringInput(String resource) throws IOException {

	try (InputStream resourceAsStream = getClass().getResourceAsStream(resource)) {
	    List<String> lines = IOUtils.readLines(resourceAsStream, StandardCharsets.UTF_8);
	    StringBuilder out = new StringBuilder();
	    for (String line : lines) {
		out.append(line);
	    }
	    return out.toString();
	}
    }

    protected WebUserSetImpl createTestUserSet(String testFile, String token)
	    throws IOException, Exception, UnsupportedEncodingException, HttpException {
	String requestJson = getJsonStringInput(testFile);
	UserSet set = getUserSetService().parseUserSetLd(requestJson);
	Authentication authentication = getAuthentication(token);
	return (WebUserSetImpl) getUserSetService().storeUserSet(set, authentication);
    }

    private Authentication getAuthentication(String token) throws ApiKeyExtractionException, AuthorizationExtractionException {
	RsaVerifier signatureVerifier = new RsaVerifier(getConfiguration().getJwtTokenSignatureKey());
	String authorizationApiName = getConfiguration().getAuthorizationApiName();
	List<? extends Authentication> oauthList = OAuthUtils.extractAuthenticationList(token, signatureVerifier,
		authorizationApiName);
	for (Authentication authentication : oauthList) {
	    if (authorizationApiName.equals(authentication.getDetails())) {
		return authentication;
	    }
	}
	
	return null;
    }

    protected abstract UserSetConfiguration getConfiguration();

    protected abstract UserSetServiceImpl getUserSetService();
}
