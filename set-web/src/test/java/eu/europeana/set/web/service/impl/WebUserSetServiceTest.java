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
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.stanbol.commons.exception.JsonParseException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.web.service.UserSetService;

/**
 * Unit test for the Web UserSet service
 * @deprecated adapt to use UserSetLdParser and UserSetLdDeserializerDeprecated
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/userset-web-context.xml", "/userset-mongo-test.xml"
	})
public class WebUserSetServiceTest extends UserSetTestObjectBuilder{

	public static String TEST_RO_VALUE = "Vlad Tepes";
	public static String TEST_EN_VALUE = "Vlad the Impaler";
	
	@Resource 
	UserSetService webUserSetService;
	
	@Rule public ExpectedException thrown= ExpectedException.none();
	
	@Test
	public void testStoreUserSetInDbRetrieveAndSerialize() 
			throws MalformedURLException, IOException, UserSetServiceException {//, JsonParseException {
		
		UserSet testUserSet = createTestUserSetInstance();		

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
		UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
		
//		if (StringUtils.isBlank(webUserSet.getType())) {
//			webUserSet.setType(UserSetTypes.OBJECT_TAG.name());
//		}
		
		System.out.println("testUserSet: " + testUserSet.toString());
		System.out.println("webUserSet: " + webUserSet.toString());
		
//		assertTrue(webUserSet.getUserSetId() != null && webUserSet.getUserSetId().toString().length() > 0);
//		assertEquals(testUserSet.getBody(), webUserSet.getBody());
//		assertEquals(testUserSet.getTarget(), webUserSet.getTarget());
		
		/**
		 * Serialize UserSet object that was retrieved from a database.
		 */
//		(JsonLd) webUserSet
//		UserSetLdSerializer serializer = new UserSetLdSerializer(webUserSet);
//        UserSetLdParser parser = new UserSetLdParser();
        
//        String actual = serializer.toString();
//        System.out.println(actual);
////        UserSetLd.toConsole("", actual);
//        
//        String actualIndent = serializer.toString(4);
//        UserSetLd.toConsole("", actualIndent);
        
        /**
         * read UserSet object from the serialized UserSetLd object.
         */
//        UserSet UserSetFromUserSetLd = parser.parseUserSet(null, actualIndent);
        
        /**
         * Compare original UserSet object with retrieved serialized UserSet object.
         */     
        // Original object does not have EuropeanaUri
//        UserSetFromUserSetLd.setUserSetId(testUserSet.getUserSetId());
//        //TODO: update test criteria
//        assertEquals(UserSetFromUserSetLd.getTarget(), testUserSet.getTarget());
//        assertEquals(UserSetFromUserSetLd.getBody(), testUserSet.getBody());
	}
		
}
