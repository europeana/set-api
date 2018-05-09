package eu.europeana.set.mongo.service.impl;

import static org.junit.Assert.assertNotNull;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;

public class UserSetTestDataBuilder {

	String baseUserSetUrl = null;
	
	public void setBaseUserSetUrl(String baseUserSetUrl) {
		this.baseUserSetUrl = baseUserSetUrl;
	}


	public String getBaseUserSetUrl() {
		return baseUserSetUrl;
	}


	public UserSetTestDataBuilder(String baseUserSetUrl){
		this.baseUserSetUrl = baseUserSetUrl;
	}
	
	
	protected void checkUserSet(UserSet persistantObject, UserSet storedUserSet) {
	
		assertNotNull(((PersistentUserSet) storedUserSet).getIdentifier());
		assertNotNull(((PersistentUserSet) storedUserSet)
				.getCreated());
	
		assertNotNull(storedUserSet.getTitle());
		assertNotNull(storedUserSet.getDescription());
		assertNotNull(storedUserSet.getCreator());
	}

	protected UserSet buildUserSet() {
		//TODO: move to definitions test object builder
		UserSet persistentObject = new PersistentUserSetImpl();
		persistentObject.setTitle("testUserSetTitle");
		persistentObject.setDescription("test descriptin of a user set");
		
		Agent creator = new SoftwareAgent();
		creator.setName("unit test");
		creator.setHomepage("http://www.pro.europeana.eu/web/europeana-creative");
		persistentObject.setCreator(creator);
		persistentObject.setCreator(creator);
		
		return persistentObject;
	}

}
