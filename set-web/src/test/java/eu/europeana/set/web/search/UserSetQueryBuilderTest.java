package eu.europeana.set.web.search;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.IOException;
import java.util.Properties;

import eu.europeana.api.commons_sb3.error.config.ErrorMessage;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

@Disabled
public class UserSetQueryBuilderTest {

    private static final String TEST_VALUE = "200";
    private static final String CREATOR_VALUE = "testAgent";

    private UserSetQueryBuilder userSetQueryBuilder;
    static UserSetConfigurationImpl config;
    
    @BeforeAll
    static void setupConfig() throws IOException {
      config = new UserSetConfigurationImpl();
      Properties props = new Properties();
      props.load(UserSetQueryBuilderTest.class.getResourceAsStream("classpath:/config/set.common.properties"));
      config.setSetProperties(props);
    }
    
    @BeforeEach
    void setup() {
        userSetQueryBuilder = new UserSetQueryBuilder();
    }
  
    @Test
    public void testBuildUserSetQueryForSetId() throws InvalidParamException {
        String query = buildQuery(WebUserSetFields.SET_ID, TEST_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config);

        assertTrue(StringUtils.equals(TEST_VALUE, userSetQuery.getSetId().get(0)));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForCreator() throws InvalidParamException {
        String query = buildQuery(WebUserSetFields.CREATOR, CREATOR_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config);

        assertTrue(StringUtils.equals(UserSetUtils.buildUserUri("http://data.europeana.eu/user/", CREATOR_VALUE), userSetQuery.getCreator().get(0)));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForVisibility() throws InvalidParamException {
        String query = buildQuery(WebUserSetFields.VISIBILITY, VisibilityTypes.PUBLIC.getJsonValue());
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config);

        assertTrue(StringUtils.equals(VisibilityTypes.PUBLIC.getJsonValue(), userSetQuery.getVisibility().get(0)));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForType() throws InvalidParamException {
        String query = buildQuery(WebUserSetFields.TYPE, UserSetTypes.COLLECTION.getJsonValue());
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config);

        assertTrue(StringUtils.equals(UserSetTypes.COLLECTION.getJsonValue(), userSetQuery.getType().get(0)));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testBuildUserSetQueryForItem() throws InvalidParamException {
        String query = buildQuery(WebUserSetFields.ITEM, TEST_VALUE);
        UserSetQuery userSetQuery = userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config);

        assertTrue(StringUtils.equals(UserSetUtils.buildItemUrl("http://data.europeana.eu/item/", TEST_VALUE), userSetQuery.getItem().get(0)));
        assertFalse(userSetQuery.isAdmin());
    }

    @Test
    public void testSupportedFields() {
        String query = buildQuery(WebUserSetFields.NICKNAME, TEST_VALUE);
        InvalidParamException thrown = assertThrows(
                InvalidParamException.class,
                () -> userSetQueryBuilder.buildUserSetQuery(query, null, null, 0, 100, config),
                "Something went wrong, check supportedFields "
        );
        assertTrue(StringUtils.equals(thrown.getMessage(), ErrorMessage.PARAM_INVALID_400.getI18nKey()));
    }

    private static String buildQuery(String fieldName, String fieldValue) {
        return fieldName + ":" + fieldValue;
    }

}