package com.o6.dto;

/*
 * Content information for rendering purposes
 */
public class ContentRenderInfo {

  private String contentId;
  private String title;
  private String language;
  private String authors;
  private String mediaType;
  private String externalId;
  private String textUrl;
  private String description;
  private boolean isFavorite = false;

  public ContentRenderInfo() {}

  public String getContentId() {
    return contentId;
  }

  public void setContentId(String contentId) {
    this.contentId = contentId;
  }

  public String getTitle() {
    return title;
  }

  public void setTitle(String title) {
    this.title = title;
  }

  public String getLanguage() {
    return language;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public String getAuthors() {
    return authors;
  }

  public void setAuthors(String authors) {
    this.authors = authors;
  }

  public String getMediaType() {
    return mediaType;
  }

  public void setMediaType(String mediaType) {
    this.mediaType = mediaType;
  }

  public String getExternalId() {
    return externalId;
  }

  public void setExternalId(String externalId) {
    this.externalId = externalId;
  }

  public String getTextUrl() {
    return textUrl;
  }

  public void setTextUrl(String textURL) {
    this.textUrl = textURL;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public boolean isFavorite() {
    return isFavorite;
  }

  public void setFavorite(boolean isFavorite) {
    this.isFavorite = isFavorite;
  }

  @Override
  public String toString() {
    return String
        .format(
            "Content Profile [id=%s, title=%s, authors=%s, language=%s, mediaType='%s', externalId='%s', description='%s']",
            contentId, title, authors, language, mediaType, externalId, description);
  }

}
