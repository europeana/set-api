package eu.europeana.set.definitions.model.agent;

import eu.europeana.set.definitions.model.vocabulary.AgentTypes;

public interface Agent {

	public abstract void setHttpUrl(String httpUrl);

	public abstract String getHttpUrl();
	
	public abstract void setType(String agentTypeStr);
	
	public abstract void setAgentTypeAsString(String agentTypeStr);
	
	public abstract void setAgentTypeEnum(AgentTypes agentType);
	
	public abstract String getType();

	public String getInternalType();

	public void setInternalType(String internalType);
	
	public abstract void setHomepage(String homepage);

	public abstract String getHomepage();

	public abstract void setEmail(String email);

	public abstract String getEmail();

	public abstract void setEmailSha1(String emailSha1);

	public abstract String getEmailSha1();

	public abstract void setNickname(String nickname);

	public abstract String getNickname();

	public abstract void setName(String name);

	public abstract String getName();
	
	public abstract void setInputString(String string);
	
	public abstract String getInputString();
	
	public boolean equalsContent(Object other);

	void setUserGroup(String userGroup);
	
	String getUserGroup();

}
