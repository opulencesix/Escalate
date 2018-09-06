package com.o6.dao;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document
public class ContentModuleMetadata {

  public static final String DEFAULT_MODULE = "open";
  public enum ModuleStatus { ACTIVE, INACTIVE };
  public static final int OPEN_FOR_ALL_MODULE_INDEX = 0;
  
  @Id
  private String id;
  
  private String title;
  private String description;
  private double inrPriceRupees;
  private ModuleStatus status;
  
  public ContentModuleMetadata() {}
  
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public String getTitle() {
    return title;
  }
  public void setTitle(String title) {
    this.title = title;
  }
  public String getDescription() {
    return description;
  }
  public void setDescription(String description) {
    this.description = description;
  }
  public double getInrPriceRupees() {
    return inrPriceRupees;
  }
  public void setInrPriceRupees(double inrPrice) {
    this.inrPriceRupees = inrPrice;
  }

  public ModuleStatus getStatus() {
    return status;
  }

  public void setStatus(ModuleStatus status) {
    this.status = status;
  }
}
