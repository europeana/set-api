package eu.europeana.api.set.integration.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jettison.json.JSONArray;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.set.integration.BaseUserSetTestUtils;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.LdProfiles;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.internal.PersistentUserSet;
import eu.europeana.set.web.model.WebUserSetImpl;

/**
 * Test class for UserSet controller.
 * <p>
 * For all the methods createUserSet , getUserSet , updateUserSet, deleteUserSet,
 * deleteItemFromUserSet, insertItemIntoUserSet, isItemInUserSet
 * <p>
 * MockMvc test for the Main entry point for server-side Spring MVC. Should check for 200 Ok, 400
 * bad request (if required paremter are not passed), 401 unauthorized (if authentication provided
 * is wrong), and 404 Not found scenarios. Should also check all the headers added using the
 * UserSetHttpHeaders constants
 *
 * @author Roman Graf on 10-09-2020.
 */
@SpringBootTest
public class EntityBestItemsSetIT extends BaseUserSetTestUtils {


  @BeforeAll
  public static void initTokens() {
    if(DISABLE_AUTH) {
      return;
    }
    initRegularUserToken();
    initEntitySetTokens();
  }

  @AfterEach
  protected void deleteCreatedSets() {
    super.deleteCreatedSets();
  }

  // create Entity user set validation tests
  @Test
  void createSetAsRegularUser() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);

    mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }

  @Test
  void createSetWithEmptyToken() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

    mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, "")
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.UNAUTHORIZED.value()));
  }

  // create entity user set with editor token
  @Test
  void createSetSuccessfully() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

    String result = mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.CREATED.value())).andReturn().getResponse()
        .getContentAsString();

    String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
    // register set to be deleted at the end
    PersistentUserSet createdSet = getMongoPersistance().getByIdentifier(identifier);
    createdUserSets.add(createdSet);

    assertNotNull(identifier);
    String creator = getStringValue(result, WebUserSetModelFields.CREATOR);
    assertNotNull(creator);
    assertTrue(StringUtils.contains(creator, getConfiguration().getEntityUserSetUserId()));
    String provider = getStringValue(result, WebUserSetModelFields.PROVIDER);
    assertNotNull(provider);
    // check name
    assertTrue(containsKeyOrValue(provider, "Europeana XX"));
    // check id
    assertTrue(
        containsKeyOrValue(provider, "https:\\/\\/pro.europeana.eu\\/project\\/europeana-xx"));
    // check subject
    assertEquals("http://data.europeana.eu/concept/114", createdSet.getSubject().get(0));
    assertNotNull(getStringListValues(result, WebUserSetModelFields.CONTRIBUTOR));
  }


  // create entity user set with editor token
  @Test
  void createSetWithProviderId() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_PROVIDER_ID);

    String result = mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.CREATED.value())).andReturn().getResponse()
        .getContentAsString();

    String identifier = getSetIdentifier(getConfiguration().getSetDataEndpoint(), result);
    // register set to be deleted at the end
    PersistentUserSet createdSet = getMongoPersistance().getByIdentifier(identifier);
    createdUserSets.add(createdSet);

    assertNotNull(identifier);
    String creator = getStringValue(result, WebUserSetModelFields.CREATOR);
    assertNotNull(creator);
    assertTrue(StringUtils.contains(creator, getConfiguration().getEntityUserSetUserId()));
    String provider = getStringValue(result, WebUserSetModelFields.PROVIDER);
    assertNotNull(provider);
    // check name - must not be present
    assertFalse(containsKeyOrValue(provider, "Europeana XX"));
    // check id
    assertTrue(
        containsKeyOrValue(provider, "https:\\/\\/pro.europeana.eu\\/project\\/europeana-xx"));

    assertNotNull(getStringListValues(result, WebUserSetModelFields.CONTRIBUTOR));
  }

  @Test
  void createSetDuplicated() throws Exception {
    WebUserSetImpl userSetDuplicated = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    assertNotNull(userSetDuplicated);
    String requestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);

    String result = mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(result.contains("duplicate"));

  }

  @Test
  void createSetInvalidSubject() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_INVALID_SUBJECT);

    mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void create_EntityUserSet_InvalidMultipleSubjects() throws Exception {
    String requestJson = getJsonStringInput(ENTITY_USER_SET_INVALID_MULTIPLE_SUBJECTS);

    mockMvc
        .perform(post(BASE_URL)
            .content(requestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void updateSetAsRegularUser() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
    mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));
  }


  @Test
  void updateSetWithNoSubject() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_NO_SUBJECT);
    mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void updateEntityUserSetWithItems() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_REGULAR);
    mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value()));
  }

  @Test
  void updateEntityUserSetSuccessfully() throws Exception {

    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE);
    String result = mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(containsKeyOrValue(result, "https://updated.reference.uri"));
    assertTrue(containsKeyOrValue(result, userSet.getId()));

    // check if items are not overwritten
    UserSet updaedUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    assertEquals(userSet.getPinned(), updaedUserSet.getPinned());
    assertEquals(userSet.getItems(), updaedUserSet.getItems());
  }

  @Test
  void updateEntityBestItemsSetDuplicate() throws Exception {

    WebUserSetImpl userSet1 = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier1 = userSet1.getIdentifier();

    // first update is to check that self update should be ok
    String updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE_2);
    String result = mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier1)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    WebUserSetImpl userSet2 = createTestUserSet(ENTITY_USER_SET_REGULAR_2, editorUserToken);
    String identifier2 = userSet2.getIdentifier();

    // second update should fail
    updateRequestJson = getJsonStringInput(ENTITY_USER_SET_UPDATE_2);
    result = mockMvc
        .perform(put(BASE_URL + "{identifier}", identifier2)
            .content(updateRequestJson).header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.BAD_REQUEST.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(result.contains("duplicate"));

    // check if a set is not overwritten
    UserSet updaedUserSet = getUserSetService().getUserSetById(identifier2);
    assertEquals(userSet2.getSubject().get(0), updaedUserSet.getSubject().get(0));

  }

  @Test
  void deleteEntityUserSetWithRegularUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    mockMvc
        .perform(delete(BASE_URL + "{identifier}", identifier)
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

  }

  @Test
  void deleteEntityUserSetWithEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    mockMvc
        .perform(delete(BASE_URL + "{identifier}", identifier)
            .queryParam(CommonApiConstants.QUERY_PARAM_PROFILE, LdProfiles.STANDARD.name())
            .header(HttpHeaders.AUTHORIZATION, editorUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

  }


  @Test
  void insertItemsWithRegularUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
            .header(HttpHeaders.AUTHORIZATION, regularUserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.FORBIDDEN.value()));

    // getUserSetService().deleteUserSet(identifier);

  }

  @Test
  void insertItems_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String newItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "01", "123_test");

    mockMvc
        .perform(put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();
    
    UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    assertTrue(existingUserSet.getItems().contains(newItem));

  }

  // test adding multiple pinned items, then adding normal item at different positions.
  @Test
  void insertPinnedItems_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    String newItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "01", "123_pinnedItem");

    String result = mockMvc
        .perform(
            put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_pinnedItem")
                .queryParam(WebUserSetFields.PATH_PARAM_POSITION,
                    WebUserSetModelFields.PINNED_POSITION)
                .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse()
        .getContentAsString();

    assertTrue(containsKeyOrValue(result, userSet.getId()));

    UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    //check for the new item
    assertTrue(existingUserSet.getItems().contains(newItem));
    assertEquals(1, existingUserSet.getPinned());
    assertEquals(3, existingUserSet.getItems().size());

    // add more pinned items
    getUserSetService().insertItem("02", "123_pinnedItem", WebUserSetModelFields.PINNED_POSITION,
        existingUserSet);
    getUserSetService().insertItem("03", "123_pinnedItem", WebUserSetModelFields.PINNED_POSITION,
        existingUserSet);
    getUserSetService().insertItem("04", "123_pinnedItem", WebUserSetModelFields.PINNED_POSITION,
        existingUserSet);

    // check if item is present
    assertTrue(existingUserSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "02", "123_pinnedItem")));
    assertTrue(existingUserSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "03", "123_pinnedItem")));
    assertTrue(existingUserSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "04", "123_pinnedItem")));

    assertEquals(4, existingUserSet.getPinned()); // pinned 4
    assertEquals(6, existingUserSet.getItems().size()); // total 6

    // add existing pinned item
    getUserSetService().insertItem("04", "123_pinnedItem", WebUserSetModelFields.PINNED_POSITION,
        existingUserSet);
    assertEquals(4, existingUserSet.getPinned()); // pinned remains same
    assertEquals(6, existingUserSet.getItems().size()); // total remains same

    // add entity item at 0 position
    getUserSetService().insertItem("05", "123_normalItem", "0", existingUserSet);
    String entityItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "05", "123_normalItem");
    // check the item
    assertTrue(existingUserSet.getItems().contains(entityItem));
    // total increase , pinned - same, position - 4+0 position
    checkItemCountAndPosition(existingUserSet, entityItem, 7, 4, 4);

    // add entity item at 3 position
    getUserSetService().insertItem("06", "123_normalItem", "3", existingUserSet);
    entityItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "06", "123_normalItem");
    // check the item
    assertTrue(existingUserSet.getItems().contains(entityItem));
    // total increase , pinned - same, position - 4+3 entity item
    checkItemCountAndPosition(existingUserSet, entityItem, 8, 4, 7);

    // add item without giving position
    getUserSetService().insertItem("07", "123_normalItem", null, existingUserSet);
    entityItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "07", "123_normalItem");
    // check the item
    assertTrue(existingUserSet.getItems().contains(entityItem));
    // total increase , pinned - same, position at last
    checkItemCountAndPosition(existingUserSet, entityItem, 9, 4,
        existingUserSet.getItems().size() - 1);

    // add existing normal item in the same position
    int currentPosition = existingUserSet.getItems().indexOf(entityItem);
    getUserSetService().insertItem("07", "123_normalItem", String.valueOf(currentPosition),
        existingUserSet);
    entityItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "07", "123_normalItem");
    // check the item
    assertTrue(existingUserSet.getItems().contains(entityItem));
    // total , pinned and position remains same
    checkItemCountAndPosition(existingUserSet, entityItem, 9, 4, currentPosition);

    // add existing normal item without providing any position
    getUserSetService().insertItem("07", "123_normalItem", null, existingUserSet);
    entityItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "07", "123_normalItem");
    // check the item
    assertTrue(existingUserSet.getItems().contains(entityItem));
    // total , pinned and position remains same
    checkItemCountAndPosition(existingUserSet, entityItem, 9, 4,
        existingUserSet.getItems().size() - 1);

    // getUserSetService().deleteUserSet(identifier);
  }

  // test insert multiple items
  @Test
  void insertMultipleItems_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();

    List<String> newItems=new ArrayList<>();
    String item1="http://data.europeana.eu/item/01/123_pinnedItem";
    String item2="/02/123_pinnedItem";
    JSONArray newItemsJson = new JSONArray();
    newItemsJson.put(item1);
    newItemsJson.put(item2);

    String result = mockMvc
        .perform(
            put(BASE_URL + "{identifier}/items", identifier)
                .content(newItemsJson.toString())
                .queryParam(WebUserSetFields.PATH_PARAM_POSITION,
                    WebUserSetModelFields.PINNED_POSITION)
                .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value()))
        .andReturn().getResponse()
        .getContentAsString();

    assertTrue(containsKeyOrValue(result, userSet.getId()));

    UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());
    //check for the new items
    assertEquals(0, existingUserSet.getItems().indexOf(item1));
    assertEquals(1, existingUserSet.getItems().indexOf(item2));
    assertEquals(2, existingUserSet.getPinned());
    assertEquals(4, existingUserSet.getItems().size());

    // insert pinned items, some of which already exist (and can be pinned or not-pinned in the set)
    newItems.clear();
    String item3="/03/123_pinnedItem";
    newItems.add(item2);
    String item4Existing=existingUserSet.getItems().get(2);
    newItems.add(item4Existing);
    newItems.add(item3);
    getUserSetService().insertMultipleItems(newItems, WebUserSetModelFields.PINNED_POSITION, -1, existingUserSet);
    //check the new items
    assertEquals(0, existingUserSet.getItems().indexOf(item2));
    assertEquals(1, existingUserSet.getItems().indexOf(item4Existing));
    assertEquals(2, existingUserSet.getItems().indexOf(item3));
    assertEquals(3, existingUserSet.getItems().indexOf(item1));
    assertEquals(4, existingUserSet.getPinned());
    assertEquals(5, existingUserSet.getItems().size());
    
    //insert un-pinned items, some of which may exist (and can be pinned or not-pinned in the set)
    newItems.clear();
    //adding an existing pinned item in the request of un-pinned items
    newItems.add(item1);
    String item5="/05/123_unPinnedItem";
    newItems.add(item5);
    String item6Existing=existingUserSet.getItems().get(4);
    newItems.add(item6Existing);
    getUserSetService().insertMultipleItems(newItems, "4", 4, existingUserSet);
    //check the new items
    assertEquals(0, existingUserSet.getItems().indexOf(item2));
    assertEquals(1, existingUserSet.getItems().indexOf(item4Existing));
    assertEquals(2, existingUserSet.getItems().indexOf(item3));
    assertEquals(3, existingUserSet.getItems().indexOf(item1));
    assertEquals(4, existingUserSet.getPinned());
    assertEquals(4, existingUserSet.getItems().indexOf(item5));
    assertEquals(5, existingUserSet.getItems().indexOf(item6Existing));
    assertEquals(4, existingUserSet.getPinned());
    assertEquals(6, existingUserSet.getItems().size());
    
    //insert item to the last position
    newItems.clear();
    String item7="/07/123_unPinnedItem";
    newItems.add(item7);
    getUserSetService().insertMultipleItems(newItems, null, -1, existingUserSet);
    //check the new items
    assertEquals(6, existingUserSet.getItems().indexOf(item7));
    assertEquals(4, existingUserSet.getPinned());
    assertEquals(7, existingUserSet.getItems().size());
    //insert item to the position grater than the total size of items
    newItems.clear();
    String item8="/08/123_unPinnedItem";
    newItems.add(item8);
    getUserSetService().insertMultipleItems(newItems, "100", 100, existingUserSet);
    //check the new items
    assertEquals(7, existingUserSet.getItems().indexOf(item8));
    assertEquals(4, existingUserSet.getPinned());
    assertEquals(8, existingUserSet.getItems().size());
        
  }

  // test conversion of pinned -> normal item
  @Test
  void insertAlreadyExistingPinnedItemAsNormalItem_EntityUserSets_withEditorUser()
      throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();
    // add 3 pinned items
    getUserSetService().insertItem("01", "123_pinUnpinItem", WebUserSetModelFields.PINNED_POSITION,
        userSet);
    getUserSetService().insertItem("02", "123_pinUnpinItem", WebUserSetModelFields.PINNED_POSITION,
        userSet);
    getUserSetService().insertItem("03", "123_pinnedItem", WebUserSetModelFields.PINNED_POSITION,
        userSet);
    getUserSetService().insertItem("04", "123_pinUnpinItem", WebUserSetModelFields.PINNED_POSITION,
        userSet);
    // check if pinned item is present
    assertTrue(userSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "01", "123_pinUnpinItem")));
    assertTrue(userSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "02", "123_pinUnpinItem")));
    assertTrue(userSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "03", "123_pinnedItem")));
    assertTrue(userSet.getItems().contains(UserSetUtils
        .buildItemUrl(getConfiguration().getItemDataEndpoint(), "04", "123_pinUnpinItem")));

    // pinned
    assertEquals(4, userSet.getPinned());
    // total
    assertEquals(6, userSet.getItems().size());
    // item to be converted into normal entity item with position < pinned items
    String newItem = UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "01",
        "123_pinUnpinItem");

    mockMvc.perform(
        put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_pinUnpinItem")
            .queryParam(WebUserSetFields.PATH_PARAM_POSITION, "3")
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());

    // check if item is present and pinned value is decreased
    assertTrue(existingUserSet.getItems().contains(newItem));
    // total remains same, pinned should be reduced, position = 2+3 = 5 so at the last
    checkItemCountAndPosition(existingUserSet, newItem, 6, 3,
        existingUserSet.getItems().size() - 1);
    // convert another item without position
    String anotherItem = UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "02",
        "123_pinUnpinItem");

    mockMvc.perform(
        put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "02", "123_pinUnpinItem")
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet existingUserSet1 = getUserSetService().getUserSetById(userSet.getIdentifier());

    // check if item is present and pinned value is decreased
    assertTrue(existingUserSet1.getItems().contains(anotherItem));
    // total remains same, pinned should be reduced, position = null so at last
    checkItemCountAndPosition(existingUserSet1, anotherItem, 6, 2,
        existingUserSet1.getItems().size() - 1);

    // item to be converted into normal entity item with valid position
    String thirditem = UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "04",
        "123_pinUnpinItem");

    mockMvc.perform(
        put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "04", "123_pinUnpinItem")
            .queryParam(WebUserSetFields.PATH_PARAM_POSITION, "5")
            .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
            .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet existingUserSet2 = getUserSetService().getUserSetById(userSet.getIdentifier());

    // check if item is present and pinned value is decreased
    assertTrue(existingUserSet2.getItems().contains(thirditem));
    // total remains same, pinned should be reduced,position = 5
    checkItemCountAndPosition(existingUserSet2, thirditem, 6, 1, 5);
    // getUserSetService().deleteUserSet(identifier);
  }

  // test conversion of normal item -> pinned item
  // position will always be 'pin'
  @Test
  void insertAlreadyExistingItemAsPinnedItem_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    String identifier = userSet.getIdentifier();
    // add 1 entity item
    getUserSetService().insertItem("02", "normal_item", null, userSet);
    // add 1 pinned item
    getUserSetService().insertItem("01", "123_pinned", WebUserSetModelFields.PINNED_POSITION,
        userSet);

    // check if pinned item is present
    assertTrue(userSet.getItems().contains(
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "02", "normal_item")));
    assertTrue(userSet.getItems().contains(
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "01", "123_pinned")));
    assertEquals(1, userSet.getPinned()); // pinned
    assertEquals(4, userSet.getTotal()); // total

    // item to be converted into pinned Item entity
    String newItem =
        UserSetUtils.buildItemUrl(getConfiguration().getItemDataEndpoint(), "02", "normal_item");

    mockMvc
        .perform(
            put(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "02", "normal_item")
                .queryParam(WebUserSetFields.PATH_PARAM_POSITION, WebUserSetFields.PINNED_POSITION)
                .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet existingUserSet = getUserSetService().getUserSetById(userSet.getIdentifier());

    assertTrue(existingUserSet.getItems().contains(newItem));
    assertEquals(2, existingUserSet.getPinned()); // pinned increases
    assertEquals(0, existingUserSet.getItems().indexOf(newItem)); // pinned item always added on top
    assertEquals(4, userSet.getTotal()); // total remains same

    // getUserSetService().deleteUserSet(identifier);

  }

  @Test
  void deletePinnedItems_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    getUserSetService().insertItem("01", "123_test", WebUserSetModelFields.PINNED_POSITION,
        userSet);
    getUserSetService().insertItem("02", "123_test", WebUserSetModelFields.PINNED_POSITION,
        userSet);

    assertEquals(2, userSet.getPinned());
    String identifier = userSet.getIdentifier();

    mockMvc
        .perform(
            delete(BASE_URL + "{identifier}/{datasetId}/{localId}", identifier, "01", "123_test")
                .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet userSet1 = getUserSetService().getUserSetById(userSet.getIdentifier());
    assertEquals(1, userSet1.getPinned());

    // getUserSetService().deleteUserSet(identifier);

  }

  @Test
  void deleteMultipleItems_EntityUserSets_withEditorUser() throws Exception {
    WebUserSetImpl userSet = createTestUserSet(ENTITY_USER_SET_REGULAR, editorUserToken);
    
    List<String> newItems=new ArrayList<>();
    String item1="/01/123_pinnedItem";
    String item2="/02/123_pinnedItem";
    newItems.add(item1);
    newItems.add(item2);    
    getUserSetService().insertMultipleItems(newItems, WebUserSetModelFields.PINNED_POSITION, 0, userSet);

    assertEquals(2, userSet.getPinned());
    String identifier = userSet.getIdentifier();

    JSONArray newItemsJson = new JSONArray();
    newItemsJson.put(item2);
    //this item is from the input json (ENTITY_USER_SET_REGULAR), so please check it exists there
    newItemsJson.put("http://data.europeana.eu/item/08641/1037479000000476591");
    //this is not existing item
    newItemsJson.put("/03/123_non_existing_item");
    
    mockMvc
        .perform(
            delete(BASE_URL + "{identifier}/items", identifier)
                .content(newItemsJson.toString())
                .header(HttpHeaders.AUTHORIZATION, editor2UserToken)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE))
        .andExpect(status().is(HttpStatus.OK.value())).andReturn().getResponse();

    UserSet userSet1 = getUserSetService().getUserSetById(userSet.getIdentifier());
    assertEquals(1, userSet1.getPinned());
    assertEquals(2, userSet1.getItems().size());

    // getUserSetService().deleteUserSet(identifier);

  }

  private void checkItemCountAndPosition(UserSet existingUserSet, String newItem,
      int expectedTotalItems, int expectedPinnedItems, int expectedPositionOfItem) {
    assertEquals(expectedPinnedItems, existingUserSet.getPinned());
    assertEquals(expectedTotalItems, existingUserSet.getItems().size());
    assertEquals(expectedPositionOfItem, existingUserSet.getItems().indexOf(newItem));
  }
}
