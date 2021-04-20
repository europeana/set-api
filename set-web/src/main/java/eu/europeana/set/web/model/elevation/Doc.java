package eu.europeana.set.web.model.elevation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

public class Doc {

    @JacksonXmlProperty(isAttribute = true)
    private String id;

    public Doc(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

}
