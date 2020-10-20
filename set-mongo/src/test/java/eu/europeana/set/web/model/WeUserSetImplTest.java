package eu.europeana.set.web.model;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;

import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import eu.europeana.set.definitions.model.util.UserSetTestObjectBuilder;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;

public class WeUserSetImplTest {

    private static final String IDENTIFIER = "test_identifier";
    UserSetTestObjectBuilder objectBuilder = new UserSetTestObjectBuilder();
    private WebUserSetImpl collectionUserSet;
    private WebUserSetImpl bookmarkUserSet;

    public UserSetTestObjectBuilder getObjectBuilder() {
        return objectBuilder;
    }

    @BeforeEach
    void initialiseUserSet() {
        collectionUserSet = new WebUserSetImpl();
        bookmarkUserSet = new WebUserSetImpl();
        getObjectBuilder().buildUserSet(collectionUserSet, true);
        getObjectBuilder().buildUserSet(bookmarkUserSet, false);
    }

    @Test
    public void testCollectionWebUserSet() {
        checkWebUserSets(collectionUserSet);
        assertTrue(StringUtils.equals(UserSetTypes.COLLECTION.getJsonValue(), collectionUserSet.getType()));
        assertTrue(StringUtils.equals(VisibilityTypes.PUBLIC.getJsonValue(), collectionUserSet.getVisibility()));
        assertTrue(collectionUserSet.isPublic());
    }

    @Test
    public void testBookmarkFolderWebUserSet() {
        checkWebUserSets(bookmarkUserSet);
        assertTrue(StringUtils.equals(UserSetTypes.BOOKMARKSFOLDER.getJsonValue(), bookmarkUserSet.getType()));
        assertTrue(StringUtils.equals(VisibilityTypes.PRIVATE.getJsonValue(), bookmarkUserSet.getVisibility()));
        assertTrue(bookmarkUserSet.isPrivate());
    }

    @Test
    public void testGetId() {
        collectionUserSet.setIdentifier(IDENTIFIER);
        String id = collectionUserSet.getId();
        assertTrue(StringUtils.contains(id, IDENTIFIER));
    }

    /**
     * Verifies the created WebUserSets
     *
     * @param userSet
     */
    private void checkWebUserSets(WebUserSetImpl userSet) {
        String title = userSet.getTitle().get(Locale.ENGLISH.getLanguage());
        String description = userSet.getDescription().get(Locale.ENGLISH.getLanguage());

        assertTrue(StringUtils.equals(UserSetTestObjectBuilder.TEST_TITLE, title));
        assertTrue(StringUtils.equals(UserSetTestObjectBuilder.TEST_DESCRIPTION, description));

        assertNotNull(userSet.getCreator());
        assertTrue(StringUtils.equals(UserSetTestObjectBuilder.TEST_AGENT, userSet.getCreator().getName()));
        assertTrue(StringUtils.equals(UserSetTestObjectBuilder.TEST_HOMEPAGE, userSet.getCreator().getHomepage()));
        assertTrue(StringUtils.contains(userSet.getCreator().getHttpUrl(), UserSetTestObjectBuilder.TEST_AGENT));

        assertNotNull(userSet.getItems());
        assertNotNull(userSet.getCreated());
        assertNotNull(userSet.getModified());

        assertTrue(userSet.getItems().size() > 0);
        assertTrue(userSet.getTotal() == userSet.getItems().size());
    }
}
