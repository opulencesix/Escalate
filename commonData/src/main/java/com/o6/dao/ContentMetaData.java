package com.o6.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import com.o6.dto.ContentRenderInfo;
import com.o6.dto.ProfileTemplate;

import java.util.LinkedList;
import java.util.List;


/*
 * Content metadata. Combines information needed to render content, as well as that needed to match
 * the content metadata to incoming user profile.
 */
@Document
public class ContentMetaData {

  // Fields common across languages, and not related to profile attributes
  @Id
  private String id;
  private String titleKey;
  private String tags;

  // Language specific fields for rendering content
  List<ContentRenderInfo> contentRenderInfo = new LinkedList<ContentRenderInfo>();

  // Fields related to matching profile attributes
  private ProfileTemplate profileTemplate = new ProfileTemplate();

  public ContentMetaData() {}

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitleKey() {
    return this.titleKey;
  }

  public void setTitleKey(String strTitleKey) {
    this.titleKey = strTitleKey;
  }

  public String getTags() {
    return this.tags;
  }

  public void setTags(String strTags) {
    this.tags = strTags;
  }

  public List<ContentRenderInfo> getContentRenderInfo() {
    return contentRenderInfo;
  }

  public void setContentRenderInfo(List<ContentRenderInfo> contentRenderInfo) {
    this.contentRenderInfo = contentRenderInfo;
  }

  public ProfileTemplate getProfileTemplate() {
    return profileTemplate;
  }

  public void setProfileTemplate(ProfileTemplate profileTemplate) {
    this.profileTemplate = profileTemplate;
  }
  
  public List<String> getContainingModuleIds() {
    return profileTemplate.getProfileContainingModules();
  }

  @Override
  public String toString() {
    return String.format(
        "Content Meta Data [id=%s, titleKey='%s', renderInfo=%s, tags='%s', "
            + "profileTemplate='%s']",
        id, titleKey, contentRenderInfo.toString(), tags, profileTemplate);
  }

}
