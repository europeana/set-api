package eu.europeana.set.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.security.core.Authentication;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaApiCredentials;
import eu.europeana.api.commons.oauth2.model.impl.EuropeanaAuthenticationToken;
import eu.europeana.api.commons.web.exception.HttpException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.model.WebUserSetImpl;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;

/**
 * Unit test for the Web UserSet service
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "classpath:set-web-test.xml"})
public class WebUserSetServiceTest {

    private String TEST_DATASET_ID = "000000";
    private String TEST_LOCAL_ID = "2";
    private String TEST_LOCAL_ID_5 = "5";
    private String TEST_LOCAL_ID_7 = "7";
    private String TEST_POSITION = "0";
    private String TEST_POSITION_BIG = "50";
    private String INSERT_IN_EMPTY_ITEM_LIST_RES = "http://data.europeana.eu/item/000000/2";
    private String INSERT_IN_POSITION_0_RES = "http://data.europeana.eu/item/000000/5";
    private String INSERT_IN_POSITION_LAST_RES = "http://data.europeana.eu/item/000000/7";

    String baseUserSetUrl = null;

    @Resource(name = "configuration")
    UserSetConfiguration configuration;

    @Resource(name = "setService")
    UserSetService webUserSetService;

    public void setBaseUserSetUrl(String baseUserSetUrl) {
	this.baseUserSetUrl = baseUserSetUrl;
    }

    public String getBaseUserSetUrl() {
	return baseUserSetUrl;
    }

    UserSetTestObjectBuilder objectBuilder = new UserSetTestObjectBuilder();

    public UserSetTestObjectBuilder getObjectBuilder() {
	return objectBuilder;
    }

    /**
     * Initialize the testing session
     * 
     * @throws IOException
     */
    @BeforeEach
    public void setup() throws IOException {
	setBaseUserSetUrl(configuration.getUserSetBaseUrl());
    }

	@Test
    public void storeUserSetInDbRetrieveAndSerialize() throws IOException, HttpException {

	UserSet userSet = new WebUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE, true);
	assertNotNull(testUserSet);

	// store user set in database
	UserSet storedUserSet = webUserSetService.storeUserSet(testUserSet, getAuthentication());
	assertNotNull(storedUserSet.getIdentifier());

	//get userset
	UserSet webUserSet = webUserSetService.getUserSetById(storedUserSet.getIdentifier());
	assertEquals(storedUserSet.getType(), webUserSet.getType());
	assertEquals(storedUserSet.getVisibility(), webUserSet.getVisibility());
	assertEquals(storedUserSet.getItems().size(), webUserSet.getItems().size());

	//Serialize UserSet object that was retrieved from a database.
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	String userSetJsonLdStr = serializer.serialize(webUserSet);
	assertNotNull(userSetJsonLdStr);
    }

	@Test
    public void deleteUserSet() throws HttpException, IOException {

	UserSet userSet = new WebUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE, true);
	assertNotNull(testUserSet);

	// store user set in database
	UserSet storedUserSet = webUserSetService.storeUserSet(testUserSet, getAuthentication());
	String userId = storedUserSet.getIdentifier();
	assertNotNull(userId);

	// delete stored user set from database
	webUserSetService.deleteUserSet(userId);

	// verify that user set with given id is deleted from database
		UserSetNotFoundException thrown = assertThrows(
				UserSetNotFoundException.class,
				() -> webUserSetService.getUserSetById(userId),
				"Must Throw UserSetNotFoundException"
		);
		assertTrue(thrown.getMessage().contains(UserSetI18nConstants.USERSET_NOT_FOUND));
    }

	@Test
    public void getUserSet() throws HttpException, IOException {

	UserSet userSet = new WebUserSetImpl();
	UserSet userSet1200 = new WebUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE, true);
	UserSet test1200UserSet = getObjectBuilder().buildUserSet(userSet1200,
		UserSetTestObjectBuilder.ITEMS_1200_TEST_INPUT_FILE, true);

	// store user set in database
	UserSet storedUserSet = webUserSetService.storeUserSet(testUserSet, getAuthentication());
	UserSet stored1200UserSet = webUserSetService.storeUserSet(test1200UserSet, getAuthentication());

	// verify output of user sets
	UserSet dbUserSet = webUserSetService.getUserSetById(storedUserSet.getIdentifier());
	UserSet db1200UserSet = webUserSetService.getUserSetById(stored1200UserSet.getIdentifier());
	assertNotNull(dbUserSet);
	assertNotNull(db1200UserSet);
	assertEquals(100, dbUserSet.getTotal());
	assertEquals(1200, db1200UserSet.getTotal());
    }


    @Test
    public void insertItemUserSet() throws HttpException, IOException {

	UserSet userSet = new WebUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE, true);
	testUserSet.setItems(null);

	// store user set in database
	UserSet webUserSet = webUserSetService.storeUserSet(testUserSet, getAuthentication());

	// insert empty items
	UserSet dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID, TEST_POSITION, webUserSet);
	assertEquals(1, dbUserSet.getItems().size());
	assertTrue(dbUserSet.getItems().get(0).equals(INSERT_IN_EMPTY_ITEM_LIST_RES));

	// insert item at position 0 if item list is not empty
	dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID_5, TEST_POSITION, webUserSet);
	assertEquals(2, dbUserSet.getItems().size());
	assertTrue(dbUserSet.getItems().get(0).equals(INSERT_IN_POSITION_0_RES));

	// insert item at position greater than items size
	dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID_7, TEST_POSITION_BIG, webUserSet);
	assertEquals(3,dbUserSet.getItems().size());
	assertTrue(dbUserSet.getItems().get(2).equals(INSERT_IN_POSITION_LAST_RES));
    }

    // provides Authentication for the unit test
	private Authentication getAuthentication() {
		return new EuropeanaAuthenticationToken(null, "usersets", "junit-test-user-id", new EuropeanaApiCredentials("junit-test-username"));
	}

}
