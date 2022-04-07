package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import eu.europeana.set.web.model.search.FacetValue;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.jwt.crypto.sign.RsaVerifier;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.api.commons.exception.ApiKeyExtractionException;
import eu.europeana.api.commons.exception.AuthorizationExtractionException;
import eu.europeana.api.commons.oauth2.utils.OAuthUtils;
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
    public static final String USER_SET_MULTIPLE_QUERY_OPEN = "/content/userset_open_multiple_query.json";
    public static final String USER_SET_LARGE_QUERY_OPEN = "/content/userset_open_large_query.json";
    public static final String USER_SET_LARGE = "/content/userset_large.json";
    public static final String USER_SET_LARGE2 = "/content/userset_large2.json";
    public static final String USER_SET_TATTOOS = "/content/userset_tattoos.json";
    public static final String USER_SET_REGULAR_PUBLIC = "/content/userset_regular_public.json";
    public static final String USER_SET_REGULAR_PUBLISHED = "/content/userset_regular_published.json";
    public static final String USER_SET_COMPLETE_PUBLIC = "/content/userset_complete.json";
    public static final String USER_SET_BOOKMARK_FOLDER = "/content/userset_bookmark_folder.json";
    public static final String USER_SET_BOOKMARK_FOLDER_1 = "/content/userset_bookmark_folder_1.json";
    public static final String USER_SET_BEST_ITEMS = "/content/userset_entity_best_items.json";
    public static final String UPDATED_USER_SET_CONTENT = "/content/updated_regular.json";
    public static final String ENTITY_USER_SET_REGULAR = "/content/entity_userset.json";
    public static final String ENTITY_USER_SET_REGULAR_2 = "/content/entity_userset_2.json";
    public static final String ENTITY_USER_SET_INVALID_SUBJECT = "/content/entity_userset_invalid_subject.json";
    public static final String ENTITY_USER_SET_INVALID_MULTIPLE_SUBJECTS = "/content/entity_userset_invalid_multiple_subjects.json";
    public static final String ENTITY_USER_SET_UPDATE = "/content/entity_userset_update.json";
    public static final String ENTITY_USER_SET_UPDATE_2 = "/content/entity_userset_update_2.json";
    public static final String ENTITY_USER_SET_NO_SUBJECT = "/content/entity_userset_invalid_subject.json";

    protected MockMvc mockMvc;

    @Autowired
    protected WebApplicationContext wac;

    @Autowired
    private UserSetService userSetService;

    @Autowired
    private UserSetConfiguration configuration;

    protected static String regularUserToken;
    protected static String editorUserToken;
    protected static String editor2UserToken;
    protected static String creatorEntitySetUserToken;
    protected static String publisherUserToken;

    public void initApplication() {
	if (mockMvc == null) {
	    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
	}
    }

    public static void initRegularUserToken() {
      regularUserToken = retrieveOatuhToken(EuropeanaOauthClient.REGULAR_USER);
    }
    
    public static void initPublisherUserToken() {
      publisherUserToken = retrieveOatuhToken(EuropeanaOauthClient.PUBLISHER_USER);
    }       

    public static void initEntitySetTokens() {
	editorUserToken=
		    retrieveOatuhToken(EuropeanaOauthClient.EDITOR_USER);
	editor2UserToken = 
		retrieveOatuhToken(EuropeanaOauthClient.EDITOR2_USER);
	creatorEntitySetUserToken =
		retrieveOatuhToken(EuropeanaOauthClient.CREATOR_ENTITYSETS);
    }
    
    
    public UserSetServiceImpl getUserSetService() {
	return (UserSetServiceImpl) userSetService;
    }

    public UserSetConfiguration getConfiguration() {
	return configuration;
    }

    public static String retrieveOatuhToken(String user) {
	EuropeanaOauthClient oauthClient = new EuropeanaOauthClient();
	return oauthClient.getOauthToken(user);
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

    protected WebUserSetImpl createTestUserSet(String testFile, String token) throws Exception {
	String requestJson = getJsonStringInput(testFile);
	UserSet set = getUserSetService().parseUserSetLd(requestJson);
	Authentication authentication = getAuthentication(token);
	return (WebUserSetImpl) getUserSetService().storeUserSet(set, authentication);
    }

    protected void deleteBookmarkFolder(String token)
	    throws ApiKeyExtractionException, AuthorizationExtractionException, UserSetNotFoundException {
	Authentication authentication = getAuthentication(token);
	String creatorId = UserSetUtils.buildUserUri(getConfiguration().getUserDataEndpoint(), (String) authentication.getPrincipal());
	UserSet bookmarkFolder = getUserSetService().getBookmarkFolder(creatorId);
	if (bookmarkFolder != null) {
	    getUserSetService().deleteUserSet(bookmarkFolder.getIdentifier());
	}

    }

    protected Authentication getAuthentication(String token)
	    throws ApiKeyExtractionException, AuthorizationExtractionException {
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

    protected boolean containsKeyOrValue(String jsonString, String property) {
	return StringUtils.contains(jsonString, "\"" + property + "\"");
    }

    protected int noOfOccurance(String jsonString, String property) {
        return StringUtils.countMatches(jsonString, "\"" + property + "\"");
    }

    protected String getvalueOfkey(String jsonString, String property) throws JSONException  {
        assertNotNull(jsonString);
        JSONObject json = new JSONObject(jsonString);
        String value = json.getString(property);
        assertNotNull(value);
        return value;
    }

    protected String getSetIdentifier(String baseUrl, String result) throws JSONException {
	assertNotNull(result);
	JSONObject json = new JSONObject(result);
	String id = json.getString("id");
	assertNotNull(id);
	String identifier = id.replace(baseUrl, "");
	return identifier;
    }

    protected List<FacetValue> getFacetResultPage(String result) throws JSONException {
    List<FacetValue> facetValueResultPages = new ArrayList<>();
    assertNotNull(result);
    JSONObject json = new JSONObject(result);
    JSONArray facets = json.getJSONArray(WebUserSetFields.FACETS);
    // for now we have only single faceting- hence only one facet will be present
    JSONArray values = ((JSONObject) facets.get(0)).getJSONArray(WebUserSetFields.VALUES);
    for (int i =0; i < values.length(); i++) {
        JSONObject o = (JSONObject) values.get(i);
       facetValueResultPages.add(new FacetValue(o.getString("label"), o.getLong("count")));
    }
    return facetValueResultPages;
    }

}
