package eu.europeana.set.web.search;

import java.util.*;

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
	    WebUserSetFields.TYPE, WebUserSetFields.ITEM, WebUserSetFields.SET_ID, WebUserSetFields.CONTRIBUTOR, WebUserSetFields.SUBJECT, WebUserSetFields.TEXT};
    Set<String> suportedFields = Set.of(fields);
    
    public static final String SEARCH_ALL = "*";
    public static final String SEARCH_ALL_ALL = "*:*";
		

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

	final String PREFIX_HTTP = "http";
	if (searchCriteria.containsKey(WebUserSetModelFields.CREATOR)) {
	    String creatorId = searchCriteria.get(WebUserSetModelFields.CREATOR);
	    if(creatorId.startsWith(PREFIX_HTTP)) {
		searchQuery.setCreator(creatorId);
	    }else {
		searchQuery.setCreator(UserSetUtils.buildUserUri(creatorId));
	    }
	}
	
	if (searchCriteria.containsKey(WebUserSetModelFields.CONTRIBUTOR)) {
	    String contributorId = searchCriteria.get(WebUserSetModelFields.CONTRIBUTOR);
	    if(contributorId.startsWith(PREFIX_HTTP)) {
		searchQuery.setContributor(contributorId);
	    }else {
		searchQuery.setContributor(UserSetUtils.buildUserUri(contributorId));
	    }
	}
	
	if (searchCriteria.containsKey(WebUserSetModelFields.SUBJECT)) {
	    String subject = searchCriteria.get(WebUserSetModelFields.SUBJECT);
	    if(subject.startsWith(PREFIX_HTTP)) {
		searchQuery.setSubject(subject);
	    }else {
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid value for search field, subject must be a URI", subject });
	    }
	}
	
	if (searchCriteria.containsKey(WebUserSetFields.ITEM)) {
	    String item = searchCriteria.get(WebUserSetFields.ITEM);
	    if(item.startsWith(PREFIX_HTTP)) {
		searchQuery.setItem(item);
	    }else {
		searchQuery.setItem(UserSetUtils.buildItemUrl(item));
	    }
	}

	if (searchCriteria.containsKey(WebUserSetFields.SET_ID)) {
		String setId = searchCriteria.get(WebUserSetFields.SET_ID);
		if (! UserSetUtils.isInteger(setId)) {
			throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
					new String[] { WebUserSetFields.SET_ID,
							setId });
		}
		searchQuery.setSetId(setId);
	}

	if (searchCriteria.containsKey(WebUserSetFields.TEXT)) {
		String text = searchCriteria.get(WebUserSetFields.TEXT);
		searchQuery.setText(text);
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
	boolean searchAllQuery = isSearchAllQuery(query);
	if (!searchAllQuery) {
	    parseCriterion(criteria, query);
	}

	if (qf != null) {
	    for (String criterion : qf) {
		parseCriterion(criteria, criterion);
	    }
	}
    }

    public static boolean isSearchAllQuery(String query) {
	return SEARCH_ALL.equals(query) || SEARCH_ALL_ALL.equals(query);
    }

    private void parseCriterion(Map<String, String> criteria, String criterion) throws ParamValidationException {
	String toParse = criterion;
	String separator = WebUserSetFields.SEPARATOR_SEMICOLON;
	String space = " ";
	String field;
	String value;

	while (toParse.contains(separator)) {
	    field = StringUtils.substringBefore(toParse, separator).trim();
	    if(StringUtils.isEmpty(field)) {
		//default to text
		field = WebUserSetFields.TEXT;
	    }
	    
	    toParse = StringUtils.substringAfter(toParse, separator).trim();
	    if (!suportedFields.contains(field)) {
		// invalid field name
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid field name in search query", field });
	    }

//	    value = StringUtils.substringBefore(toParse, space);
//	    toParse = StringUtils.substringAfter(toParse, space);
	    if(!toParse.contains(separator) || toParse.startsWith("http")) {
		//allow separator for URIs
		//TODO: for the time being we assume that queries with URIs do not use multiple criterions
		value = toParse;
	    }else {
		//multiple search criteria, extract value and remove processed criterion
		value = StringUtils.substringBefore(toParse, separator).trim();
		//remove next field name
		if(!value.contains(space)) {
		    //invalid query format, there must be a space before next field in the query
		    throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
				new String[] { "invalid formatting of search query for field " + field, value });
		}
		//extract correct search value for current field
		value = StringUtils.substringBeforeLast(toParse, space);
		//remove processed value from query string
		toParse = StringUtils.removeStart(toParse, value);
		
	    }
	    
	    if (StringUtils.isBlank(value) || (!value.startsWith("http") && value.contains(separator))) {
		// invalid seearch value
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
			new String[] { "invalid formatting of search query for field " + field, value });
	    }
	    criteria.put(field, value);
	}
    }
}
