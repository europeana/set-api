package eu.europeana.set.definitions.model.vocabulary;

import eu.europeana.set.definitions.exception.UserSetProfileValidationException;

/**
 * This enumeration is intended for Linked Data profiles
 * 
 * @author GrafR
 *
 */
public enum LdProfiles implements ProfileKeyword {

    MINIMAL(ProfileConstants.VALUE_LD_MINIMAL, ProfileConstants.VALUE_PREFER_MINIMAL), STANDARD(ProfileConstants.VALUE_LD_CONTAINEDIRIS, ProfileConstants.VALUE_PREFER_CONTAINEDIRIS),
    ITEMDESCRIPTIONS(ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS, ProfileConstants.VALUE_PREFER_ITEM_DESCRIPTIONS), FACETS(ProfileConstants.VALUE_LD_FACETS, "");


    private String headerValue;
    private String preferHeaderValue;

    LdProfiles(String headerValue, String preferHeaderValue) {
	this.headerValue = headerValue;
	this.preferHeaderValue = preferHeaderValue;
    }

    /**
     * Identifying requested profile by Linked Data value. For user friendliness the
     * the comparison is case insensitive
     * 
     * @param headerValue
     * @return
     * @throws UserSetProfileValidationException
     */
    public static LdProfiles getByHeaderValue(String headerValue) throws UserSetProfileValidationException {

	for (LdProfiles ldType : LdProfiles.values()) {
	    if (headerValue.equals(ldType.getHeaderValue())) {
		return ldType;
	    }
	}
	throw new UserSetProfileValidationException(headerValue);
    }

    /**
     * 
     * @param name
     * @return
     * @throws UserSetProfileValidationException
     */
    public static LdProfiles getByName(String name) throws UserSetProfileValidationException {

	for (LdProfiles ldType : LdProfiles.values()) {
	    if (name.equalsIgnoreCase(ldType.name())) {
		return ldType;
	    }
	}
	throw new UserSetProfileValidationException(name);
    }

    @Override
    public String getHeaderValue() {
	return headerValue;
    }

    @Override
    public String toString() {
	return getHeaderValue();
    }

    public String getPreferHeaderValue() {
	return preferHeaderValue;
    }

}
