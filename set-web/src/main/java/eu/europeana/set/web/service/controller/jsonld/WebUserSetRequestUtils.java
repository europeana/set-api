package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.commons_sb3.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons_sb3.error.exceptions.InvalidParamException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

import java.util.Arrays;

public class WebUserSetRequestUtils {

  /**
   * Verify if the position is the pin keyword
   * @param position request parameter value
   * @return true if equals "pin"
   */
  public static boolean isPinPosition(String position) {
    return WebUserSetModelFields.POSITION_PIN.equals(position);
  }
  
  /**
   * Parses the request parameter and verifies if the value is in the expected range 
   * @param paramName the name of the request parameter (e.g. page, pageSize)
   * @param paramValue the request paramter value
   * @param maxValue max allowed value
   * @param minValue min allowed value (negative value will be ignored)
   * @return the parsed value or null 
   * @throws InvalidParamException is the value is not in the expected range
   */
  public static Integer parseIntegerParam(String paramName, String paramValue, int maxValue, int minValue)
      throws InvalidParamException {
    if (paramValue != null) {
      try {
        Integer value = Integer.valueOf(paramValue);
        if ((maxValue > 0 && value > maxValue) || value < minValue) {
          throw new InvalidParamException(Arrays.asList(paramName, "value in range", paramValue));
        }
        return value;
      } catch (NumberFormatException e) {
        throw new InvalidParamException(Arrays.asList(paramName, "integer value", paramValue));
      }
    }
    return null;
  }
  
  /**
   * Parse the value of the request parameter page
   * @param page the value of the request param
   * @param maxPageNumber the mx value for the page number, negative value is ignored
   * @return
   * @throws InvalidParamException
   */
  public static Integer parsePageNumber(String page, int maxPageNumber) throws InvalidParamException {
    Integer pageNr;
    //
    pageNr = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE, page, maxPageNumber,
        WebUserSetFields.DEFAULT_PAGE);
    return (pageNr == null) ? Integer.valueOf(WebUserSetFields.DEFAULT_PAGE) : pageNr;
  }
  
  /**
   * Parses the page size, if not provided the default will be returned 
   * @param pageSize request param value
   * @param maxPageSize maximum pageSize value (depends on requested profile)
   * @param defaultItemsPerPage default value to return if the param value is empty 
   * @return the value parsed from the param or the default
   * @throws InvalidParamException if ti is out of range
   */
  public static Integer getPageSizeOrDefault(String pageSize, int maxPageSize,
      final int defaultItemsPerPage) throws InvalidParamException {
    Integer pageItems;

    pageItems = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize, maxPageSize,
        UserSetConfigurationImpl.MIN_ITEMS_PER_PAGE);
    return (pageItems == null) ? Integer.valueOf(defaultItemsPerPage)
            : pageItems;
  }
}