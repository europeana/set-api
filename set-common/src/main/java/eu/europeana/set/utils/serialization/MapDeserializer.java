package eu.europeana.set.utils.serialization;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import eu.europeana.set.utils.JsonUtils;


public class MapDeserializer extends JsonDeserializer<Map<String, String>> {

    @Override
    public Map<String, String> deserialize(JsonParser jp, DeserializationContext context)
            throws IOException {
        ObjectMapper mapper = (ObjectMapper) jp.getCodec();
        Map<String, String> multilingualMap = new HashMap<String, String>();
        JsonNode jn = mapper.readTree(jp);
        if (jn != null) {
            String multilingualMapStr = jn.toString(); //getTextValue();
            if (StringUtils.isNotEmpty(multilingualMapStr))
            	multilingualMap = JsonUtils.stringToMap(multilingualMapStr);
        }
        return multilingualMap;
    }
}