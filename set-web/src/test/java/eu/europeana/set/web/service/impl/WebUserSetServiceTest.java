/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.set.web.service.impl;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.service.UserSetService;

/**
 * Unit test for the Web UserSet service
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/set-web-context.xml", "/set-mongo-context.xml", "/set-mongo-test.xml"
	})
public class WebUserSetServiceTest { 

	String baseUserSetUrl = null;
	
	public void setBaseUserSetUrl(String baseUserSetUrl) {
		this.baseUserSetUrl = baseUserSetUrl;
	}


	public String getBaseUserSetUrl() {
		return baseUserSetUrl;
	}
	
	@Resource 
	UserSetService webUserSetService;
	
	UserSetTestObjectBuilder objectBuilder;
	
	public UserSetTestObjectBuilder getObjectBuilder() {
		return objectBuilder;
	}

	public void setObjectBuilder(UserSetTestObjectBuilder objectBuilder) {
		this.objectBuilder = objectBuilder;
	}
	
	@Rule public ExpectedException thrown= ExpectedException.none();
	
	/**
	 * Initialize the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		objectBuilder = new UserSetTestObjectBuilder();
	}
	
	@Test
	public void testStoreUserSetInDbRetrieveAndSerialize() 
			throws MalformedURLException, IOException, UserSetServiceException, UserSetNotFoundException {//, JsonParseException {
		
		UserSet userSet = new PersistentUserSetImpl();
		UserSet testUserSet = getObjectBuilder().buildUserSet(userSet);
		       		
		/**
		 * Store UserSet in database.
		 */
//		UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
		String userSetId = "36";
		UserSet webUserSet = webUserSetService.getUserSetById(userSetId);
		
		System.out.println("testUserSet: " + testUserSet.toString());
		System.out.println("webUserSet: " + webUserSet.toString());
			
		/**
		 * Serialize UserSet object that was retrieved from a database.
		 */
		UserSetLdSerializer serializer = new UserSetLdSerializer();         
        String userSetJsonLdStr = serializer.serialize(webUserSet); 
        System.out.println(userSetJsonLdStr);
        
        /**
         * read UserSet object from the serialized UserSetLd object.
         */
       // UserSet userSetFromUserSetLd = JsonUtils.toUserSetObject(userSetJsonLdStr, WebUserSetImpl.class);
        
        /**
         * Compare original UserSet object with retrieved serialized UserSet object.
         */     
        //assertEquals(userSetFromUserSetLd.getTitle(), testUserSet.getTitle());
	}
		
}
