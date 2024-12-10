package eu.europeana.set.client.json;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.Person;
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.vocabulary.AgentTypes;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class AgentDeserializer extends JsonDeserializer<Agent> {

    @Override
    public Agent deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
        ObjectNode root = mapper.readTree(jsonParser);

        if (root.has("type") ) {
            String agentType = root.get("type").asText();
            if (StringUtils.equals(agentType, AgentTypes.PERSON.getJsonValue())) {
                return mapper.readValue(root.toString(), Person.class);
            }
            if (StringUtils.equals(agentType, AgentTypes.SOFTWARE.getJsonValue())) {
                return mapper.readValue(root.toString(), SoftwareAgent.class);
            }
            // There is no class for Organisation type
        }
        return null;
    }
}
