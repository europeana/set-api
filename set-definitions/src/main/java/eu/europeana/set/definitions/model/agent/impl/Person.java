package eu.europeana.set.definitions.model.agent.impl;

import eu.europeana.set.definitions.model.vocabulary.AgentTypes;

public class Person extends BaseAgent {

	public Person(){
		super();
		setAgentTypeEnum(AgentTypes.PERSON);
	}
}
