package eu.europeana.set.definitions.model.utils;

import java.util.HashMap;
import java.util.Map;

import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
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

    /* This method updates pagination values.
    * @param userSet The user set object
    * @return user set object with updated pagination values
    */
   public UserSet updatePagination(UserSet userSet) {
       
       if (userSet != null && userSet.getItems() != null) {
           int total = userSet.getItems().size();
           userSet.setTotal(total);
           //NOTE: the first and last properties are not used now and might be deprecated, they should not be stored in the database
           if (total > 0) {
               
               int first = 0;
               String firstPageStr = fillPage(userSet, first, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
               userSet.setFirst(firstPageStr);
               int last = (int) Math.ceil( (double)total / UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE); 
               if(last > 0) {
        	last = last - 1; // we start counting by 0    
               }
               String lastPageStr = fillPage(userSet, last, UserSetConfigurationImpl.DEFAULT_ITEMS_PER_PAGE);
               userSet.setLast(lastPageStr);
           }
       }

       return userSet;
   }
    
    
    /**
     * This method fills pagination field in format '<id url>?page=0&pageSize=10'
     * for a user set object
     *
     * @param userSet
     * @param pageIndex
     * @return pagination string
     */
    public String fillPage(UserSet userSet, int pageIndex, int pageSize) {
        StringBuilder pageUriBuilder = new StringBuilder();
        return pageUriBuilder.append(String.format("%s%s?%s=%d&%s=%d",WebUserSetFields.BASE_SET_URL,
                userSet.getIdentifier(), CommonApiConstants.QUERY_PARAM_PAGE, pageIndex
                , CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize
        )).toString();
    }
    
    public static String buildCreatorUri(String userId) {
   	return WebUserSetFields.DEFAULT_CREATOR_URL + userId;
    }
    
    public static String buildUserSetId(String identifier) {
	StringBuilder urlBuilder = new StringBuilder();
	urlBuilder.append(WebUserSetFields.BASE_SET_URL);
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
 
}
