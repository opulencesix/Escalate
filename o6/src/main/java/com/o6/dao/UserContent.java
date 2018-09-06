package com.o6.dao;

import java.util.HashSet;
import java.util.Set;

import com.o6.dto.ContentReference;

/*
 * Stores all content information pertaining to user. 
 * Does NOT Corresponds to back end table
 */
public class UserContent {

  
 // User ID
  private String id;

  private Set<ContentReference> favoriteContent = new HashSet<ContentReference>();
  private Set<ContentReference> seenContent = new HashSet<ContentReference>();
  private Set<ContentReference> inProgressContent = new HashSet<ContentReference>();

  public UserContent() {}
  
  public UserContent(String userId) {
	    this.id = userId;
  }
    
  @Override
  public String toString() {
    return String.format("Customer[userId=%s, seenContentUnits=%d, favoriteContentUnits=%d]", id,
        seenContent.size(), favoriteContent.size());
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public Set<ContentReference> getSeenContent() {
    return seenContent;
  }

  public void setSeenContentIds(Set<ContentReference> seenContent) {
    this.seenContent = seenContent;
  }

  public Set<ContentReference> getFavoriteContent() {
    return favoriteContent;
  }

  public void setFavoriteContent(Set<ContentReference> favoriteContent) {
    this.favoriteContent = favoriteContent;
  }

  public Set<ContentReference> getInProgressContent() {
    return inProgressContent;
  }

  public void setInProgressContent(Set<ContentReference> inProgressContent) {
    this.inProgressContent = inProgressContent;
  }

}
