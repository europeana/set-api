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

        UserSet storedUserSet = storeUserSet(true);
        assertTrue(storedUserSet instanceof UserSet);
    }

    @Test
    public void getUserSetById() {

        UserSet storedUserSet = storeUserSet(true);
        UserSet foundUserSet = userSetService
                .findByID(((PersistentUserSetImpl) storedUserSet).getObjectId().toString());
        checkUserSet(foundUserSet, storedUserSet);

        assertTrue(storedUserSet instanceof UserSet);
        assertEquals(((WebUserSetImpl) storedUserSet).getObjectId().toString(),
                ((WebUserSetImpl) foundUserSet).getObjectId().toString());
    }

    @Test
    public void getUserSetByIdentifier() {

        UserSet storedUserSet = storeUserSet(true);
        UserSet foundUserSet = userSetService.getByIdentifier(storedUserSet.getIdentifier());
        checkUserSet(foundUserSet, storedUserSet);

        assertTrue(storedUserSet instanceof UserSet);
        assertEquals(storedUserSet.getIdentifier(),
                foundUserSet.getIdentifier());
    }

    @Test
    public void getBookmarksFolder() {
        UserSet storedUserSet = storeUserSet(false);
        PersistentUserSet foundUserSet = userSetService.getBookmarkFolder(storedUserSet.getCreator().getHttpUrl());

        assertNotNull(foundUserSet);
        checkUserSet(foundUserSet, storedUserSet);
        assertTrue(storedUserSet instanceof UserSet);
    }

    @Test
    public void getByCreator() {
        String creatorId = UserSetUtils.buildCreatorUri(UserSetTestObjectBuilder.TEST_AGENT);
        QueryResults<PersistentUserSet> foundUserSet = userSetService.getByCreator(creatorId);

        assertNotNull(foundUserSet);
        assertTrue(foundUserSet.asList().size() > 0);
        assertEquals(creatorId, foundUserSet.get().getCreator().getHttpUrl());
    }

    @Test
    public void findPrivateSets() {
        String creatorId = UserSetUtils.buildCreatorUri(UserSetTestObjectBuilder.TEST_AGENT);
        UserSetQuery query = new UserSetQueryImpl();
        query.setUser(creatorId);
        query.setType(UserSetTypes.BOOKMARKSFOLDER.getJsonValue());
        query.setVisibility(VisibilityTypes.PRIVATE.getJsonValue());

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
    }

    @Test
    public void findPublicSets() {
        String creatorId = UserSetUtils.buildCreatorUri(UserSetTestObjectBuilder.TEST_AGENT);
        UserSetQuery query = new UserSetQueryImpl();
        query.setUser(creatorId);
        query.setType(UserSetTypes.COLLECTION.getJsonValue());

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
    }

    @Test
    public void findWithAdmin() {
        UserSetQuery query = new UserSetQueryImpl();
        query.setAdmin(true);

        List<PersistentUserSet> userSetList = userSetService.find(query).getResults();
        assertTrue(userSetList.size() > 0);
    }

    @Test
    public void remove() {
        UserSet storedUserSet = storeUserSet(true);
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

    private UserSet storeUserSet(boolean isCollection) {
        UserSet userSet = new WebUserSetImpl();
        UserSet persistentUserSet = getObjectBuilder().buildUserSet(userSet, isCollection);
        UserSet storedUserSet = userSetService.store(persistentUserSet);
        checkUserSet(persistentUserSet, storedUserSet);
        return storedUserSet;
    }

}
