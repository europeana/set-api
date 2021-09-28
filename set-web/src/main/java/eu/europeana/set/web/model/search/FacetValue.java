package eu.europeana.set.web.model.search;

import com.fasterxml.jackson.annotation.JsonGetter;

import eu.europeana.set.definitions.model.vocabulary.WebUserSetFields;

public class FacetValue {

    private String label;
    private long count;

    public FacetValue(String label, long count) {
        this.label = label;
        this.count = count;
    }

    @JsonGetter(WebUserSetFields.LABEL)
    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    @JsonGetter(WebUserSetFields.COUNT)
    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

}
