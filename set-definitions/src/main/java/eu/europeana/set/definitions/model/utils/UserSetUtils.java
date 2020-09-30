package eu.europeana.set.definitions.model.utils;

import java.util.HashMap;
import java.util.Map;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

/**
 * This class supports conversion methods for user set object.
 *
 * @author GrafR
 */
public class UserSetUtils {

    /**
     * This method converts string value to Map<String,String> values for given key - language.
     *
     * @param key   The language
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
     * This method analyses the items and fills in pagination values.
     *
     * @param userSet The user set object
     * @return user set object enriched by pagination values
     */
    public UserSet analysePagination(UserSet userSet) {
        UserSet res = userSet;
        res.setIdentifier(WebUserSetFields.BASE_SET_URL + userSet.getIdentifier());
        res = updatePagination(res);
        return res;
    }

    /**
     * This method updates pagination values.
     *
     * @param userSet The user set object
     * @return user set object with updated pagination values
     */
    public UserSet updatePagination(UserSet userSet) {
        UserSet res = userSet;

        if (res != null && res.getItems() != null) {
            int total = res.getItems().size();
            res.setTotal(total);
            if (total > 0) {
                int first = 0;
                res.setFirst("" + first);
                int last = total / WebUserSetFields.MAX_ITEMS_PER_PAGE - 1; // we start counting by 0
                res.setLast("" + last);
            }
        }

        return res;
    }

    /**
     * This method forms an identifier URL
     *
     * @param id   The sequential ID
     * @param base The base URL
     * @return identifier URL
     */
    public static String buildItemUrl(String recordId) {
	if(recordId.startsWith(WebUserSetFields.SLASH)) {
	    return WebUserSetFields.BASE_ITEM_URL + recordId.substring(1);    
	} else {
	    return WebUserSetFields.BASE_ITEM_URL + recordId;
	}
        
    }

    /**
     * This method forms an identifier URL
     *
     * @param base The base URL
     * @param dataset   The sequential ID
     * @param id   The sequential ID
     * @return identifier URL
     */
    public static String buildItemUrl(String baseUrl, String dataset, String id) {
	StringBuilder builder = new StringBuilder(baseUrl);
	if(!baseUrl.endsWith(WebUserSetFields.SLASH)) {
	    builder.append(WebUserSetFields.SLASH);
	}
	builder.append(dataset);
	builder.append(WebUserSetFields.SLASH).append(id);
	return builder.toString();
    }
    
    /**
     * This method fills in pagination strings to the user set object. Format is '<id url>?page=0&pageSize=10'
     *
     * @param userSet The user set object
     * @return user set object enriched by pagination values
     */
    public UserSet fillPagination(UserSet userSet) {

        UserSet res = userSet;

        if (res != null && res.getItems() != null) {
            int total = res.getItems().size();
            res.setTotal(total);
            if (total > 0) {
                int first = 0;
                String firstPageStr = fillPage(userSet, first);
                res.setFirst(firstPageStr);
                int last = total / WebUserSetFields.MAX_ITEMS_PER_PAGE - 1; // we start counting by 0
                String lastPageStr = fillPage(userSet, last);
                res.setLast(lastPageStr);
            }
        }
        return res;
    }

    /**
     * This method fills pagination field in format '<id url>?page=0&pageSize=10'
     * for a user set object
     *
     * @param userSet
     * @param pageIndex
     * @return pagination string
     */
    public String fillPage(UserSet userSet, int pageIndex) {
        StringBuilder firstBuilder = new StringBuilder();
        return firstBuilder.append(String.format("%s?%s=%d&%s=%d",
                userSet.getIdentifier(), CommonApiConstants.QUERY_PARAM_PAGE, pageIndex
                , CommonApiConstants.QUERY_PARAM_PAGE_SIZE, WebUserSetFields.MAX_ITEMS_PER_PAGE
        )).toString();
    }
    
    public static String buildCreatorUri(String userId) {
   	return WebUserSetFields.DEFAULT_CREATOR_URL + userId;
    }

    /**
     * This method is to check if setId is numeric
     *
     * @param setId
     * @return true if numeric
     */
    public static boolean isSetIdNumeric(String setId) {
        try {
             Integer.parseInt(setId);
             return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
 
}
