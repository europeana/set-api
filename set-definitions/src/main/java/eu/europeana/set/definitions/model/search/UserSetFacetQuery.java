package eu.europeana.set.definitions.model.search;

public class UserSetFacetQuery {

    private String outputField;
    private String matchField;
    private String matchValue;
    private boolean unwind;
    private String facet;
    private int facetLimit;

    public UserSetFacetQuery(String outputField, String matchField, String matchValue, boolean unwind, String facet, int facetLimit) {
        this.outputField = outputField;
        this.matchField = matchField;
        this.matchValue = matchValue;
        this.unwind = unwind;
        this.facet = facet;
        this.facetLimit = facetLimit;
    }

    public String getOutputField() {
        return outputField;
    }

    public void setOutputField(String outputField) {
        this.outputField = outputField;
    }

    public String getMatchField() {
        return matchField;
    }

    public void setMatchField(String matchField) {
        this.matchField = matchField;
    }

    public String getMatchValue() {
        return matchValue;
    }

    public void setMatchValue(String matchValue) {
        this.matchValue = matchValue;
    }

    public boolean isUnwind() {
        return unwind;
    }

    public void setUnwind(boolean unwind) {
        this.unwind = unwind;
    }

    public String getFacet() {
        return facet;
    }

    public void setFacet(String facet) {
        this.facet = facet;
    }

    public int getFacetLimit() {
        return facetLimit;
    }

    public void setFacetLimit(int facetLimit) {
        this.facetLimit = facetLimit;
    }
}
