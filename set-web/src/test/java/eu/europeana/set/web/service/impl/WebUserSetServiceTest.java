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

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Resource;

//import org.apache.commons.lang.StringUtils;
//import org.apache.stanbol.commons.exception.JsonParseException;
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
import eu.europeana.set.utils.JsonUtils;
import eu.europeana.set.utils.serialize.UserSetLdSerializer;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.service.UserSetService;

/**
 * Unit test for the Web UserSet service
 */
@RunWith(SpringJUnit4ClassRunner.class)
//@ContextConfiguration({ "/set-mongo-context.xml", "/set-mongo-test.xml"
@ContextConfiguration({ "/set-web-context.xml", "/set-mongo-context.xml", "/set-mongo-test.xml"
	})
public class WebUserSetServiceTest { //extends UserSetTestDataBuilder {//UserSetTestObjectBuilder{

	String baseUserSetUrl = null;
	
	public void setBaseUserSetUrl(String baseUserSetUrl) {
		this.baseUserSetUrl = baseUserSetUrl;
	}


	public String getBaseUserSetUrl() {
		return baseUserSetUrl;
	}
	
//	public WebUserSetServiceTest(String baseUserSetUrl) {
//		this.baseUserSetUrl = baseUserSetUrl;
////		super(baseUserSetUrl);
//	}

//	public static String TEST_RO_VALUE = "Vlad Tepes";
//	public static String TEST_EN_VALUE = "Vlad the Impaler";
	
	@Resource 
	UserSetService webUserSetService;
	
	UserSetTestObjectBuilder objectBuilder;
	
	public UserSetTestObjectBuilder getObjectBuilder() {
		return objectBuilder;
	}

	public void setObjectBuilder(UserSetTestObjectBuilder objectBuilder) {
		this.objectBuilder = objectBuilder;
	}
	
//	@Resource 
//	UserSetConfiguration configuration;
	
	@Rule public ExpectedException thrown= ExpectedException.none();
	
	/**
	 * Initialize the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
//		setBaseUserSetUrl(configuration.getUserSetBaseUrl());
		objectBuilder = new UserSetTestObjectBuilder();
	}
	
	@Test
	public void testStoreUserSetInDbRetrieveAndSerialize() 
			throws MalformedURLException, IOException, UserSetServiceException, UserSetNotFoundException {//, JsonParseException {
		
		UserSet userSet = new PersistentUserSetImpl();
		UserSet testUserSet = getObjectBuilder().buildUserSet(userSet);
		
		/**
		 * Serialize an original UserSet test object.
		 */
//        UserSetLdSerializer origUserSetLd = new UserSetLdSerializer(testUserSet);
//        
//        String original = origUserSetLd.toString();
//        UserSetLd.toConsole("", original);
//        String expectedOrig = "{\"@context\":{\"oa\":\"http://www.w3.org/ns/oa-context-20130208.json\"},\"@id\":\"http://data.europeana.eu/UserSet/webanno/null\",\"@type\":\"OBJECT_TAG\",\"annotatedAt\":\"2012-11-10T09:08:07\",\"annotatedBy\":{\"@id\":\"open_id_1\",\"@type\":\"foaf:Person\",\"name\":\"annonymous web user\"},\"body\":{\"@type\":\"[SEMANTIC_TAG,oa:Tag,cnt:ContentAsText,dctypes:Text]\",\"chars\":\"Vlad Tepes\",\"foaf:page\":\"https://www.freebase.com/m/035br4\",\"format\":\"text/plain\",\"language\":\"ro\",\"multilingual\":\"\"},\"equivalentTo\":\"http://historypin.com/UserSet/1234\",\"motivation\":\"TAGGING\",\"serializedAt\":\"2012-11-10T09:08:07\",\"serializedBy\":{\"@id\":\"open_id_2\",\"@type\":\"prov:Software\",\"foaf:homepage\":\"http://annotorious.github.io/\",\"name\":\"Annotorious\"},\"styledBy\":{\"@type\":\"oa:CSS\",\"source\":\"http://annotorious.github.io/latest/themes/dark/annotorious-dark.css\",\"styleClass\":\"annotorious-popup\"},\"target\":{\"@type\":\"[oa:IMAGE]\",\"contentType\":\"image/jpeg\",\"httpUri\":\"http://europeanastatic.eu/api/image?uri=http%3A%2F%2Fbilddatenbank.khm.at%2Fimages%2F500%2FGG_8285.jpg&size=FULL_DOC&type=IMAGE\",\"selector\":{\"@type\":\"\",\"dimensionMap\":\"\"},\"source\":{\"@id\":\"http://europeana.eu/portal/record//testCollection/testObject.html\",\"contentType\":\"text/html\",\"format\":\"dctypes:Text\"},\"type\":\"oa:IMAGE\"},\"type\":\"OBJECT_TAG\"}";
        
//        assertEquals(expectedOrig, original);
		
//        String origIndent = origUserSetLd.toString(4);
//        UserSetLd.toConsole("", origIndent);
        		
		/**
		 * Store UserSet in database.
		 */
//		UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
//		String userSetId = "http://localhost:8080/set34";
		String userSetId = "36";
		UserSet webUserSet = webUserSetService.getUserSetById(userSetId);
		
		System.out.println("testUserSet: " + testUserSet.toString());
		System.out.println("webUserSet: " + webUserSet.toString());
		
//		assertTrue(webUserSet.getUserSetId() != null && webUserSet.getUserSetId().toString().length() > 0);
//		assertEquals(testUserSet.getBody(), webUserSet.getBody());
//		assertEquals(testUserSet.getTarget(), webUserSet.getTarget());
		
		/**
		 * Serialize UserSet object that was retrieved from a database.
		 */
//		(JsonLd) webUserSet
		UserSetLdSerializer serializer = new UserSetLdSerializer(); //webUserSet);
//        UserSetLdParser parser = new UserSetLdParser();
//        JsonUtils parser = new JsonUtils();
        
        String userSetJsonLdStr = serializer.serialize(webUserSet); //serializer.toString();
        System.out.println(userSetJsonLdStr);
//        UserSetLd.toConsole("", actual);
        
//        String actualIndent = serializer.toString(4);
//        UserSetLd.toConsole("", actualIndent);
        
        /**
         * read UserSet object from the serialized UserSetLd object.
         */
//        UserSet userSetFromUserSetLd = parser.parseUserSet(WebUserSetImpl.class, userSetJsonLdStr);
        UserSet userSetFromUserSetLd = JsonUtils.toUserSetObject(userSetJsonLdStr, WebUserSetImpl.class);
        
        /**
         * Compare original UserSet object with retrieved serialized UserSet object.
         */     
        assertEquals(userSetFromUserSetLd.getTitle(), testUserSet.getTitle());
	}
		
}
