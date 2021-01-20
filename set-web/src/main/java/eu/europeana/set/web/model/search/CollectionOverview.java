package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import eu.europeana.api.commons.definitions.vocabulary.CommonLdConstants;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;
import eu.europeana.set.definitions.model.vocabulary.WebUserSetModelFields;

@JsonPropertyOrder({WebUserSetModelFields.ID, WebUserSetModelFields.TYPE, WebUserSetModelFields.TOTAL,
	WebUserSetFields.FIRST, WebUserSetFields.LAST})
@JsonInclude(value = JsonInclude.Include.NON_NULL)
public class CollectionOverview {
    
    private String id;
    private Long total;
    private String first;
    private String last;
    private String type;
    
    public String getType() {
        return type;
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
    

    public CollectionOverview(String id, Long total, String first, String last) {
        this(id, total, first, last, CommonLdConstants.RESULT_LIST);
    }
    
    public CollectionOverview(String id, Long total, String first, String last, String type) {
        this.id = id;
        this.total = total;
        this.first = first;
        this.last = last;
        this.type = type;
    }
}
