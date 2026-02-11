package eu.europeana.set.search.service;

import java.util.ArrayList;
import java.util.List;

/**
 * Response class for Search API
 * @author Srishti singh
 */
public class SearchApiResponse {

    private List<String> items = new ArrayList<>();

    private int total;

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }
}
