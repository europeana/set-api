package eu.europeana.set.utils.serialization;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.set.utils.JsonUtils;

/**
 * @author RomanG
 */
public class ListDeserializer extends JsonDeserializer<List<String>> {

    @Override
    public List<String> deserialize(JsonParser jp, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        List<String> valueList = new ArrayList<String>();
        JsonNode jn = mapper.readTree(jp);
        if (jn != null) {
            String valueListStr = jn.toString();//getTextValue();
            if (StringUtils.isNotEmpty(valueListStr))
            	valueList = JsonUtils.stringToList(valueListStr);
        }
        return valueList;
    }
}