package eu.europeana.set.definitions.model.vocabulary;

import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import eu.europeana.set.definitions.exception.UserSetProfileValidationException;

public class SetProfileHelper {

  public static boolean hasAlias(UserSetProfile profile, String urlParamValue) {
    if(profile.getAliases() == null) {
      return false;
    }
    return profile.getAliases().contains(urlParamValue);
  }

  SetResourceProfile getSetResourceProfile(String urlParamValue) {
    for (SetResourceProfile profile : SetResourceProfile.values()) {
      if (hasNameOrAlias(urlParamValue, profile)) {
        return profile;
      }
    }
    return null;
  }

  private boolean hasNameOrAlias(String urlParamValue, UserSetProfile profile) {
    return profile.getProfileParamValue().equals(urlParamValue) || hasAlias(profile, urlParamValue);
  }

  /**
   * Verifies the list of profile names and returns the identified profiles
   * @param profiles the list of profile names
   * @param setPageProfiles identified in the input
   * @throws UserSetProfileValidationException in case that the input array contains invalid profiles
   */
  public List<SetPageProfile> parseProfiles(List<String> profiles)
      throws UserSetProfileValidationException {
    
    final int defaultProfilesSize = 3;
    if(profiles == null || profiles.isEmpty()) {
      return new ArrayList<>(defaultProfilesSize);
    }
    
    List<SetPageProfile> setPageProfiles = new ArrayList<>(defaultProfilesSize);
    for (String profile : profiles) {
      if(isTechnicalProfile(profile)) {
        continue;
      }
      SetPageProfile pageProfile = getByProfileParamOrAlias(profile);
      if(pageProfile == null) {
        throw new UserSetProfileValidationException("Invalid profile requsted through request parameter: " + profile);
      }
      setPageProfiles.add(pageProfile);
    }
    return setPageProfiles;
  }
  
  /**
   * Extracts the list of SetPageProfiles from the preferHeader if present, otherwise from the profile request param
   * @param profileStr the coma or spase separated list of profiles
   * @param preferHeader the value of the prefer header
   * @return the list of identified SetPageProfiles
   * @throws UserSetProfileValidationException in case of incorrect profiles
   */
  public List<SetPageProfile> getSetPageProfiles(String profileStr, String preferHeader) throws UserSetProfileValidationException {
    if (StringUtils.isEmpty(preferHeader) && StringUtils.isEmpty(profileStr)) {
      //quick return if empty
      return new ArrayList<>();
    }
    
    List<SetPageProfile> setPageProfiles;
    if(StringUtils.isNotEmpty(preferHeader)) {
      setPageProfiles = parsePreferHeader(preferHeader);
    }else {
      setPageProfiles = parseProfileParam(profileStr);   
    }

    return setPageProfiles;
  }

  private List<SetPageProfile> parsePreferHeader(String preferHeader)
      throws UserSetProfileValidationException{
    
    if(StringUtils.isEmpty(preferHeader)) {
      return new ArrayList<>();
    }
    
    // identify profile by prefer header
    // retrieve profile if provided within the "If-Match" HTTP
    String ldProfile = extractProfileValueFromHeader(preferHeader);
    if(ldProfile == null) {
      throw new UserSetProfileValidationException("Cannot extract profile specification from prefer header: " + preferHeader);
    }
      
    SetPageProfile profile = SetPageProfile.getByLdPreference(ldProfile);  
    if(profile == null) {
      throw new UserSetProfileValidationException("Invalid profile requested through prefer header: " + ldProfile); 
    }
    
    return List.of(profile); 
  }
  
  /**
   * This method parses prefer header in keys and values
   *
   * @param preferHeader
   * @return map of prefer header keys and values
   */
  String extractProfileValueFromHeader(String preferHeader) {
      String[] headerParts = null;
      String[] contentParts = null;
      int keyPos = 0;
      int valuePos = 1;

      headerParts = preferHeader.split(";");
      for (String headerPart : headerParts) {
          contentParts = headerPart.split("=");
          if(contentParts.length == 2 && "include".equals(contentParts[keyPos])) {
            //remove quotes  
            return contentParts[valuePos].replace("\"", "");
          }
      }
      return null;
  }

  private List<SetPageProfile> parseProfileParam(String profileStr) throws UserSetProfileValidationException {
    // multiple profiles can be present separated by comma or space
    // check each param
    return parseProfiles(List.of(toStringArray(profileStr)));
  }

  private boolean isTechnicalProfile(String profile) {
    return ProfileConstants.VALUE_PARAM_DEBUG.equalsIgnoreCase(profile);
  }

  private SetPageProfile getByProfileParamOrAlias(String profile) {
    for (SetPageProfile profileEnum : SetPageProfile.values()) {
      // by name or alias
      if (hasNameOrAlias(profile, profileEnum)) {
        return profileEnum;
      }
    }
    return null;
  }

  private String[] toStringArray(String profileStr) {
    String separator;
    if (profileStr.contains(WebUserSetFields.COMMA)) {
      separator = WebUserSetFields.COMMA;
    } else if (profileStr.contains(WebUserSetFields.SPACE)) {
      separator = WebUserSetFields.SPACE;
    } else {
      // no separator convert to array
      return new String[] {profileStr};
    }
    return StringUtils.split(profileStr, separator);
  }
  
  public SetPageProfile getProfileForPagination(List<SetPageProfile> profiles) {
    if(profiles == null || profiles.isEmpty()) {
      return null;
    }
    //return first profile, the list contains only pagination profiles
    return profiles.get(0);
  }
}
