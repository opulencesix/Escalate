package com.o6.security;

import java.util.Date;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.security.core.userdetails.UserDetails;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.o6.dao.ContentModuleMetadata;

@SuppressWarnings("serial")
public class User implements UserDetails {

  public User() {
    unlockedContentModules.add(ContentModuleMetadata.DEFAULT_MODULE);
  }

  public User(String username) {
    this();
    this.username = username;
  }

  public User(String username, Date expires) {
    this();
    this.username = username;
    this.expires = expires.getTime();
  }

  @Id
  private String id;

  private String username;

  private String password;

  @Transient
  private long expires;

  private boolean accountExpired;

  private boolean accountLocked;

  private boolean credentialsExpired;

  private boolean accountEnabled;
  
  private String emailVerifyKey;
  
  private boolean emailVerified;

  private Set<String> unlockedContentModules = new HashSet<String>();

  @Transient
  private String newPassword;

  private Set<UserAuthority> authorities = new HashSet<UserAuthority>();

  private String email;

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  @Override
  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  @Override
  @JsonIgnore
  public String getPassword() {
    return password;
  }

  @JsonProperty
  public void setPassword(String password) {
    this.password = password;
  }

  @JsonIgnore
  public String getNewPassword() {
    return newPassword;
  }

  @JsonProperty
  public void setNewPassword(String newPassword) {
    this.newPassword = newPassword;
  }

  @Override
  @JsonIgnore
  public Set<UserAuthority> getAuthorities() {
    return authorities;
  }

  // Use Roles as external API
  public Set<UserRole> getRoles() {
    Set<UserRole> roles = EnumSet.noneOf(UserRole.class);
    if (authorities != null) {
      for (UserAuthority authority : authorities) {
        roles.add(UserRole.valueOf(authority));
      }
    }
    return roles;
  }

  public void setRoles(Set<UserRole> roles) {
    for (UserRole role : roles) {
      grantRole(role);
    }
  }

  public void grantRole(UserRole role) {
    if (authorities == null) {
      authorities = new HashSet<UserAuthority>();
    }
    authorities.add(role.asAuthorityFor(this));
  }

  public void revokeRole(UserRole role) {
    if (authorities != null) {
      authorities.remove(role.asAuthorityFor(this));
    }
  }

  public boolean hasRole(UserRole role) {
    return authorities.contains(role.asAuthorityFor(this));
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonExpired() {
    return !accountExpired;
  }

  @Override
  @JsonIgnore
  public boolean isAccountNonLocked() {
    return !accountLocked;
  }

  @Override
  @JsonIgnore
  public boolean isCredentialsNonExpired() {
    return !credentialsExpired;
  }

  @Override
  @JsonIgnore
  public boolean isEnabled() {
    return !accountEnabled;
  }

  public long getExpires() {
    return expires;
  }

  public void setExpires(long expires) {
    this.expires = expires;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  @JsonIgnore
  public String getEmailVerifyKey() {
    return emailVerifyKey;
  }

  public void setEmailVerifyKey(String emailVerifyKey) {
    this.emailVerifyKey = emailVerifyKey;
  }

  @JsonIgnore
  public boolean isEmailVerified() {
    return emailVerified;
  }

  public void setEmailVerified(boolean emailVerified) {
    this.emailVerified = emailVerified;
  }

  public Set<String> getUnlockedContentModules() {
    return unlockedContentModules;
  }

  public void setUnlockedContentModules(Set<String> unlockedContentModules) {
    this.unlockedContentModules = unlockedContentModules;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName() + ": " + getUsername();
  }
}
