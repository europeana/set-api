<<<<<<< HEAD
package eu.europeana.set.definitions.config;

import java.util.Properties;

public class UserSetConfigurationImpl implements UserSetConfiguration {

    private Properties setProperties;

    @Override
    public String getComponentName() {
	return "set";
    }

    public Properties getSetProperties() {
	return setProperties;
    }

    public void setSetProperties(Properties setProperties) {
	this.setProperties = setProperties;
    }

    @Override
    public String getUserSetBaseUrl() {
	String key = USERSET_ENVIRONMENT + "." + getEnvironment() + "." + SUFFIX_BASEURL;
	return getSetProperties().getProperty(key);
    }

    @Override
    public String getEnvironment() {
	return getSetProperties().getProperty(USERSET_ENVIRONMENT);
    }

    public int getMaxPageSize(String profile) {
	String key = PREFIX_MAX_PAGE_SIZE + profile;
	return Integer.parseInt(getSetProperties().getProperty(key));
    }

    @Override
    public boolean isProductionEnvironment() {
	return VALUE_ENVIRONMENT_PRODUCTION.equals(getEnvironment());
    }

    @Override
    public String getJwtTokenSignatureKey() {
	return getSetProperties().getProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY);
    }

    public String getAuthorizationApiName() {
	return getSetProperties().getProperty(AUTHORIZATION_API_NAME);
    }

    @Override
    public String getApiVersion() {
	return getSetProperties().getProperty(API_VERSION);
    }

    @Override
    public String getSearchApiKey() {
	return getSetProperties().getProperty(KEY_SEARCH_APIKEY);
    }

}
=======
package eu.europeana.set.definitions.config;

import java.util.Properties;

public class UserSetConfigurationImpl implements UserSetConfiguration {

    private Properties setProperties;

    @Override
    public String getComponentName() {
	return "set";
    }

    public Properties getSetProperties() {
	return setProperties;
    }

    public void setSetProperties(Properties setProperties) {
	this.setProperties = setProperties;
    }

    @Override
    public String getUserSetBaseUrl() {
	String key = USERSET_ENVIRONMENT + "." + getEnvironment() + "." + SUFFIX_BASEURL;
	return getSetProperties().getProperty(key);
    }

    @Override
    public String getEnvironment() {
	return getSetProperties().getProperty(USERSET_ENVIRONMENT);
    }

    public int getMaxPageSize(String profile) {
	String key = PREFIX_MAX_PAGE_SIZE + profile;
	return Integer.parseInt(getSetProperties().getProperty(key));
    }

    @Override
    public boolean isProductionEnvironment() {
	return VALUE_ENVIRONMENT_PRODUCTION.equals(getEnvironment());
    }

    @Override
    public String getJwtTokenSignatureKey() {
	return getSetProperties().getProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY);
    }

    public String getAuthorizationApiName() {
	return getSetProperties().getProperty(AUTHORIZATION_API_NAME);
    }

    @Override
    public String getApiVersion() {
	return getSetProperties().getProperty(API_VERSION);
    }

    @Override
    public String getSearchApiKey() {
	return getSetProperties().getProperty(KEY_SEARCH_APIKEY);
    }

}
>>>>>>> refs/remotes/origin/develop
