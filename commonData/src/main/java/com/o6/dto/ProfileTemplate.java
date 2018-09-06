package com.o6.dto;

import java.util.LinkedList;
import java.util.List;

/*
 * This is a profile template. Could be used for multiple things Storage to the DB for targeted
 * content profile In User profile, for communication of profile information between UI for queries
 * to match content with target profile.
 * 
 * WARNING, the names of the variables are important, since UI also
 * refers to the rest response by those names.
 */
public class ProfileTemplate {

  private String name = "unspecified";
  private String description = "unspecified";
  private List<String> profileContainingModules = new LinkedList<String>();
  private List<String> profileLanguage = new LinkedList<String>();

  private List<String> profileAudioVideoType = new LinkedList<String>();
  private List<String> profileContentType = new LinkedList<String>();
  private List<String> profileCulture = new LinkedList<String>();
  private List<String> profileAgeGroup = new LinkedList<String>();
  private List<String> profileDifficultyLevel = new LinkedList<String>();
  private List<String> profileTopics = new LinkedList<String>();

  public ProfileTemplate() {}

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public List<String> getProfileContainingModules() {
    return profileContainingModules;
  }

  public void setProfileContainingModules(List<String> containingModules) {
    this.profileContainingModules = containingModules;
  }

  public List<String> getProfileLanguage() {
    return profileLanguage;
  }

  public void setProfileLanguage(List<String> profileLanguage) {
    this.profileLanguage = profileLanguage;
  }

  public List<String> getProfileAudioVideoType() {
    return profileAudioVideoType;
  }

  public void setProfileAudioVideoType(List<String> profileAudioVideoType) {
    this.profileAudioVideoType = profileAudioVideoType;
  }

  public List<String> getProfileContentType() {
    return profileContentType;
  }

  public void setProfileContentType(List<String> profileContentType) {
    this.profileContentType = profileContentType;
  }

  public List<String> getProfileCulture() {
    return profileCulture;
  }

  public void setProfileCulture(List<String> profileCulture) {
    this.profileCulture = profileCulture;
  }

  public List<String> getProfileAgeGroup() {
    return profileAgeGroup;
  }

  public void setProfileAgeGroup(List<String> profileAgeGroup) {
    this.profileAgeGroup = profileAgeGroup;
  }

  public List<String> getProfileDifficultyLevel() {
    return profileDifficultyLevel;
  }

  public void setProfileDifficultyLevel(List<String> profileDifficultyLevel) {
    this.profileDifficultyLevel = profileDifficultyLevel;
  }

  public List<String> getProfileTopics() {
    return profileTopics;
  }

  public void setProfileTopics(List<String> profileTopics) {
    this.profileTopics = profileTopics;
  }

  public void setProfileDefaultsIfEmpty() {
    setDefaultAttrValIfEmpty(profileContentType);
    setDefaultAttrValIfEmpty(profileCulture);
    setDefaultAttrValIfEmpty(profileAgeGroup);
    setDefaultAttrValIfEmpty(profileDifficultyLevel);
    setDefaultAttrValIfEmpty(profileTopics);
  }

  private void setDefaultAttrValIfEmpty(List<String> attrVals) {
    if (attrVals.size() == 0) {
      attrVals.add(ContentMetadataSchema.ALL);
    }
  }

  @Override
  public String toString() {
    return String
        .format(
            "Content Meta Data [profileAudioVideoType='%s', profileContentType='%s', profileCulture='%s', profileAgeGroup='%s', profileDiffLevel='%s', profileTopics='%s']",
            profileAudioVideoType, profileContentType, profileCulture, profileAgeGroup,
            profileDifficultyLevel, profileTopics);
  }
}
