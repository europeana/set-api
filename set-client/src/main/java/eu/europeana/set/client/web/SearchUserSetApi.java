package eu.europeana.set.client.web;

import eu.europeana.api.commons.definitions.search.result.impl.ResultsPageImpl;
import eu.europeana.set.client.exception.SetApiClientException;
import eu.europeana.set.definitions.model.UserSet;

/**
 * Search Client API interface
 * @author Srishti singh
 * @since 20 Nov 2024
 */
public interface SearchUserSetApi {

    /**
     * This methods retrieves the search results from the db
     * @param query
     * @param qf
     * @param sort
     * @param page
     * @param pageSize
     * @param facet
     * @param facetLimit
     * @param profile
     * @return
     */
    ResultsPageImpl<? extends UserSet> searchUserSet(String query, String[] qf, String sort, int page, int pageSize,
                                                     String facet, int facetLimit, String profile) throws SetApiClientException;
}
