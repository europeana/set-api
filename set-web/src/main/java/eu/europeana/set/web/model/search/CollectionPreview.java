package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

/**
 * consider using ResultList whenever possible
 * 
 * @author GordeaS
 *
 */
@JsonPropertyOrder({WebUserSetModelFields.TYPE, WebUserSetModelFields.ID, WebUserSetModelFields.TOTAL,
	WebUserSetFields.FIRST, WebUserSetFields.LAST})
@JsonInclude(value = JsonInclude.Include.NON_EMPTY)
public class CollectionPreview {

    private String id;
    private Long total;
    private String first;
    private String last;
    private String type;
    public String getType() {
        return type;
    }

    public CollectionPreview() {
        super();
    }

    public CollectionPreview(String id, Long total, String first, String last, String type) {
        this.id = id;
        this.total = total;
        this.first = first;
        this.last = last;
        this.type = type;
    }

    @JsonProperty(WebUserSetModelFields.ID)
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonProperty(WebUserSetFields.FIRST)
    public String getFirst() {
        return first;
    }

    public void setFirst(String first) {
        this.first = first;
    }

    @JsonProperty(WebUserSetFields.LAST)
    public String getLast() {
        return last;
    }

    public void setLast(String last) {
        this.last = last;
    }

    @JsonProperty(WebUserSetFields.TOTAL)
    public Long getTotal() {
        return total;
    }
    public void setTotal(Long total) {
        this.total = total;
    } 
}
