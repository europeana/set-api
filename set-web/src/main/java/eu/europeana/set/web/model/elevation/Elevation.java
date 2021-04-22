package eu.europeana.set.web.model.elevation;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.util.List;

@JacksonXmlRootElement(localName = "elevate")
public class Elevation {

    @JacksonXmlProperty(localName = "query")
    private List<Query> query;

    public List<Query> getQuery() {
        return query;
    }

    public void setQuery(List<Query> query) {
        this.query = query;
    }

}
