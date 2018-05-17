package eu.europeana.set.definitions.model.utils;

import java.util.HashMap;
import java.util.Map;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

/**
 * This class supports conversion methods for user set object.
 * 
 * @author GrafR
 *
 */
public class UserSetUtils {

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
    Map<String, String> resMap = new HashMap<String, String>();
    resMap.put(key, value);
    return resMap;
  }
  
	/**
	 * This method analyses the items and fills in pagination values.
	 * @param userSet The user set object
	 * @return user set object enriched by pagination values
	 */
	public UserSet analysePagination(UserSet userSet) {
		UserSet res = userSet;
		
		res.setIdentifier(buildIdentifierUrl(userSet.getIdentifier()));
		
		if (res != null && res.getItems() != null) {
			int total = res.getItems().size();
			res.setTotal(total);
			if (total > 0) {
				int first = 0;
				res.setFirst("" + first);
				int last = total/WebUserSetFields.MAX_ITEMS_PER_PAGE - 1; // we start counting by 0
				res.setLast("" + last);
			}
		}
		return res;
	}
	
	/**
	 * This method forms an identifier URL
	 * @param id The sequential ID
	 * @return identifier URL
	 */
	public String buildIdentifierUrl(String id) {
		StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(WebUserSetFields.BASE_SET_URL); 
        urlBuilder.append(id); 
        return urlBuilder.toString();
	}
	  
	/**
	 * This method fills in pagination strings to the user set object. Format is '<id url>?page=0&pageSize=10'
	 * @param userSet The user set object
	 * @return user set object enriched by pagination values
	 */
	public UserSet fillPagination(UserSet userSet) {

		UserSet res = userSet;

		res.setIdentifier(buildIdentifierUrl(userSet.getIdentifier()));
		
		if (res != null && res.getItems() != null) {
			int total = res.getItems().size();
			res.setTotal(total);
			if (total > 0) {
				int first = 0;
		        String firstPageStr = fillPage(userSet, first);
				res.setFirst(firstPageStr);
				int last = total/WebUserSetFields.MAX_ITEMS_PER_PAGE - 1; // we start counting by 0
		        String lastPageStr = fillPage(userSet, last);
				res.setLast(lastPageStr);
			}
		}
		return res;
	}

	/**
	 * This method fills pagination field in format '<id url>?page=0&pageSize=10'
	 * for a user set object
	 * @param userSet
	 * @param pageIndex
	 * @return pagination string
	 */
	public String fillPage(UserSet userSet, int pageIndex) {
		StringBuilder firstBuilder = new StringBuilder();
        String pageStr = firstBuilder.append(String.format("%s?%s=%d&%s=%d", 
        		userSet.getIdentifier(), WebUserSetFields.PAGE, pageIndex
        		, WebUserSetFields.PAGE_SIZE,WebUserSetFields.MAX_ITEMS_PER_PAGE
        		)).toString();
		return pageStr;		
	}
}
