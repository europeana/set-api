package eu.europeana.set.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.impl.BaseUserSet;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * Used for Deserializing UserSet.
 * <p>
 * Field - {@link UserSet#getIdentifier()}
 * As Identifier is always null, we fetch the value from the 'id' field in the response
 * exmaple : 'id' : http://data.europeana.eu/set/xyz , identifier : xyz
 *
 * @author srishti singh
 * @since 9 December 2024
 */
public class UserSetDeserializer extends JsonDeserializer<UserSet> {

    @Override
    public UserSet deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        if (mapper.readTree(jsonParser).isObject()) {
            ObjectNode root = mapper.readTree(jsonParser);
            BaseUserSet set = mapper.readValue(root.toString(), BaseUserSet.class);
            if (root.has("id")) {
                String id = root.get("id").asText();
                String identifier = set.getIdentifier();
                if (identifier == null) {
                    set.setIdentifier(StringUtils.substringAfterLast(id, "/"));
                }
            }
            return set;
        } else {
            // there are profiles where only id value is returned instead of UserSet object
           //  hence we will form a userset object with 'id' value and return that
            JsonNode root = mapper.readTree(jsonParser);
            String id = mapper.readValue(root.toString(), String.class);
            UserSet set = new BaseUserSet();
            set.setIdentifier(StringUtils.substringAfterLast(id, "/"));
            return set;
        }
    }
}
