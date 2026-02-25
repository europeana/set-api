package eu.europeana.set.web.model;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import eu.europeana.api.commons_sb3.definitions.search.impl.QueryImpl;

@JsonInclude(Include.NON_NULL)
public class SearchInSetQuery extends QueryImpl {

  private List<String> profile;
  
  public SearchInSetQuery() {
    super();
  }
  
  public SearchInSetQuery(String[] filters, int page, int pageSize, List<String> profile) {
    super();
    this.setFilters(filters);
    this.setPageNr(page);
    this.setPageSize(pageSize);
    this.profile  = profile;
    
  }

  @JsonProperty("qf")
  @Override
  public void setFilters(String[] filters) {
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
    this.profile = profile;
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
  public void setPageNr(int pageNr) {
    super.setPageNr(pageNr);
  }
  
  @JsonProperty("facet")
  @Override
  public void setFacetFields(String[] facetFields) {
    super.setFacetFields(facetFields);
  }
  
  
}
