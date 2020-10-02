package eu.europeana.set.mongo.service.impl;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import eu.europeana.set.definitions.model.UserSet;
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

}
