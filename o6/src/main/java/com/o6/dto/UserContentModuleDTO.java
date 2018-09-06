package com.o6.dto;

import java.util.ArrayList;
import java.util.List;

import com.o6.dao.ContentModuleMetadata;

/*
 * Content module metadata to be transferred to outside world, for a specific user
 */
public class UserContentModuleDTO {
  private String userId;
  private List<ContentModuleMetadata> subscribedModules = new ArrayList<ContentModuleMetadata>();
  private List<ContentModuleMetadata> unSubscribedModules = new ArrayList<ContentModuleMetadata>();

  public UserContentModuleDTO() {}

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public List<ContentModuleMetadata> getSubscribedModules() {
    return subscribedModules;
  }

  public void setSubscribedModules(List<ContentModuleMetadata> subscribedModules) {
    this.subscribedModules = subscribedModules;
  }

  public List<ContentModuleMetadata> getUnSubscribedModules() {
    return unSubscribedModules;
  }

  public void setUnSubscribedModules(List<ContentModuleMetadata> unSubscribedModules) {
    this.unSubscribedModules = unSubscribedModules;
  }

}
