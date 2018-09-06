package com.o6.dto;

/*
 * This class contains content references for saving space.
 * Other needed data is pulled from the DB based on this info.
 */
public class ContentReference {
  
  private String contentId;
  private String language;
  
  public ContentReference() {}
  
  public ContentReference(String contentId, String language) {
    this.contentId = contentId;
    this.language = language;
  }

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((contentId == null) ? 0 : contentId.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ContentReference other = (ContentReference) obj;
    if (contentId == null) {
      if (other.contentId != null)
        return false;
    } else if (!contentId.equals(other.contentId))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return String.format("ContentID: %s, Language %s", contentId, language); 
  }

}
