package eu.europeana.set.mongo.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.api.commons.nosql.embedded.EmbeddedMongoServer;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/set-mongo-test.xml"})
public class PersistentUserSetServiceTest extends UserSetTestDataBuilder {
	
	public PersistentUserSetServiceTest() {
		super(null);
	}

	@Resource
	EmbeddedMongoServer mongod; 
	
	@Resource 
	PersistentUserSetService userSetService;

	@Resource 
	UserSetConfiguration configuration;

	@Resource(name = "set_db_setDao")
	NosqlDao<PersistentUserSet, UserSetId> userSetDao;

	UserSetTestObjectBuilder objectBuilder;
	
	public UserSetTestObjectBuilder getObjectBuilder() {
		return objectBuilder;
	}

	public void setObjectBuilder(UserSetTestObjectBuilder objectBuilder) {
		this.objectBuilder = objectBuilder;
	}

	/**
	 * Initialize the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		//mongo server is started as resoource
//		userSetDao.getCollection().drop();
		setBaseUserSetUrl(configuration.getUserSetBaseUrl());
		objectBuilder = new UserSetTestObjectBuilder();
	}

	@Test
	public void testStoreUserSet() {

		UserSet storedUserSet = storeUserSet(); 
		assertTrue(storedUserSet instanceof UserSet);
	}
		 
	@Test
	public void testGetUserSetById() {

		UserSet storedUserSet = storeUserSet(); 
		UserSet foundUserSet = userSetService
				.findByID(((PersistentUserSetImpl) storedUserSet).getObjectId().toString());
		checkUserSet(foundUserSet, storedUserSet);

		assertTrue(storedUserSet instanceof UserSet);
		assertEquals(((PersistentUserSetImpl) storedUserSet).getObjectId().toString(),
				((PersistentUserSetImpl) foundUserSet).getObjectId().toString());
	}

	private UserSet storeUserSet() {
		UserSet userSet = new PersistentUserSetImpl();
		UserSet persistentUserSet = getObjectBuilder().buildUserSet(userSet);
		UserSet storedUserSet = userSetService.store(persistentUserSet);
		checkUserSet(persistentUserSet, storedUserSet);
		return storedUserSet;
	}
		 
	@Test
	public void testGetUserSetByIdentifier() {

		UserSet storedUserSet = storeUserSet(); 
		UserSet foundUserSet = userSetService
				.getByIdentifier(((PersistentUserSetImpl) storedUserSet).getIdentifier());
		checkUserSet(foundUserSet, storedUserSet);

		assertTrue(storedUserSet instanceof UserSet);
		assertEquals(((PersistentUserSetImpl) storedUserSet).getIdentifier(),
				((PersistentUserSetImpl) foundUserSet).getIdentifier());
	}
		 
}
