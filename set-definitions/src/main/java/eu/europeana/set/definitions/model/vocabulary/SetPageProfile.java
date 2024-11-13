package eu.europeana.set.definitions.model.vocabulary;

import java.util.List;

public enum SetPageProfile implements UserSetProfile{
  META(ProfileConstants.VALUE_PARAM_META,  List.of(ProfileConstants.VALUE_PARAM_MINIMAL), ProfileConstants.VALUE_LD_MINIMAL,
      ProfileConstants.VALUE_PREFER_MINIMAL),
  ITEMS(ProfileConstants.VALUE_PARAM_ITEMS, List.of(ProfileConstants.VALUE_PARAM_STANDARD),  ProfileConstants.VALUE_LD_CONTAINEDIRIS,
      ProfileConstants.VALUE_PREFER_CONTAINEDIRIS),
  ITEMS_META(ProfileConstants.VALUE_PARAM_ITEMS_META,List.of(ProfileConstants.VALUE_PARAM_ITEMS_DEFAULT), 
      ProfileConstants.VALUE_LD_ITEM_DESCRIPTIONS,
      ProfileConstants.VALUE_PREFER_ITEM_DESCRIPTIONS),
  FACETS(ProfileConstants.VALUE_PARAM_FACETS, null, null, null);
  
  List<String> aliases;
  String profileParamValue;
  String ldPreference;
  String preferenceApplied;
  
  SetPageProfile(String profileParamValue, List<String> aliases,  String ldPreference, String preferenceApplied) {
    this.aliases = aliases;
    this.profileParamValue = profileParamValue;
    this.preferenceApplied = preferenceApplied;
  }

  @Override
  public List<String> getAliases() {
    return aliases;
  }

  public String getProfileParamValue() {
    return profileParamValue;
  }

  public String getPreferenceApplied() {
    return preferenceApplied;
  }
  
  public static SetPageProfile getByLdProfile(String ldProfile) {
    for(SetPageProfile profile : SetPageProfile.values()) {
      if(profile.getProfileParamValue() != null && profile.getProfileParamValue().equals(ldProfile)) {
        return profile;
      }
    }
    return null;
  }

  public String getLdPreference() {
    return ldPreference;
  }
  

}
