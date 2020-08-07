package eu.europeana.set.search.service;

import java.util.ArrayList;
import java.util.List;

public class SearchApiResponse extends ApiResponse{


//	public static String ERROR_SUGGESTION_NOT_FOUND = "Suggestion not found!";
//	public static String ERROR_USERSET_NOT_FOUND = "User set not found!";

	public SearchApiResponse(String apiKey, String action) {
		super(apiKey, action);
	}

    private List<String> items = new ArrayList<>();
    private int total;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

}
