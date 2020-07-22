package eu.europeana.set.web.search;

import eu.europeana.api.commons.search.util.QueryBuilder;
import eu.europeana.set.definitions.model.search.UserSetQuery;
import eu.europeana.set.definitions.model.search.UserSetQueryImpl;

public class UserSetQueryBuilder extends QueryBuilder {

    public UserSetQuery buildSearchQuery(String query, String creatorId, String visibility, String type, String sort, int page, int pageSize) {
    UserSetQuery searchQuery = new UserSetQueryImpl();
	searchQuery.setQuery(query);
	searchQuery.setCreator(creatorId);
	searchQuery.setVisibility(visibility);
	searchQuery.setType(type);
	searchQuery.setSortCriteria(toArray(sort));	
	searchQuery.setPageSize(pageSize);
	searchQuery.setPageNr(page);
	
	return searchQuery;
    }
}
