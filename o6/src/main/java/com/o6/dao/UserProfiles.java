package com.o6.dao;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.annotation.Id;

import com.o6.dto.ProfileTemplate;

/**
 * Stores all profile definitions for a user.
 * 
 * 
 * TODO, later, have a currentProfile name, to maintain the state persistently.
 */
public class UserProfiles {

  @Id
  private String id;

  private String currentProfileName;
  private Map<String, ProfileTemplate> profiles = new HashMap<String, ProfileTemplate>();

  public UserProfiles() {}

  public UserProfiles(String userId) {
    id = userId;
  }

  public UserProfiles(String userId, Map<String, ProfileTemplate> profiles) {
    this.id = userId;
    this.profiles = profiles;
  }

  public String getUserId() {
    return id;
  }

  public void setUserId(String userId) {
    this.id = userId;
  }

  public String getCurrentProfileName() {
    return currentProfileName;
  }

  public void setCurrentProfileName(String currentProfileName) {
    this.currentProfileName = currentProfileName;
  }

  public Map<String, ProfileTemplate> getProfiles() {
    return profiles;
  }

  public void setProfiles(Map<String, ProfileTemplate> profiles) {
    this.profiles = profiles;
  }

}
