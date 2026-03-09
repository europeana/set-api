package eu.europeana.set.web.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.commons_sb3.definitions.search.impl.QueryImpl;

/**
 * Class for modeling the input for Set in search using post method
 */
@JsonInclude(Include.NON_NULL)
public class SearchInSetQuery extends QueryImpl {

  private List<String> profile;
  
  /**
   * default constructor
   */
  public SearchInSetQuery() {
    super();
  }
  
  /**
   * Constructor for typically used fields in the query
   * @param filters list of item:<record_id> tupples
   * @param page page to retrieve
   * @param pageSize nr of items per page
   * @param profile the requested profiles
   */
  public SearchInSetQuery(String[] filters, int page, int pageSize, List<String> profile) {
    super();
    if(profile != null) {
      this.profile = List.copyOf(profile);  
    }
    
    this.setFilters(filters);
    this.setPageNr(page);
    this.setPageSize(pageSize);
     
  }
  
  @JsonProperty("qf")
  @Override
  public final void setFilters(String[] filters) {
    super.setFilters(filters);
  }

  @JsonProperty("query")
  @Override
  public void setQuery(String query) {
    super.setQuery(query);
  }

  public List<String> getProfile() {
    return profile;
  }

  @JsonProperty("profile")
  public void setProfile(List<String> profile) {
    this.profile = List.copyOf(profile);
  }
  
  @JsonProperty("fl")
  @Override
  public void setViewFields(String[] viewFields) {
    super.setViewFields(viewFields);
  }

  @JsonProperty("sort")
  @Override
  public void setSortCriteria(String[] sortCriteria) {
    super.setSortCriteria(sortCriteria);
  }
  
  @JsonProperty("page")
  @Override
  public final void setPageNr(int pageNr) {
    super.setPageNr(pageNr);
  }
  
  @JsonProperty("facet")
  @Override
  public void setFacetFields(String[] facetFields) {
    super.setFacetFields(facetFields);
  }
  
  @JsonProperty("pageSize")
  @Override
  public final void setPageSize(int pageSize) {
      super.setPageSize(pageSize);
  }

  
  
}
