package eu.europeana.set.definitions.model.utils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

/**
 * This class supports conversion methods for user set object.
 *
 * @author GrafR
 */
public class UserSetUtils {

  public static final String EUROPEANA_ID_FIELD_REGEX = "^[a-zA-Z0-9_]*$";
  public static final Pattern EUROPEANA_ID = Pattern.compile("^/[a-zA-Z0-9_]*/[a-zA-Z0-9_]*$");
  public static final int DEFAULT_PAGE = 1;

  /**
   * This method converts string value to Map<String,String> values for given key - language.
   *
   * @param key The language
   * @param value The input string value
   * @return the Map<String,List<String>>
   */
  public Map<String, String> createMap(String key, String value) {
    if (value == null)
      return null;
    Map<String, String> resMap = new HashMap<>();
    resMap.put(key, value);
    return resMap;
  }

  /**
   * This method forms an identifier URL
   *
   * @param id The sequential ID
   * @param base The base URL
   * @return identifier URL
   */
  public static String buildItemUrl(String itemDataEndpoint, String recordId) {
    if (recordId.startsWith(WebUserSetFields.SLASH)) {
      return itemDataEndpoint + recordId.substring(1);
    } else {
      return itemDataEndpoint + recordId;
    }

  }

  /**
   * This method forms an identifier URL
   *
   * @param itemDataEndpoint The base URL
   * @param dataset The sequential ID
   * @param id The sequential ID
   * @return identifier URL
   */
  public static String buildItemUrl(String itemDataEndpoint, String dataset, String id) {
    StringBuilder builder = new StringBuilder(itemDataEndpoint);
    if (!itemDataEndpoint.endsWith(WebUserSetFields.SLASH)) {
      builder.append(WebUserSetFields.SLASH);
    }
    builder.append(dataset);
    builder.append(WebUserSetFields.SLASH).append(id);
    return builder.toString();
  }

  /*
   * This method updates pagination values.
   * 
   * @param userSet The user set object
   * 
   * @return user set object with updated pagination values
   */
  public UserSet updatePagination(UserSet userSet, UserSetConfiguration config) {
    if (userSet == null) {
      return null;
    }

    // set base URL for set.id
    userSet.setBaseUrl(config.getSetDataEndpoint());
    if (userSet.getItems() != null) {
      int total = userSet.getItems().size();
      userSet.setTotal(total);
      // NOTE: the first and last properties are not used now and might be deprecated, they should
      // not be stored in the database
      if (total > 0) {
        int first = UserSetUtils.DEFAULT_PAGE;
        String firstPageStr =
            fillPage(userSet, config, first, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
        userSet.setFirst(firstPageStr);
        final int totalPages =
            (int) Math.floor((double) total / UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
        int pageIndexOfset = UserSetUtils.DEFAULT_PAGE - 1;
        // the index of last page depends on the start index. i.e. 2 pages [0,1] vs. [1,2]
        int lastPageIndex = totalPages + pageIndexOfset;
        String lastPageStr = fillPage(userSet, config, lastPageIndex,
            UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
        userSet.setLast(lastPageStr);
      }

    } else if (userSet != null && userSet.getTotal() == 0) {
      // no items
      // #EA-2557 remove pagination for legacy usersets
      // will not be needed anymore after databse migration
      // should not be used for minimal profile
      userSet.setFirst(null);
      userSet.setLast(null);
    }

    return userSet;
  }


  /**
   * This method fills pagination field in format '<id url>?page=0&pageSize=10' for a user set
   * object
   *
   * @param userSet
   * @param pageIndex
   * @return pagination string
   */
  public String fillPage(UserSet userSet, UserSetConfiguration config, int pageIndex,
      int pageSize) {
    StringBuilder pageUriBuilder = new StringBuilder();
    return pageUriBuilder.append(String.format("%s%s?%s=%d&%s=%d", config.getSetApiEndpoint(),
        userSet.getIdentifier(), CommonApiConstants.QUERY_PARAM_PAGE, pageIndex,
        CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize)).toString();
  }

  public static String buildUserUri(String userDataEndpoint, String userId) {
    return userDataEndpoint + userId;
  }

  public static String buildUserSetId(String baseUrl, String identifier) {
    StringBuilder urlBuilder = new StringBuilder();
    urlBuilder.append(baseUrl);
    if (!baseUrl.endsWith("/")) {
      urlBuilder.append("/");
    }
    urlBuilder.append(identifier);
    return urlBuilder.toString();
  }

  /**
   * This method is to check if setId is numeric
   *
   * @param setId
   * @return true if numeric
   */
  public static boolean isInteger(String setId) {
    try {
      Integer.parseInt(setId);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }

  /**
   * extract item local id from data.europeana URI
   * 
   * @param dataEuropeanaUri
   * @return
   */
  public static String extractItemIdentifier(String dataEuropeanaUri, String itemDataEndpoint) {
    // preserve the first / in the item id
    if(itemDataEndpoint == null) {
     return extractItemIdentifier(dataEuropeanaUri); 
    } else if (itemDataEndpoint.endsWith("/")) {
      return StringUtils.substring(dataEuropeanaUri, itemDataEndpoint.length() - 1);
    } else {
      return StringUtils.substring(dataEuropeanaUri, itemDataEndpoint.length());
    }
  }
  
  /**
   * extract item local id from data.europeana URI
   * 
   * @param dataEuropeanaUri
   * @return
   */
  public static String extractItemIdentifier(String dataEuropeanaItemUri) {
    String[] parts = StringUtils.split(dataEuropeanaItemUri, '/');
    assert parts.length > 2;
    final int collectionIndex = parts.length-2;
    final int itemIndex = parts.length-1;
    return '/'+ parts[collectionIndex] + '/' + parts[itemIndex];  
  }

}
