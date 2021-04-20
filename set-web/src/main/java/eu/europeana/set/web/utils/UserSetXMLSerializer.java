package eu.europeana.set.web.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule;
import eu.europeana.set.web.model.elevation.Elevation;

import java.io.IOException;

public class UserSetXMLSerializer {

    XmlMapper mapper = new XmlMapper();

    public UserSetXMLSerializer() {
        mapper.configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true );
        mapper.setDefaultUseWrapper(false);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
    }

    /**
     * This method provides full serialization of Elevation object
     *
     * @param elevation
     * @return xml string
     * @throws IOException
     */
    public String serialize(Elevation elevation) throws JsonProcessingException {
        mapper.registerModule(new JaxbAnnotationModule());
        return mapper.writeValueAsString(elevation);
    }

}
