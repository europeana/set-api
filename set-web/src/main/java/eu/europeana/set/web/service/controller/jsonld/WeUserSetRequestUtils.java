package eu.europeana.set.web.service.controller.jsonld;

import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.config.UserSetConfigurationImpl;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

public class WeUserSetRequestUtils {

  public static boolean isPinnRequest(String position) {
    return WebUserSetModelFields.PINNED_POSITION.equals(position);
  }
  
  public static Integer parseIntegerParam(String paramName, String paramValue, int maxValue, int minValue)
      throws ParamValidationException {
    if (paramValue != null) {
      try {
        Integer value = Integer.valueOf(paramValue);
        if ((maxValue > 0 && value > maxValue) || value < minValue) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE, new String[] {paramName, paramValue});
        }
        return value;
      } catch (NumberFormatException e) {
        throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
            I18nConstants.INVALID_PARAM_VALUE, new String[] {paramName, paramValue}, e);
      }
    }
    return null;
  }
  
  public static Integer parsePageNumber(String page, int maxPageNumber) throws ParamValidationException {
    Integer pageNr;
    //
    pageNr = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE, page, maxPageNumber,
        WebUserSetFields.DEFAULT_PAGE);
    pageNr = (pageNr == null) ? Integer.valueOf(WebUserSetFields.DEFAULT_PAGE) : pageNr;
    return pageNr;
  }
  
  public static Integer getPageSizeOrDefault(String pageSize, int maxPageSize,
      final int defaultItemsPerPage) throws ParamValidationException {
    Integer pageItems;

    pageItems = parseIntegerParam(CommonApiConstants.QUERY_PARAM_PAGE_SIZE, pageSize, maxPageSize,
        UserSetConfigurationImpl.MIN_ITEMS_PER_PAGE);
    pageItems =
        (pageItems == null) ? Integer.valueOf(defaultItemsPerPage)
            : pageItems;
    return pageItems;
  }
}
