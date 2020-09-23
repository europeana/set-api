package eu.europeana.api.set.integration.web;

import eu.europeana.api.set.integration.connection.BaseUserSetApi;

/**
 * This is a base test class for UserSet testing, which contains
 * base supporting functionality, such as JWT token generation.
 * 
 * @author Roman Graf on 23-09-2020.
 */
public class BaseUserSetTest {

    private static BaseUserSetApi userSetApi;
    
    public static BaseUserSetApi getUserSetApi() {
        return userSetApi;
    }
    
    public static void initUserSetApi() {
        userSetApi = new BaseUserSetApi();
    }

    public static String getToken() {
	return getUserSetApi().getApiConnection().getRegularUserAuthorizationValue();
    }
    
}
