package com.o6.dto;

/**
 * Basic user authentication related information passed from outside.
 * @author parikshit
 *
 */
public class BasicUserAuthInfo {
  
  private String userName;
  private String email;
  private String password;
  private String reCaptchaVal;
  private String newPassword;
  
  BasicUserAuthInfo() {}
  
  BasicUserAuthInfo(String email) {
    this.email = email;
  }
  
  BasicUserAuthInfo(String userName, String email, String password, String newPassword) {
    this.email = email;
    this.password = password;
    this.newPassword = newPassword;
  }
  
  public String getUserName() {
    return userName;
  }

  public void setUserName(String name) {
    this.userName = name;
  }

  public String getEmail() {
    return email;
  }
  
  public void setEmail(String email) {
    this.email = email;
  }
  
  public String getPassword() {
    return password;
  }
  
  public void setPassword(String password) {
    this.password = password;
  }
  
  public String getReCaptchaVal() {
    return reCaptchaVal;
  }

  public void setReCaptchaVal(String reCaptchaVal) {
    this.reCaptchaVal = reCaptchaVal;
  }

  public String getNewPassword() {
    return newPassword;
  }
  
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }
  
  

}
