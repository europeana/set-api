package eu.europeana.set.web.search;

import java.util.*;

import eu.europeana.api.common.config.UserSetI18nConstants;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.vocabulary.*;
import eu.europeana.set.web.exception.request.RequestValidationException;
import org.apache.commons.lang3.StringUtils;

import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.search.util.QueryBuilder;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.utils.UserSetUtils;

public class UserSetQueryBuilder extends QueryBuilder {

    String[] fields = new String[] {WebUserSetModelFields.CREATOR, WebUserSetModelFields.VISIBILITY,
	    WebUserSetFields.TYPE, WebUserSetFields.ITEM, WebUserSetFields.SET_ID, WebUserSetFields.CONTRIBUTOR, WebUserSetFields.SUBJECT};
    String[] facetsFields = new String[] {WebUserSetModelFields.VISIBILITY, WebUserSetFields.ITEM};

    Set<String> suportedFields = Set.of(fields);
	Set<String> supportedFacets = Set.of(facetsFields);

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

	// if query field is not empty, default to text-title search
	// Multiple criteria are not supported with text-title search
	if (!toParse.isEmpty() && !toParse.contains(separator)) {
		criteria.put(WebUserSetFields.TEXT, toParse);
	}

	while (toParse.contains(separator)) {
	    field = StringUtils.substringBefore(toParse, separator).trim();
	    
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
		toParse="";
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

    public UserSetFacetQuery buildUserSetFacetQuery(String facet, int facetLimit, LdProfiles profile) throws RequestValidationException, ParamValidationException {
	if (LdProfiles.FACETS == profile && (facet == null || facet.isEmpty()) ) {
		throw new RequestValidationException(UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
				new String[]{CommonApiConstants.QUERY_PARAM_FACET + " (for facets profile)"});
		}
	validateFacet(facet);
	return buildFacetQuery(facet, facetLimit);
    }

	/**
	 * Will validate the facets field.
	 * For now, we don't support multiple facets seperated with comma
	 * @param facet
	 * @return
	 * @throws ParamValidationException
	 */
    private void validateFacet(String facet) throws ParamValidationException {
    if (facet.contains(",")) {
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
				new String[] { "multiple facet value is not supported ", facet });
	}
    if (!supportedFacets.contains(facet)) {
		throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE, I18nConstants.INVALID_PARAM_VALUE,
				new String[] { "parameter value not supported in facets query ", facet });
	}
    }

	/**
	 * Builds facets query
	 * facet value will be converted to mongo values
	 * so $ is appended along with mongo Db value
	 * ex : '$items' OR '$visibility'
	 *
	 * @param facet
	 * @param facetLimit
	 * @return
	 */
    private UserSetFacetQuery buildFacetQuery(String facet, int facetLimit) {
    // For item facets - we get the most liked items. Hence, the match should be {type : 'BookmarkFolder'}
	// also as items is an array unwind will be true
    if(facet.equals(WebUserSetFields.ITEM)) {
    	return new UserSetFacetQuery(WebUserSetFields.MOST_LIKED_ITEMS_FACETS, WebUserSetFields.TYPE, UserSetTypes.BOOKMARKSFOLDER.getJsonValue(),
				true, WebUserSetFields.MONGO_ITEMS, facetLimit);
	}
    if(facet.equals(WebUserSetFields.VISIBILITY)) {
		return new UserSetFacetQuery(WebUserSetFields.VISIBILITY, null, null,
				false, WebUserSetFields.MONGO_VISIBILITY, facetLimit);
	}
    return null;
    }

}
