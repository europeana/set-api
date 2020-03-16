package eu.europeana.set.definitions.config;

import java.util.Properties;

public class UserSetConfigurationImpl implements UserSetConfiguration{

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
	
	public String getValidationApi() {
		return getSetProperties().getProperty(VALIDATION_API);
	}

	public String getValidationAdminApiKey() {
		return getSetProperties().getProperty(VALIDATION_ADMIN_API_KEY);
	}

	public String getValidationAdminSecretKey() {
		return getSetProperties().getProperty(VALIDATION_ADMIN_SECRET_KEY);
	}

	public int getMaxPageSize(String profile) {
		String key = PREFIX_MAX_PAGE_SIZE + profile;
		return Integer.parseInt(getSetProperties().getProperty(key));
	}

	@Override
	public long getApiKeyCachingTime() {
		return Long.parseLong(getSetProperties().getProperty(API_KEY_CACHING_TIME));
	}
	
	@Override
	public String getValidationString() {
		return getSetProperties().getProperty(VALIDATION_STRING);
	}
	
	@Override
	public boolean isProductionEnvironment() {
		return VALUE_ENVIRONMENT_PRODUCTION.equals(getEnvironment());
	}
	
    public String getJwtTokenSignatureKey() {
    	return getSetProperties().getProperty(KEY_APIKEY_JWTTOKEN_SIGNATUREKEY);
    }

    @Override
    public String getAuthorizationApiName() {
    	return getSetProperties().getProperty(AUTHORIZATION_API_NAME);
    }
	
}
