package eu.europeana.set.web.service.controller;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.codec.binary.Base64;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

import eu.europeana.api.common.config.I18nConstants;
import eu.europeana.api.commons.config.i18n.I18nService;
import eu.europeana.api.commons.utils.JsonWebUtils;
import eu.europeana.api.commons.web.controller.ApiResponseBuilder;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.api.commons.web.http.HttpHeaders;
import eu.europeana.api.commons.web.model.ApiResponse;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.web.http.UserSetHttpHeaders;
import eu.europeana.set.web.model.UserSetOperationResponse;
import eu.europeana.set.web.service.UserSetService;


public class BaseRest extends ApiResponseBuilder {

    /**
     * API key cache map contains apiKeys as a key and last response time as a value.
     * We only add keys if API key client responded with "204" - valid apikey.
     */
//    private static Map<String, Long> apyKeyCache = new HashMap<String, Long>();
    

	@Resource
	UserSetConfiguration configuration;

	@Resource
	private UserSetService userSetService;

//	@Resource
//	AuthenticationService authenticationService;
//	
//	@Resource
//	AuthorizationService authorizationService;
	
	@Resource
	I18nService i18nService;

	@Override
	protected I18nService getI18nService() {
		return i18nService;
	}

	@Override
	public ApiResponse buildErrorResponse(String errorMessage, String action, String apiKey) {
		// TODO Auto-generated method stub
		return null;
	}

	Logger logger = Logger.getLogger(getClass());

	public Logger getLogger() {
		return logger;
	}

//	public AuthenticationService getAuthenticationService() {
//		return authenticationService;
//	}
//		
//	public void setAuthenticationService(AuthenticationService authenticationService) {
//		this.authenticationService = authenticationService;
//	}

//	protected UserSetBuilder userSetBuilder = new UserSetBuilder();
//	protected UserSetIdHelper userSetIdHelper;

//	public UserSetIdHelper getUserSetIdHelper() {
//		if (UserSetIdHelper == null)
//			UserSetIdHelper = new UserSetIdHelper();
//		return UserSetIdHelper;
//	}

//	TypeUtils typeUtils = new TypeUtils();
//
//	public BaseRest() {
//		super();
//	}
//
//	protected TypeUtils getTypeUtils() {
//		return typeUtils;
//	}

	protected UserSetConfiguration getConfiguration() {
		return configuration;
	}

	protected UserSetService getUserSetService() {
		return userSetService;
	}

	public void setUserSetService(UserSetService userSetService) {
		this.userSetService = userSetService;
	}

	public void setConfiguration(UserSetConfiguration configuration) {
		this.configuration = configuration;
	}

//	protected UserSetBuilder getUserSetBuilder() {
//		return userSetBuilder;
//	}

	public String toResourceId(String collection, String object) {
		return "/" + collection + "/" + object;
	}

//	public UserSetSearchResults<AbstractUserSet> buildSearchResponse(List<? extends UserSet> UserSets,
//			String apiKey, String action) {
//		UserSetSearchResults<AbstractUserSet> response = new UserSetSearchResults<AbstractUserSet>(apiKey,
//				action);
//		response.items = new ArrayList<AbstractUserSet>(UserSets.size());
//
//		AbstractUserSet webUserSet;
//		for (UserSet UserSet : UserSets) {
//			webUserSet = getUserSetBuilder().copyIntoWebUserSet(UserSet);
//			response.items.add(webUserSet);
//		}
//		response.itemsCount = response.items.size();
//		response.totalResults = UserSets.size();
//		return response;
//	}
//
//	public UserSetSearchResults<AbstractUserSet> buildSearchErrorResponse(String apiKey, String action,
//			Throwable th) {
//
//		UserSetSearchResults<AbstractUserSet> response = new UserSetSearchResults<AbstractUserSet>(apiKey,
//				action);
//		response.success = false;
//		response.error = th.getMessage();
//		// response.requestNumber = 0L;
//
//		return response;
//	}

//	protected UserSetId buildUserSetId(String provider, String identifier) throws ParamValidationException {
//
//		return buildUserSetId(provider, identifier, true);
//	}
//
//	protected UserSetId buildUserSetId(String provider, String identifier, boolean validation) throws ParamValidationException {
//
//		UserSetId annoId = new BaseUserSetId(getConfiguration().getUserSetBaseUrl(), provider, identifier);
//
//		if(validation)
//			UserSetService.validateUserSetId(annoId);
//
//		return annoId;
//	}

	
	/**
	 * This method is employed when identifier is an URL and contains provider.
	 * e.g. identifier 'http://data.europeana.eu/annotaion/base/1'
	 * 
	 * @param identifier
	 * @return UserSetId
	 * @throws ParamValidationException
	 */
//	protected UserSetId buildUserSetId(String identifier) throws ParamValidationException {
//
//		if (identifier.split(WebUserSetFields.SLASH).length < ParamValidationException.MIN_IDENTIFIER_LEN)
//			return null;
//
//		UserSetId annoId = getUserSetIdHelper().parseUserSetId(identifier);
//
//		// UserSetService.validateUserSetId(annoId);
//
//		return annoId;
//	}
		
    /**
     * This method employs API key client library for API key validation
     * @param apiKey The API key e.g. ApiKey1
     * @param method The method e.g. read, write, delete...
     * @return true if API key is valid
     * @throws ApplicationAuthenticationException 
     */
//    public boolean validateApiKeyUsingClient(String apiKey, String method) throws ApplicationAuthenticationException {
//    	
//    	boolean res = false;
//    	
//    	// check in cache if there is a valid value
//    	// if yes - return true
//    	long currentTime = System.currentTimeMillis();
//    	Long cacheTime = apyKeyCache.get(apiKey);
//    	if (cacheTime != null) {
//    	    long diff = currentTime - cacheTime.longValue();
//    	    long configCacheTime = getConfiguration().getApiKeyCachingTime();
//    	    if (diff < configCacheTime) 
//    	    	return true; // we already have recent positive response from the client 
//    	} 
//    	
//        ValidationRequest request = new ValidationRequest(
//        		getConfiguration().getValidationAdminApiKey() // the admin API key
//        		, getConfiguration().getValidationAdminSecretKey() // the admin secret key
//        		, apiKey
//        		, getConfiguration().getValidationApi() // the name of API e.g. search, entity, UserSet...
//        		);
//        
//        if (StringUtils.isNotBlank(method)) request.setMethod(method);
//        res = getAdminService().validateApiKey(request, method);
//        if (res) {
//        	apyKeyCache.put(apiKey, currentTime);
//        } else {
//        	//remove invalid from cache if exists
//        	if (apyKeyCache.containsKey(apiKey)) 
//        		apyKeyCache.remove(apiKey);
//   			
//        	throw new ApplicationAuthenticationException(null, I18nConstants.INVALID_APIKEY, new String[]{apiKey});
//        }
//        return res;
//    }

//	protected void validateApiKey(String wsKey, String method) throws ApplicationAuthenticationException {
//		
//		//TODO: will not be included in the 0.2.8-RELEASE, enable in 0.2.9
////		validateApiKeyUsingClient(wsKey, method);
//		
//		// throws exception if the wskey is not found
//		getAuthenticationService().getByApiKey(wsKey);
//	}

	
	protected ResponseEntity <String> buildResponseEntityForJsonString(String jsonStr) {
		
		HttpStatus httpStatus = HttpStatus.OK;
		ResponseEntity<String> response = buildResponseEntityForJsonString(jsonStr, httpStatus);
		
		return response;		
	}
	

	protected ResponseEntity<String> buildResponseEntityForJsonString(String jsonStr, HttpStatus httpStatus) {
		MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>(5);
		headers.add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
		headers.add(HttpHeaders.ETAG, Integer.toString(hashCode()));
		headers.add(HttpHeaders.ALLOW, HttpHeaders.ALLOW_GET);

		ResponseEntity<String> response = new ResponseEntity<String>(jsonStr, headers, httpStatus);
		return response;
	}

//	public AuthorizationService getAuthorizationService() {
//		return authorizationService;
//	}
//
//	public void setAuthorizationService(AuthorizationService authorizationService) {
//		this.authorizationService = authorizationService;
//	}

	/**
	 * This method performs decoding of base64 string
	 * 
	 * @param base64Str
	 * @return decoded string
	 * @throws ApplicationAuthenticationException
	 */
	public String decodeBase64(String base64Str) throws ApplicationAuthenticationException {
		String res = null;
		try {
			byte[] decodedBase64Str = Base64.decodeBase64(base64Str);
			res = new String(decodedBase64Str);
		} catch (Exception e) {
			throw new ApplicationAuthenticationException(
					I18nConstants.BASE64_DECODING_FAIL, I18nConstants.BASE64_DECODING_FAIL, null);			
		}
		return res;
	}

	/**
	 * This method takes user token from a HTTP header if it exists or from the
	 * passed request parameter.
	 * 
	 * @param paramUserToken
	 *            The HTTP request parameter
	 * @param request
	 *            The HTTP request with headers
	 * @return user token
	 * @throws ApplicationAuthenticationException
	 */
	public String getUserToken(String paramUserToken, HttpServletRequest request)
			throws ApplicationAuthenticationException {
		int USER_TOKEN_TYPE_POS = 0;
		int BASE64_ENCODED_STRING_POS = 1;
		String userToken = null;
		String userTokenHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (userTokenHeader != null) {
			logger.trace("'Authorization' header value: " + userTokenHeader);
			String[] headerElems = userTokenHeader.split(" ");
			if(headerElems.length < 2 )
				throw new ApplicationAuthenticationException(
						I18nConstants.INVALID_HEADER_FORMAT, I18nConstants.INVALID_HEADER_FORMAT, null);

			String userTokenType = headerElems[USER_TOKEN_TYPE_POS];
			if (!UserSetHttpHeaders.BEARER.equals(userTokenType))
				throw new ApplicationAuthenticationException(
						I18nConstants.UNSUPPORTED_TOKEN_TYPE, I18nConstants.UNSUPPORTED_TOKEN_TYPE,
						new String[] {userTokenType});

			String encodedUserToken = headerElems[BASE64_ENCODED_STRING_POS];
			
			userToken = decodeBase64(encodedUserToken);
			logger.debug("Decoded user token: " + userToken);

		} else {
			//@deprecated to be removed in the next versions
			//fallback to URL param 
			userToken = paramUserToken;
		}
		return userToken;
	}
	
	
    /**
     * Validate private key (by cipher a constant string with the public key and decipher with the private), 
     * if false respond with HTTP 401.
     * @param publicKey
     * @param privateKey
     * @return true if validation ok
     * @throws ApplicationAuthenticationException
     */
//    public boolean validatePrivateKey(String publicKey, String privateKey) 
//    		throws ApplicationAuthenticationException {
//    	
//    	boolean res = false;
//    	
//   	    String validationString = getConfiguration().getValidationString();
//    	
//        if (StringUtils.isBlank(publicKey)) 
//        	throw new ApplicationAuthenticationException(
//        			null, I18nConstants.INVALID_APIKEY, new String[]{publicKey});
//
//        if (StringUtils.isNotBlank(privateKey)) {
//            //res = getAdminService().validatePrivateKey(publicKey, privateKey, validationString); // TODO
//        	res = true;
//        } else {
//        	throw new ApplicationAuthenticationException(
//        			null, I18nConstants.INVALID_PRIVATE_KEY, new String[]{privateKey});
//        }
//        return res;
//    }

    
    /**
     * This method generates custom UserSet operation response.
     * @param wsKey
     * @param action
     * @param message
     * @return response entity
     */
    public ResponseEntity<String> buildUserSetOperationResponse(String wsKey, String action, String message) {
    	eu.europeana.set.web.service.controller.ApiResponseBuilder arb = 
    			new eu.europeana.set.web.service.controller.ApiResponseBuilder();
		UserSetOperationResponse aor = arb.getValidationReport(
				wsKey
				, action
				, message
				, null
				, false
				);
		String jsonStr = JsonWebUtils.toJson(aor, null);
		HttpStatus httpStatus = aor.success ? HttpStatus.OK : HttpStatus.INTERNAL_SERVER_ERROR;
		return buildResponseEntityForJsonString(jsonStr, httpStatus);			    	
    }
	
}