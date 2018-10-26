package eu.europeana.set.definitions.model.authentication;

import java.util.Map;

import eu.europeana.set.definitions.model.agent.Agent;

public interface Application {

	void setAdminUser(Agent adminUser);

	Agent getAdminUser();

	void setAnonymousUser(Agent anonymousUser);

	Agent getAnonymousUser();

	void setApiKey(String apiKey);

	String getApiKey();

	void setName(String name);

	String getName();
	
	Map<String, Agent> getAuthenticatedUsers();
	
	void addAuthenticatedUser(String token, Agent user);
	

}
