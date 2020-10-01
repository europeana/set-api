package eu.europeana.set.mongo.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;

@ExtendWith(SpringExtension.class)
@ContextConfiguration({ "/set-mongo-test.xml"})
public class PersistentUserSetServiceTest extends UserSetTestDataBuilder {
	
	public PersistentUserSetServiceTest() {
		super(null);
	}

	@Resource 
	PersistentUserSetService userSetService;

	@Resource 
	UserSetConfiguration configuration;

	@Resource(name = "set_db_setDao")
	NosqlDao<PersistentUserSet, UserSetId> userSetDao;

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
		//mongo server is started as resoource
//		userSetDao.getCollection().drop();
		setBaseUserSetUrl(configuration.getUserSetBaseUrl());
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
