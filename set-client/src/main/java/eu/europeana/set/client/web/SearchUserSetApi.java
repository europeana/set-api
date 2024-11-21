package eu.europeana.set.client.web;

import org.springframework.http.ResponseEntity;

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
    public ResponseEntity<String> searchUserSet(String query, String[] qf, String sort, int page, int pageSize,
                                                String facet, int facetLimit, String profile);
}
