package eu.europeana.api.set.integration.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import eu.europeana.api.set.integration.connection.http.EuropeanaOauthClient;

/**
 * This is a base test class for UserSet testing, which contains
 * base supporting functionality, such as JWT token generation.
 * 
 * @author Roman Graf on 23-09-2020.
 */
public class BaseUserSetTest {

    public static String getToken() {
	EuropeanaOauthClient oauthClient = new EuropeanaOauthClient(); 
	return oauthClient.getOauthToken();
    }
    
    /**
     * This method extracts JSON content from a file
     * @param resource
     * @return JSON string
     * @throws IOException
     */
    protected String getJsonStringInput(String resource) throws IOException {
	InputStream resourceAsStream = getClass().getResourceAsStream(resource);

	StringBuilder out = new StringBuilder();
	BufferedReader br = new BufferedReader(new InputStreamReader(resourceAsStream));
	for (String line = br.readLine(); line != null; line = br.readLine())
	    out.append(line);
	br.close();
	return out.toString();

    }
    
}
