package eu.europeana.api.set.integration.web;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;

import eu.europeana.api.commons.exception.ApiKeyExtractionException;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;
import eu.europeana.set.web.service.impl.UserSetServiceImpl;

/**
 * This is a base test class for UserSet testing, which contains base supporting
 * functionality, such as JWT token generation.
 * 
 * @author Roman Graf on 23-09-2020.
 */
public abstract class BaseUserSetTestUtils {

    static final String BASE_URL = "/set/";
    public static final String USER_SET_REGULAR = "/content/userset_regular.json";
    public static final String USER_SET_MANDATORY = "/content/userset_mandatory.json";
    public static final String USER_SET_OPEN = "/content/userset_open.json";
    public static final String USER_SET_LARGE = "/content/userset_large.json";
    public static final String USER_SET_REGULAR_PUBLIC = "/content/userset_regular_public.json";
    public static final String USER_SET_REGULAR_PUBLISHED = "/content/userset_regular_published.json";
    public static final String USER_SET_COMPLETE_PUBLIC = "/content/userset_complete.json";
    public static final String USER_SET_BOOKMARK_FOLDER = "/content/userset_bookmark_folder.json";
    public static final String ENTITY_USER_SET_REGULAR = "/content/entity_userset.json";


    public static final String UPDATED_USER_SET_CONTENT = "/content/updated_regular.json";

    @Autowired
    private UserSetService userSetService; 

    @Autowired
    private UserSetConfiguration configuration; 
    
    protected static String token;

    @BeforeAll
    public static void initToken() {
        token = retrieveOatuhToken();
    }

    public UserSetServiceImpl getUserSetService() {
        return (UserSetServiceImpl) userSetService;
    }
    
    public UserSetConfiguration getConfiguration() {
	return configuration;
    }

    public static String retrieveOatuhToken() {
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

    
    protected void deleteBookmarkFolder(String token) throws ApiKeyExtractionException, AuthorizationExtractionException, UserSetNotFoundException {
	// TODO Auto-generated method stub
	Authentication authentication = getAuthentication(token);
	String creatorId = buildCreatorId(authentication);
	UserSet bookmarkFolder = getUserSetService().getBookmarkFolder(creatorId);
	if(bookmarkFolder != null) {
	    getUserSetService().deleteUserSet(bookmarkFolder.getIdentifier());
	}
	
	
    }

    private String buildCreatorId(Authentication authentication) throws ApiKeyExtractionException, AuthorizationExtractionException {
	String creator = (String) authentication.getPrincipal();
	String creatorId = UserSetUtils.buildCreatorUri(creator);
	return creatorId;
    }
    
    protected Authentication getAuthentication(String token) throws ApiKeyExtractionException, AuthorizationExtractionException {
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
    
    protected boolean constainsKeyOrValue(String jsonString, String property) {
	return StringUtils.contains(jsonString, "\"" + property + "\"");
    }
}
