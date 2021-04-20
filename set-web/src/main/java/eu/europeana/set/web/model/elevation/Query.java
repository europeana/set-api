package eu.europeana.set.web.model.elevation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.List;

public class Query {

    @JacksonXmlProperty(isAttribute = true)
    private String text;

    @JacksonXmlProperty
    private List<Doc> doc;

    public Query(String text, List<Doc> doc) {
        this.text = text;
        this.doc = doc;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<Doc> getDoc() {
        return doc;
    }

    public void setDoc(List<Doc> doc) {
        this.doc = doc;
    }

}
