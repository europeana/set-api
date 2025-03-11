package eu.europeana.set.search;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class SearchApiRequest {

    private String query;
    private List<String> qf;
    private List<String> reusability;
    private int start = 1;
    private int rows ;
    private List<String> sort;
    private List<String> profile;


    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<String> getQf() {
        return qf;
    }

    public void setQf(List<String> qf) {
        this.qf = qf;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
    }

    public List<String> getSort() {
        return sort;
    }

    public void setSort(List<String> sort) {
        this.sort = sort;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public List<String> getProfile() {
      return profile;
    }

    public void setProfile(List<String> profile) {
      this.profile = profile;
    }

    public List<String> getReusability() {
      return reusability;
    }

    public void setReusability(List<String> reusability) {
      this.reusability = reusability;
    }
}
