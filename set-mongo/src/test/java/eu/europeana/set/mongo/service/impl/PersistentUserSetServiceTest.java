package eu.europeana.set.mongo.service.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import javax.annotation.Resource;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({ "/set-mongo-context.xml",
		"/set-mongo-test.xml" })
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

	/**
	 * Initialize the testing session
	 * 
	 * @throws IOException
	 */
	@Before
	public void setup() throws IOException {
		userSetDao.getCollection().drop();
		setBaseUserSetUrl(configuration.getUserSetBaseUrl());
		
	}

	/**
	 * Cleaning the testing session's data
	 * 
	 * @throws IOException
	 */
	@After
	public void tearDown() throws IOException {
		// UserSetDao.getCollection().drop();
	}

	@Test
	public void testStoreUserSet() {

		UserSet persistentUserSet = buildUserSet();

		UserSet storedUserSet = userSetService.store(persistentUserSet); 
		checkUserSet(persistentUserSet, storedUserSet);

		assertTrue(storedUserSet instanceof UserSet);
	}
		 
	@Test
	public void testGetUserSetById() {

		UserSet persistentUserSet = buildUserSet();
		UserSet storedUserSet = userSetService.store(persistentUserSet); 
		UserSet foundUserSet = userSetService
				.findByID(((PersistentUserSetImpl) storedUserSet).getObjectId().toString());
		checkUserSet(foundUserSet, storedUserSet);

		assertTrue(storedUserSet instanceof UserSet);
		assertEquals(((PersistentUserSetImpl) storedUserSet).getObjectId().toString(),
				((PersistentUserSetImpl) foundUserSet).getObjectId().toString());
	}
		 
	@Test
	public void testGetUserSetByIdentifier() {

		UserSet persistentUserSet = buildUserSet();
		UserSet storedUserSet = userSetService.store(persistentUserSet); 
		UserSet foundUserSet = userSetService
				.getByIdentifier(((PersistentUserSetImpl) storedUserSet).getIdentifier());
		checkUserSet(foundUserSet, storedUserSet);

		assertTrue(storedUserSet instanceof UserSet);
		assertEquals(((PersistentUserSetImpl) storedUserSet).getIdentifier(),
				((PersistentUserSetImpl) foundUserSet).getIdentifier());
	}
		 
}
