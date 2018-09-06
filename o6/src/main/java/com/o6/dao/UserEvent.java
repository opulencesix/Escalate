package com.o6.dao;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.springframework.data.annotation.Id;

/*
 * Stores all content information pertaining to user. Corresponds to backend table
 */
public class UserEvent {
  
  static SimpleDateFormat dateFormatGmt = new SimpleDateFormat("yyyy-MMM-dd HH:mm:ss");
  static {
    dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
  }

  @Id
  private String id;

  private String userId;
  private String eventName;
  private String eventParams;
  private Date date = new Date();

  public UserEvent() {}

  public UserEvent(String userId, String eventName, String eventParams) {
    this.userId = userId;
    this.eventName = eventName;
    this.eventParams = eventParams;
  }

  @Override
  public String toString() {
    return String.format("Customer[userId=%s, event=%s, params=%s]", userId, eventName, eventParams);
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public String getEventName() {
    return eventName;
  }

  public void setEventName(String eventName) {
    this.eventName = eventName;
  }

  public String getEventParams() {
    return eventParams;
  }

  public void setEventParams(String eventParams) {
    this.eventParams = eventParams;
  }

  public Date getDate() {
    return date;
  }

  public void setDate(Date date) {
    this.date = date;
  }


}
