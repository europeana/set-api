package eu.europeana.set.definitions.model.factory.impl;

import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.agent.impl.Person;
//import eu.europeana.set.definitions.model.agent.impl.Organization;
//import eu.europeana.set.definitions.model.agent.impl.Person;
import eu.europeana.set.definitions.model.agent.impl.SoftwareAgent;
import eu.europeana.set.definitions.model.factory.AbstractModelObjectFactory;
import eu.europeana.set.definitions.model.vocabulary.AgentTypes;

public class AgentObjectFactory extends
		AbstractModelObjectFactory<Agent, AgentTypes> {

	private static AgentObjectFactory singleton;

	// force singleton usage
	private AgentObjectFactory() {
	}

	public static synchronized AgentObjectFactory getInstance() {

		if (singleton == null)
			singleton = new AgentObjectFactory();

		return singleton;

	}
	
	@Override
	public Agent createObjectInstance(Enum<AgentTypes> modelObjectType) {
		Agent res = super.createObjectInstance(modelObjectType);
		res.setInternalType(modelObjectType.name());
		res.setType(modelObjectType.toString());
		return res;
	}	

	@Override
	public Class<? extends Agent> getClassForType(Enum<AgentTypes> modelType) {

		Class<? extends Agent> returnType = null;
		AgentTypes agentType = AgentTypes.valueOf(modelType.name());
		switch (agentType) {
		case PERSON:
			returnType = Person.class;
			break;

//		case ORGANIZATION:
//			returnType = Organization.class;
//			break;

		case SOFTWARE:
			returnType = SoftwareAgent.class; 
			break;

		default:
			break;
		}

		return returnType;
	}

	@Override
	public Class<AgentTypes> getEnumClass() {
		return AgentTypes.class;
	}
	
}
