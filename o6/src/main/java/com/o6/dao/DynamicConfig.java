package com.o6.dao;

import org.springframework.data.annotation.Id;

/*
 * Minor Dynamic configurations
 */
public class DynamicConfig {
  // Constants for notifications and status
  public static final String NOTIFICATION_CONFIG_KEY = "notifications";
  
  @Id
  private String configKey;
  
  private Object configVal;

  public DynamicConfig() {}

  public DynamicConfig(String key, Object val) {
    this.configKey = key;
    this.configVal = val;
  }

  public String getConfigKey() {
    return configKey;
  }

  public void setConfigKey(String configKey) {
    this.configKey = configKey;
  }

  public Object getConfigVal() {
    return configVal;
  }

  public void setConfigVal(Object configVal) {
    this.configVal = configVal;
  }

  @Override
  public String toString() {
    return String.format("DynamicConfig[key=%s, val=%s]", configKey, configVal);
  }

}
