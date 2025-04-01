package eu.europeana.set.web.search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.api.commons.definitions.config.i18n.I18nConstants;
import eu.europeana.api.commons.definitions.utils.LanguageUtils;
import eu.europeana.api.commons.definitions.vocabulary.CommonApiConstants;
import eu.europeana.api.commons.search.util.QueryBuilder;
import eu.europeana.api.commons.web.exception.ParamValidationException;
import eu.europeana.set.definitions.config.UserSetConfiguration;
import eu.europeana.set.definitions.model.search.UserSetFacetQuery;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;
import eu.europeana.set.definitions.model.utils.UserSetUtils;
import eu.europeana.set.definitions.model.vocabulary.UserSetTypes;
import eu.europeana.set.definitions.model.vocabulary.VisibilityTypes;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;
import eu.europeana.set.mongo.model.UserSetMongoConstants;
import eu.europeana.set.web.config.UserSetI18nConstants;
import eu.europeana.set.web.exception.request.RequestValidationException;

public class UserSetQueryBuilder extends QueryBuilder {
  
  public static final String SEARCH_ALL = "*";
  public static final String SEARCH_ALL_ALL = "*:*";
  public static final String PREFIX_HTTP = "http";

  static Set<String> arrayFields = Set.of(WebUserSetFields.ITEM, WebUserSetModelFields.CONTRIBUTOR, 
      WebUserSetModelFields.SUBJECT, WebUserSetModelFields.CREATOR, WebUserSetModelFields.VISIBILITY,
      WebUserSetModelFields.TYPE, WebUserSetModelFields.COLLECTION_TYPE, WebUserSetFields.SET_ID,
      WebUserSetModelFields.PROVIDER);
  static Set<String> nonArrayFields = Set.of(WebUserSetFields.LANG);
  static Set<String> facetsFields = Set.of(WebUserSetModelFields.VISIBILITY, WebUserSetFields.ITEM);
  static Set<String> suportedFields;
  static {
    suportedFields = new HashSet<>();
    suportedFields.addAll(arrayFields);
    suportedFields.addAll(nonArrayFields);    
  }
  
  private UserSetQuery buildSearchQuery(Map<String, Object> searchCriteria, String sort, int page,
      int pageSize, UserSetConfiguration config) throws ParamValidationException {
    UserSetQuery searchQuery = new UserSetQueryImpl();
    searchQuery.setQuery(searchCriteria.toString());

    addVisibilityCriterion(searchCriteria, searchQuery);

    addTypeCriterion(searchCriteria, searchQuery);

    addCollectionTypeCriterion(searchCriteria, searchQuery);

    addCreatorCriterion(searchCriteria, searchQuery, config.getUserDataEndpoint());

    addContributorCriterion(searchCriteria, searchQuery, config.getUserDataEndpoint());

    addProviderCriterion(searchCriteria, searchQuery);

    addSubjectCriterion(searchCriteria, searchQuery);

    addItemCriterion(searchCriteria, searchQuery, config.getItemDataEndpoint());

    addSetIdCriterion(searchCriteria, searchQuery);

    addFullTextCriterion(searchCriteria, searchQuery);

    addTitleLangCriterion(searchCriteria, searchQuery);

    addSortCriterion(searchCriteria, searchQuery, sort);

    searchQuery.setPageSize(pageSize);
    searchQuery.setPageNr(page);

    return searchQuery;
  }

  private void addSortCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery,
      String sort) throws ParamValidationException {
    // validate sorting based on score (can be only in the descending order)
    if (sort != null && sort.contains(WebUserSetFields.TEXT_SCORE_SORT)
        && !searchCriteria.containsKey(WebUserSetModelFields.TEXT)) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE,
          new String[] {
              "invalid value for the sort field, it cannot contain 'score' if the search is not on the text field",
              sort});
    }
    String[] sortCriteria = toArray(sort);
    if (sortCriteria != null) {
      for (String sortCriterion : sortCriteria) {
        if (sortCriterion.contains(WebUserSetFields.TEXT_SCORE_SORT)
            && sortCriterion.contains(WebUserSetFields.SORT_ORDER_ASC)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE,
              new String[] {
                  "invalid value for the sort field, it cannot contain 'score asc' since only the descending order is supported",
                  sort});
        }
      }
    }
    searchQuery.setSortCriteria(sortCriteria);
  }

  private void addTitleLangCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetFields.LANG)) {
      String lang = ((String) searchCriteria.get(WebUserSetFields.LANG)).toLowerCase(Locale.ENGLISH);
      if (!LanguageUtils.isIsoLanguage(lang)) {
        throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
            I18nConstants.INVALID_PARAM_VALUE,
            new String[] {
                "invalid value for search value for lang: field, language must be a 2-letter ISO Code",
                lang});
      }
      searchQuery.setTitleLang(lang);
    }
  }

  private void addFullTextCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) {
    if (searchCriteria.containsKey(WebUserSetModelFields.TEXT)) {
      String text = (String) searchCriteria.get(WebUserSetModelFields.TEXT);
      searchQuery.setText(text);
    }
  }

  private void addSetIdCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetFields.SET_ID)) {
      List<String> setId = (List<String>) searchCriteria.get(WebUserSetFields.SET_ID);
      for(String id : setId) {
        if (!UserSetUtils.isInteger(id)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE, new String[] {WebUserSetFields.SET_ID, id});
        }        
      }
      searchQuery.setSetId(setId);
    }
  }

  private void addItemCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery,
      String itemDataEndpoint) {
    if (searchCriteria.containsKey(WebUserSetFields.ITEM)) {
      List<String> items = (List<String>) searchCriteria.get(WebUserSetFields.ITEM);
      for(int i=0;i<items.size();i++) {
        if (!items.get(i).startsWith(PREFIX_HTTP)) {
          items.set(i, UserSetUtils.buildItemUrl(itemDataEndpoint, items.get(i)));
        }        
      }
      searchQuery.setItem(items);
    }
  }

  private void addSubjectCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetModelFields.SUBJECT)) {
      List<String> subjects = (List<String>) searchCriteria.get(WebUserSetModelFields.SUBJECT);
      for(String subj : subjects) {
        if (!subj.startsWith(PREFIX_HTTP)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE,
              new String[] {"invalid value for search field, subject must be a URI", subj});
        }        
      }
      searchQuery.setSubject(subjects);
    }
  }

  private void addContributorCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery,
      String userDataEndpoint) {
    if (searchCriteria.containsKey(WebUserSetModelFields.CONTRIBUTOR)) {
      List<String> contributorId = (List<String>) searchCriteria.get(WebUserSetModelFields.CONTRIBUTOR);
      for(int i=0;i<contributorId.size();i++) {
        if (!contributorId.get(i).startsWith(PREFIX_HTTP)) {
          contributorId.set(i, UserSetUtils.buildUserUri(userDataEndpoint, contributorId.get(i)));
        }
        
      }
      searchQuery.setContributor(contributorId);
    }
  }

  private void addCreatorCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery, 
      String userDataEndpoint) {
    if (searchCriteria.containsKey(WebUserSetModelFields.CREATOR)) {
      List<String> creatorId = (List<String>) searchCriteria.get(WebUserSetModelFields.CREATOR);
      for(int i=0;i<creatorId.size();i++) {
        if (!creatorId.get(i).startsWith(PREFIX_HTTP)) {
          creatorId.set(i, UserSetUtils.buildUserUri(userDataEndpoint, creatorId.get(i)));
        }
      }
      searchQuery.setCreator(creatorId);
    }
  }

  private void addProviderCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetModelFields.PROVIDER)) {
      List<String> providerId = (List<String>) searchCriteria.get(WebUserSetModelFields.PROVIDER);
      for(String prov : providerId) {
        if (!prov.startsWith(PREFIX_HTTP)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE, new String[] {
                  "invalid value for search field, provider (id) must be a URI", prov});
        }        
      }
      searchQuery.setProvider(providerId);
    }
  }

  private void addTypeCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetModelFields.TYPE)) {
      List<String> type = (List<String>) searchCriteria.get(WebUserSetModelFields.TYPE);
      for(String el : type) {
        if (!UserSetTypes.isValid(el)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE,
              new String[] {"invalid value for the type field: ", el});
        }
      }
      searchQuery.setType(type);
    }
  }

  private void addCollectionTypeCriterion(Map<String, Object> searchCriteria,
      UserSetQuery searchQuery) throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetModelFields.COLLECTION_TYPE)) {
      List<String> collectionType = (List<String>) searchCriteria.get(WebUserSetModelFields.COLLECTION_TYPE);
      for(String el : collectionType) {
        if (!WebUserSetModelFields.TYPE_GALLERY.equals(el)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE,
              new String[] {"invalid value for the collectionType field: ", el});
        }        
      }
      searchQuery.setCollectionType(collectionType);
    }
  }

  private void addVisibilityCriterion(Map<String, Object> searchCriteria, UserSetQuery searchQuery) 
      throws ParamValidationException {
    if (searchCriteria.containsKey(WebUserSetModelFields.VISIBILITY)) {
      List<String> visibility = (List<String>) searchCriteria.get(WebUserSetModelFields.VISIBILITY);
      for(String visib : visibility) {
        if (!VisibilityTypes.isValid(visib)) {
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE, new String[] {
                  "invalid value for the visibility field: ", visib});
        }
      }
      searchQuery.setVisibility(visibility);
    }
  }

  public UserSetQuery buildUserSetQuery(String query, String qf[], String sort, int page,
      int pageSize, UserSetConfiguration config) throws ParamValidationException {

    Map<String, Object> criteria = new HashMap<>();
    parseSearchCriteria(criteria, query, qf);
    return buildSearchQuery(criteria, sort, page, pageSize, config);
  }

  private void parseSearchCriteria(Map<String, Object> criteria, String query, String[] qf) 
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

  private void parseCriterion(Map<String, Object> criteria, String criterion)
      throws ParamValidationException {
    String toParse = criterion;
    String separator = WebUserSetFields.SEPARATOR_SEMICOLON;
    String space = " ";
    String field;
    String value;

    // if query field is not empty, default to text-title search
    // Multiple criteria are not supported with text-title search
    if (!toParse.isEmpty() && !toParse.contains(separator)) {
      criteria.put(WebUserSetModelFields.TEXT, toParse);
    }

    while (toParse.contains(separator)) {
      field = StringUtils.substringBefore(toParse, separator).trim();

      toParse = StringUtils.substringAfter(toParse, separator).trim();
      if (!suportedFields.contains(field)) {
        // invalid field name
        throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
            I18nConstants.INVALID_PARAM_VALUE,
            new String[] {"invalid field name in search query", field});
      }

      // value = StringUtils.substringBefore(toParse, space);
      // toParse = StringUtils.substringAfter(toParse, space);
      if (!toParse.contains(separator) || toParse.startsWith("http")) {
        // allow separator for URIs
        // TODO: for the time being we assume that queries with URIs do not use multiple criterions
        value = toParse;
        toParse = "";
      } else {
        // multiple search criteria, extract value and remove processed criterion
        value = StringUtils.substringBefore(toParse, separator).trim();
        // remove next field name
        if (!value.contains(space)) {
          // invalid query format, there must be a space before next field in the query
          throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
              I18nConstants.INVALID_PARAM_VALUE,
              new String[] {"invalid formatting of search query for field " + field, value});
        }
        // extract correct search value for current field
        value = StringUtils.substringBeforeLast(toParse, space);
        // remove processed value from query string
        toParse = StringUtils.removeStart(toParse, value);
      }

      if (StringUtils.isBlank(value) || (!value.startsWith("http") && value.contains(separator))) {
        // invalid seearch value
        throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
            I18nConstants.INVALID_PARAM_VALUE,
            new String[] {"invalid formatting of search query for field " + field, value});
      }
      
      addCriterionToOthers(field, value, criteria);
    }
  }
  
  private void addCriterionToOthers(String field, String value, Map<String, Object> criteria) {
    //field is of type List
    if(arrayFields.contains(field)) {
      if(criteria.containsKey(field)) {
        List<String> val = (List<String>) criteria.get(field);
        val.add(value);
      } else {
        List<String> val = new ArrayList<>();
        val.add(value);
        criteria.put(field, val);
      }
    } else {
      criteria.put(field, value);
    }    
  }
  
  public UserSetFacetQuery buildUserSetFacetQuery(String facet, int facetLimit)
      throws RequestValidationException, ParamValidationException {
    if (facet == null || facet.isEmpty()) {
      throw new RequestValidationException(
          UserSetI18nConstants.USERSET_VALIDATION_MANDATORY_PROPERTY,
          new String[] {CommonApiConstants.QUERY_PARAM_FACET + " (for facets profile)"});
    }
    validateFacetAndLimit(facet, facetLimit);
    return buildFacetQuery(facet, facetLimit);
  }

  /**
   * Will validate the facet and the facet limit fields. For now, we don't support multiple facets
   * seperated with comma
   * 
   * @param facet
   * @param facetLimit
   * @throws ParamValidationException
   */
  private void validateFacetAndLimit(String facet, int facetLimit) throws ParamValidationException {
    if (facet.contains(",")) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE,
          new String[] {"multiple facet value is not supported ", facet});
    }
    if (!facetsFields.contains(facet)) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE,
          new String[] {"parameter value not supported in facets query ", facet});
    }
    if (facetLimit <= 0) {
      throw new ParamValidationException(I18nConstants.INVALID_PARAM_VALUE,
          I18nConstants.INVALID_PARAM_VALUE,
          new String[] {"facet limit value needs to be positive ", String.valueOf(facetLimit)});
    }
  }

  /**
   * Builds facets query facet value will be converted to mongo values so $ is appended along with
   * mongo Db value ex : '$items' OR '$visibility'
   *
   * @param facet
   * @param facetLimit
   * @return
   */
  private UserSetFacetQuery buildFacetQuery(String facet, int facetLimit) {
    // For item facets - we get the most liked items. Hence, the match should be {type :
    // 'BookmarkFolder'}
    // also as items is an array unwind will be true
    if (WebUserSetFields.ITEM.equals(facet)) {
      return new UserSetFacetQuery(facet, WebUserSetModelFields.TYPE,
          UserSetTypes.BOOKMARKSFOLDER.getJsonValue(), true, UserSetMongoConstants.MONGO_ITEMS,
          facetLimit);
    }
    if (WebUserSetModelFields.VISIBILITY.equals(facet)) {
      return new UserSetFacetQuery(facet, null, null, false, UserSetMongoConstants.MONGO_VISIBILITY,
          facetLimit);
    }
    return null;
  }

}
