package eu.europeana.set.mongo.service.impl;

import eu.europeana.api.commons.nosql.dao.NosqlDao;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.UserSetId;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.mongo.model.PersistentUserSetImpl;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.mongo.service.PersistentUserSetService;
import eu.europeana.set.web.model.WebUserSetImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mongodb.morphia.query.QueryResults;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = {"classpath:set-mongo-test.xml"})
public class PersistentUserSetServiceTest extends UserSetTestDataBuilder {

    @Resource(name = "configuration")
    UserSetConfiguration configuration;
    @Resource(name = "set_db_setDao")
    NosqlDao<PersistentUserSet, UserSetId> userSetDao;
    @Resource(name = "set_db_setService")
    PersistentUserSetService userSetService;
    UserSetTestObjectBuilder objectBuilder = new UserSetTestObjectBuilder();

    public PersistentUserSetServiceTest() {
        super(null);
    }

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
        // mongo server is started as resoource
//		userSetDao.getCollection().drop();	
        setBaseUserSetUrl(configuration.getUserSetBaseUrl());
    }

    @Test
    public void storeUserSet() {

        UserSet storedUserSet = createUserSet(true);
        assertTrue(storedUserSet instanceof UserSet);
    }

    @Test
    public void storeBookmarkFoder() {
        UserSet storedUserSet = createUserSet(false);
        assertTrue(storedUserSet instanceof UserSet);
        assertEquals(UserSetTypes.BOOKMARKSFOLDER.getJsonValue(),storedUserSet.getType());
        //delete object
        deleteBookmarkFolder();
    }
    
    @Test
    public void getUserSetById() {

        UserSet storedUserSet = createUserSet(true);
        UserSet foundUserSet = userSetService
                .findByID(((PersistentUserSetImpl) storedUserSet).getObjectId().toString());
        checkUserSet(foundUserSet, storedUserSet);

        assertTrue(storedUserSet instanceof UserSet);
        assertEquals(((WebUserSetImpl) storedUserSet).getObjectId().toString(),
                ((WebUserSetImpl) foundUserSet).getObjectId().toString());
        //delete test object
        userSetService.remove(storedUserSet.getIdentifier());
    }

    @Test
    public void getUserSetByIdentifier() {

        UserSet storedUserSet = createUserSet(true);
        UserSet foundUserSet = userSetService.getByIdentifier(storedUserSet.getIdentifier());
        checkUserSet(foundUserSet, storedUserSet);

        assertTrue(storedUserSet instanceof UserSet);
        assertEquals(storedUserSet.getIdentifier(),
                foundUserSet.getIdentifier());
        
        //delete test object
        userSetService.remove(storedUserSet.getIdentifier());
    }

    @Test
    public void getBookmarksFolder() {
        UserSet storedUserSet = createUserSet(false);
        PersistentUserSet foundUserSet = userSetService.getBookmarkFolder(storedUserSet.getCreator().getHttpUrl());

        assertNotNull(foundUserSet);
        checkUserSet(foundUserSet, storedUserSet);
        assertTrue(storedUserSet instanceof UserSet);
        //delete test object
        userSetService.remove(storedUserSet.getIdentifier());
    }

    @Test
    public void getByCreator() {
	UserSet storedUserSet = createUserSet(true);
        String creatorId = UserSetUtils.buildUserUri(UserSetTestObjectBuilder.TEST_AGENT);
        QueryResults<PersistentUserSet> foundUserSet = userSetService.getByCreator(creatorId);

        assertNotNull(foundUserSet);
        assertTrue(foundUserSet.asList().size() > 0);
        assertEquals(creatorId, foundUserSet.get().getCreator().getHttpUrl());
        
        //delete test object
        userSetService.remove(storedUserSet.getIdentifier());
    }

    @Test
    public void findBookmarkFolder() {
	//ensure BookmarkFolder exists
	UserSet bf = retrieveOrCreateBookmarkFolder();
	String creatorId = UserSetUtils.buildUserUri(UserSetTestObjectBuilder.TEST_AGENT);
        UserSetQuery query = new UserSetQueryImpl();
        query.setUser(creatorId);
        query.setType(UserSetTypes.BOOKMARKSFOLDER.getJsonValue());
        query.setVisibility(VisibilityTypes.PRIVATE.getJsonValue());

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
        //delete created object
        userSetService.remove(bf.getIdentifier());
    }

    @Test
    public void findPublicSets() {
        UserSet set = createUserSet(true);
	String creatorId = UserSetUtils.buildUserUri(UserSetTestObjectBuilder.TEST_AGENT);
        UserSetQuery query = new UserSetQueryImpl();
        query.setUser(creatorId);
        query.setType(UserSetTypes.COLLECTION.getJsonValue());

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
        
        //delete created object
        userSetService.remove(set.getIdentifier());
    }

    @Test
    public void findWithAdmin() {
	UserSet set = createUserSet(true);
	
	UserSetQuery query = new UserSetQueryImpl();
        query.setAdmin(true);

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
        //TODO: verify that the created set is included in the results
        
        //delete created object
        userSetService.remove(set.getIdentifier());
    }

    @Test
    public void remove() {
        UserSet storedUserSet = createUserSet(true);
        userSetService.remove(storedUserSet.getIdentifier());

        UserSet deletedUserSet = userSetService.getByIdentifier(storedUserSet.getIdentifier());
        assertNull(deletedUserSet);
    }

    @Test
    public void removeAll() {
        UserSetQuery query = new UserSetQueryImpl();
        query.setAdmin(true);
        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();

        assertTrue(userSetList.size() > 0);

        userSetService.removeAll(userSetList);
        List<PersistentUserSet> userSetListnow = userSetService.find(query).getResults();

        assertTrue(userSetListnow.size() == 0);
    }

    private UserSet createUserSet(boolean isCollection) {
        UserSet userSet = new WebUserSetImpl();
        UserSet persistentUserSet = getObjectBuilder().buildUserSet(userSet, isCollection);
        UserSet storedUserSet = userSetService.store(persistentUserSet);
        checkUserSet(persistentUserSet, storedUserSet);
        return storedUserSet;
    }

    private UserSet retrieveOrCreateBookmarkFolder() {
	UserSet set = retrieveBookmarkFolder();
	if(set == null) {
	    set = createUserSet(false);
	}
	return set;
    }

    private UserSet retrieveBookmarkFolder() {
	UserSet set = userSetService.getBookmarkFolder(
		UserSetUtils.buildUserUri(UserSetTestObjectBuilder.TEST_AGENT));
	return set;
    }
    
    private void deleteBookmarkFolder() {
	UserSet set = retrieveBookmarkFolder();
	if(set != null) {
	    userSetService.remove(set.getIdentifier());
	}
    }
    
}
