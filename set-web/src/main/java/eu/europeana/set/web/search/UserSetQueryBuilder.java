package eu.europeana.set.web.search;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.search.util.QueryBuilder;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

public class UserSetQueryBuilder extends QueryBuilder {

    String[] fields = new String[] {WebUserSetModelFields.CREATOR, WebUserSetModelFields.VISIBILITY,
	    WebUserSetFields.TYPE, WebUserSetFields.ITEM, WebUserSetFields.SET_ID};
    Set<String> suportedFields = Set.of(fields);

    private UserSetQuery buildSearchQuery(Map<String, String> searchCriteria, String sort, int page, int pageSize) throws ParamValidationException {
	UserSetQuery searchQuery = new UserSetQueryImpl();
	searchQuery.setQuery(searchCriteria.toString());
	
	if (searchCriteria.containsKey(WebUserSetModelFields.VISIBILITY)) {
	    // only for sorting WebUserSetFields.MODIFIED
	    String visibility = searchCriteria.get(WebUserSetModelFields.VISIBILITY);
	    if (visibility != null && !VisibilityTypes.isValid(visibility)) {
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid value for search field " + WebUserSetModelFields.VISIBILITY,
				visibility });
	    }
	    searchQuery.setVisibility(visibility);
	}

	if (searchCriteria.containsKey(WebUserSetFields.TYPE)) {
	    String type = searchCriteria.get(WebUserSetFields.TYPE);
	    if (type != null && !UserSetTypes.isValid(type)) {
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid value for search field " + WebUserSetFields.TYPE, type });
	    }
	    searchQuery.setType(type);
	}

	if (searchCriteria.containsKey(WebUserSetModelFields.CREATOR)) {
	    String creatorId = searchCriteria.get(WebUserSetModelFields.CREATOR);
	    if(creatorId.startsWith("http")) {
		searchQuery.setCreator(creatorId);
	    }else {
		searchQuery.setCreator(UserSetUtils.buildCreatorUri(creatorId));
	    }
	}
	
	if (searchCriteria.containsKey(WebUserSetFields.ITEM)) {
	    String item = searchCriteria.get(WebUserSetFields.ITEM);
	    if(item.startsWith("http")) {
		searchQuery.setItem(item);
	    }else {
		searchQuery.setItem(UserSetUtils.buildItemUrl(item));
	    }
	}

	if (searchCriteria.containsKey(WebUserSetFields.SET_ID)) {
		String setId = searchCriteria.get(WebUserSetFields.SET_ID);
		if (setId != null && ! UserSetUtils.isSetIdNumeric(setId)) {
			throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
					new String[] { "invalid value for search field " + WebUserSetFields.SET_ID,
							setId });
		}
		searchQuery.setSetId(setId);
	}

	searchQuery.setSortCriteria(toArray(sort));	
	searchQuery.setPageSize(pageSize);
	searchQuery.setPageNr(page);
	
	return searchQuery;
    }

    public UserSetQuery buildUserSetQuery(String query, String qf[], String sort, int page, int pageSize)
	    throws ParamValidationException {

	Map<String, String> criteria = new HashMap<>();
	parseSearchCriteria(criteria, query, qf);
	return buildSearchQuery(criteria, sort, page, pageSize);
    }

    private void parseSearchCriteria(Map<String, String> criteria, String query, String[] qf)
	    throws ParamValidationException {
	boolean searchAllQuery = "*".equals(query) || "*:*".equals(query);
	if (!searchAllQuery) {
	    parseCriterion(criteria, query);
	}

	if (qf != null) {
	    for (String criterion : qf) {
		parseCriterion(criteria, criterion);
	    }
	}
    }

    private void parseCriterion(Map<String, String> criteria, String criterion) throws ParamValidationException {
	String toParse = criterion;
	String separator = WebUserSetFields.SEPARATOR_SEMICOLON;
	String space = " ";
	String field;
	String value;

	while (toParse.contains(separator)) {
	    field = StringUtils.substringBefore(toParse, separator);
	    toParse = StringUtils.substringAfter(toParse, separator);
	    if (!suportedFields.contains(field)) {
		// invalid field name
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid field name in search query", field });
	    }

	    value = StringUtils.substringBefore(toParse, space);
	    toParse = StringUtils.substringAfter(toParse, space);
	    if (value.contains(separator) || StringUtils.isBlank(value)) {
		// invalid seearch value
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid formatting of search query for field " + field, value });
	    }
	    criteria.put(field, value);
	}
    }
}
