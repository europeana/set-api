package eu.europeana.set.web.service.impl;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.net.MalformedURLException;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.api.commons.web.exception.ApplicationAuthenticationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.exception.UserSetServiceException;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.web.exception.response.UserSetNotFoundException;
import eu.europeana.set.web.search.UserSetLdSerializer;
import eu.europeana.set.web.service.UserSetService;

/**
 * Unit test for the Web UserSet service
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = { "classpath:set-web-test.xml", "classpath:set-web-context.xml",
	"classpath:set-web-beans.xml", "set-definitions-context.xml",  "set-mongo-context.xml"})
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

    @Resource(name = "set_db_setDao")
    NosqlDao<PersistentUserSet, UserSetId> userSetDao;

    @Resource(name = "set_db_setService")
    PersistentUserSetService userSetService;

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

//	@Rule public ExpectedException thrown= ExpectedException.none();

    /**
     * Initialize the testing session
     * 
     * @throws IOException
     */
    @BeforeEach
    public void setup() throws IOException {
	setBaseUserSetUrl(configuration.getUserSetBaseUrl());
    }

//	@Test
    public void testStoreUserSetInDbRetrieveAndSerialize()
	    throws MalformedURLException, IOException, UserSetServiceException, UserSetNotFoundException {// ,
													  // JsonParseException
													  // {

	UserSet userSet = new PersistentUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE);

	/**
	 * Store UserSet in database.
	 */
//		UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
	String userSetId = "51";
	UserSet webUserSet = webUserSetService.getUserSetById(userSetId);

	System.out.println("testUserSet: " + testUserSet.toString());
	System.out.println("webUserSet: " + webUserSet.toString());

	/**
	 * Serialize UserSet object that was retrieved from a database.
	 */
	UserSetLdSerializer serializer = new UserSetLdSerializer();
	String userSetJsonLdStr = serializer.serialize(webUserSet);
	System.out.println(userSetJsonLdStr);
    }

//	@Test(expected = UserSetNotFoundException.class)
    public void testDeleteUserSet()
	    throws MalformedURLException, IOException, UserSetServiceException, UserSetNotFoundException {

	UserSet userSet = new PersistentUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE);

	// store user set in database
	UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
	String userSetId = webUserSet.getIdentifier();
	System.out.println("testUserSet id: " + userSetId);

	// delete stored user set from database
	webUserSetService.deleteUserSet(userSetId);

	// verify that user set with given id is deleted from database
	webUserSetService.getUserSetById(userSetId);
    }

//	@Test
    public void testGetUserSet()
	    throws MalformedURLException, IOException, UserSetServiceException, UserSetNotFoundException {

	UserSet userSet = new PersistentUserSetImpl();
	UserSet userSet1200 = new PersistentUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE);
	UserSet test1200UserSet = getObjectBuilder().buildUserSet(userSet1200,
		UserSetTestObjectBuilder.ITEMS_1200_TEST_INPUT_FILE);

	// store user set in database
	UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
	String userSetId = webUserSet.getIdentifier();
	System.out.println("testUserSet id: " + userSetId);
	UserSet web1200UserSet = webUserSetService.storeUserSet(test1200UserSet);
	String userSetId1200 = web1200UserSet.getIdentifier();
	System.out.println("test1200UserSet id: " + userSetId1200);

	// verify output of user sets
	UserSet dbUserSet = webUserSetService.getUserSetById(userSetId);
	UserSet db1200UserSet = webUserSetService.getUserSetById(userSetId1200);
	assertTrue(dbUserSet.getTotal() == 100);
	assertTrue(db1200UserSet.getTotal() == 1200);
    }

    @Test
    public void testInsertItemUserSet() throws MalformedURLException, IOException, UserSetServiceException,
	    UserSetNotFoundException, ApplicationAuthenticationException {

	UserSet userSet = new PersistentUserSetImpl();
	UserSet testUserSet = getObjectBuilder().buildUserSet(userSet, UserSetTestObjectBuilder.ITEMS_TEST_INPUT_FILE);
	testUserSet.setItems(null);

	// store user set in database
	UserSet webUserSet = webUserSetService.storeUserSet(testUserSet);
	String userSetId = webUserSet.getIdentifier();
	System.out.println("testUserSet id: " + userSetId);

	// insert empty items
	UserSet dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID, TEST_POSITION, webUserSet);
	assertTrue(dbUserSet.getItems().size() == 1);
	assertTrue(dbUserSet.getItems().get(0).equals(INSERT_IN_EMPTY_ITEM_LIST_RES));

	// insert item at position 0 if item list is not empty
	dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID_5, TEST_POSITION, webUserSet);
	assertTrue(dbUserSet.getItems().size() == 2);
	assertTrue(dbUserSet.getItems().get(0).equals(INSERT_IN_POSITION_0_RES));

	// insert item at position greater than items size
	dbUserSet = webUserSetService.insertItem(TEST_DATASET_ID, TEST_LOCAL_ID_7, TEST_POSITION_BIG, webUserSet);
	assertTrue(dbUserSet.getItems().size() == 3);
	assertTrue(dbUserSet.getItems().get(2).equals(INSERT_IN_POSITION_LAST_RES));
    }

}
