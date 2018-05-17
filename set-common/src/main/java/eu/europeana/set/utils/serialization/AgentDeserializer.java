package eu.europeana.set.utils.serialization;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map.Entry;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import eu.europeana.set.definitions.model.UserSet;
import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.BaseAgent;
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.factory.impl.AgentObjectFactory;
import eu.europeana.set.definitions.model.impl.BaseUserSet;

//import eu.europeana.annotation.definitions.model.agent.Agent;
//import eu.europeana.annotation.definitions.model.factory.impl.AgentObjectFactory;
//import eu.europeana.annotation.definitions.model.utils.ModelConst;
//import eu.europeana.annotation.definitions.model.utils.TypeUtils;

public class AgentDeserializer extends JsonDeserializer<Agent> {
//	public class AgentDeserializer extends StdDeserializer<Agent> {
	
	public AgentDeserializer() {
//		super(Agent.class);
		super();		
	}

	@Override
	public Agent deserialize(JsonParser jp, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		ObjectMapper mapper = (ObjectMapper) jp.getCodec();
		ObjectNode root = (ObjectNode) mapper.readTree(jp);
		Class<? extends Agent> realClass = null;
		
        JsonNode node = jp.getCodec().readTree(jp);     
        try {       
            String id = node.get("@id").asText();
            String type = node.get("@type").asText();           
            Agent agent = new SoftwareAgent();
            agent.setType(type);
            return agent;
        } catch (Exception e) {
            e.printStackTrace();
            throw new IOException(e);
        }		
		/*
		Iterator<Entry<String, JsonNode>> elementsIterator = root.getFields();
		while (elementsIterator.hasNext()) {
			Entry<String, JsonNode> element = elementsIterator.next();
			if (ModelConst.AGENT_TYPE.equals(element.getKey())) {
				String textValue = element.getValue().toString();//.getTextValue();
				String typeValue = TypeUtils.getInternalTypeFromTypeArrayStatic(textValue).replace("\"", "");
				if (typeValue.equals("")) {
					typeValue = textValue.replace("\"", "").replace("[", "").replace("]", "");
				}
				realClass = AgentObjectFactory.getInstance()
						.getClassForType(typeValue);
				break;
			}
		}
		
		if (realClass == null)
			return null;
		
		return mapper.readValue(root, realClass);
		*/
	}
	
	
}
