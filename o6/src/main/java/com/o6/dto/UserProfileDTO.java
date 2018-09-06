package com.o6.dto;

import java.util.ArrayList;
import java.util.List;

/*
 * User profiles, and currently selected profile.
 * 
 */
public class UserProfileDTO {
  List<ProfileTemplate> allProfiles = new ArrayList<ProfileTemplate>();
  String currentProfileName;
  
  public UserProfileDTO() {}

  public List<ProfileTemplate> getAllProfiles() {
    return allProfiles;
  }

  public void setAllProfiles(List<ProfileTemplate> allProfiles) {
    this.allProfiles = allProfiles;
  }

  public String getCurrentProfileName() {
    return currentProfileName;
  }

  public void setCurrentProfileName(String currentProfileName) {
    this.currentProfileName = currentProfileName;
  }
  
  

}
