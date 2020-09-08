package eu.europeana.set.definitions.model.agent.impl;

import eu.europeana.set.definitions.model.agent.Agent;
import eu.europeana.set.definitions.model.vocabulary.AgentTypes;

public abstract class BaseAgent implements Agent {

    private String httpUrl;
    private String agentType;
    private String internalType;
    private String name;
    private String email;
    private String emailSha1;
    private String nickname;

    private String homepage;
    private String inputString;
    private String userGroup;

    @Override
    public String getHttpUrl() {
        return httpUrl;
    }

    @Override
    public void setHttpUrl(String httpUrl) {
        this.httpUrl = httpUrl;
    }

    @Override
    public String getUserGroup() {
        return userGroup;
    }

    @Override
    public void setUserGroup(String userGroup) {
        this.userGroup = userGroup;
    }

    @Override
    public String getType() {
        return agentType;
    }

    @Override
    public void setType(String agentType) {
        this.agentType = agentType;
    }

    @Override
    public String getInternalType() {
        return internalType;
    }

    @Override
    public void setInternalType(String internalType) {
        this.internalType = internalType;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String getEmailSha1() {
        return emailSha1;
    }

    @Override
    public void setEmailSha1(String emailSha1) {
        this.emailSha1 = emailSha1;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    @Override
    public String getHomepage() {
        return homepage;
    }

    @Override
    public void setHomepage(String homepage) {
        this.homepage = homepage;
    }

    @Override
    public String getInputString() {
        return inputString;
    }

    @Override
    public void setInputString(String inputString) {
        this.inputString = inputString;
    }

    @Override
    public void setAgentTypeEnum(AgentTypes curAgentType) {
        agentType = curAgentType.name();
    }

    @Override
    public void setAgentTypeAsString(String agentTypeStr) {
        agentType = agentTypeStr;
    }

    protected BaseAgent() {
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Agent)) {
            return false;
        }
        Agent that = (Agent) other;

        boolean res = true;

        /**
         * equality check for all relevant fields.
         */
        if ((this.getType() != null) && (that.getType() != null) &&
                (!this.getType().equals(that.getType()))) {
            res = false;
        }

        if ((this.getHomepage() != null) && (that.getHomepage() != null) &&
                (!this.getHomepage().equals(that.getHomepage()))) {
            res = false;
        }

        if ((this.getName() != null) && (that.getName() != null) &&
                (!this.getName().equals(that.getName()))) {
            res = false;
        }

        if ((this.getHttpUrl() != null) && (that.getHttpUrl() != null) &&
                (!this.getHttpUrl().equals(that.getHttpUrl()))) {
            res = false;
        }
        return res;
    }

    public boolean equalsContent(Object other) {
        return equals(other);
    }

    @Override
    public String toString() {
        String res = "\t### Agent ###\n";

        if (getType() != null)
            res = res + "\t\t" + "agentType:" + getType() + "\n";
        if (getName() != null)
            res = res + "\t\t" + "name:" + getName() + "\n";
        if (getHttpUrl() != null)
            res = res + "\t\t" + "httpUrl:" + getHttpUrl() + "\n";
        if (getHomepage() != null)
            res = res + "\t\t" + "homepage:" + getHomepage() + "\n";
        return res;
    }
}
