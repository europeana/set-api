package eu.europeana.set.web.search;

import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class UserSetQueryBuilderTest {

    private static final String TEST_VALUE = "200";
    private static final String CREATOR_VALUE = "testAgent";

    private UserSetQueryBuilder userSetQueryBuilder;

    @BeforeEach
    void setup() {
        userSetQueryBuilder = new UserSetQueryBuilder();
    }

    @Test
    public void testBuildUserSetQueryForSetId() throws ParamValidationException {
        String query = buildQuery(WebUserSetFields.SET_ID, TEST_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100);

        assertTrue(StringUtils.equals(TEST_VALUE, userSetQuery.getSetId()));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForCreator() throws ParamValidationException {
        String query = buildQuery(WebUserSetFields.CREATOR, CREATOR_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100);

        assertTrue(StringUtils.equals(UserSetUtils.buildCreatorUri(CREATOR_VALUE), userSetQuery.getCreator()));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForVisibility() throws ParamValidationException {
        String query = buildQuery(WebUserSetFields.VISIBILITY, VisibilityTypes.PUBLIC.getJsonValue());
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100);

        assertTrue(StringUtils.equals(VisibilityTypes.PUBLIC.getJsonValue(), userSetQuery.getVisibility()));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForType() throws ParamValidationException {
        String query = buildQuery(WebUserSetFields.TYPE, UserSetTypes.COLLECTION.getJsonValue());
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100);

        assertTrue(StringUtils.equals(UserSetTypes.COLLECTION.getJsonValue(), userSetQuery.getType()));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForItem() throws ParamValidationException {
        String query = buildQuery(WebUserSetFields.ITEM, TEST_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100);

        assertTrue(StringUtils.equals(UserSetUtils.buildItemUrl(TEST_VALUE), userSetQuery.getItem()));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testSupportedFields() {
        String query = buildQuery(WebUserSetFields.NICKNAME, TEST_VALUE);
        ParamValidationException thrown = assertThrows(
                ParamValidationException.class,
                () -> userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100),
                "Something went wrong, check supportedFields "
        );
        assertTrue(StringUtils.equals(thrown.getMessage(), I18nConstants.INVALID_PARAM_VALUE));
    }

    private static String buildQuery(String fieldName, String fieldValue) {
        return fieldName + ":" + fieldValue;
    }

}
