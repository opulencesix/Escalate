package com.o6;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {
  
  @Value("${email.smtpHost}")
  public String smtpHost;
  
  @Value("${email.smtpPort}")
  public int smtpPort;  

  @Value("${email.senderName}")
  public String senderName;  

  @Value("${email.senderEmail}")
  public String senderEmail;  

  @Value("${email.senderPassword}")
  public String senderPassword;
  
  @Value("${server.port}")
  public int serverHttpsPort;
  
  @Value("${o6.plain-http-port}")
  public int serverHttpPort;
  
  @Value("${o6.recaptcha-key}")
  public String recaptchaKey;

  @Value("${o6.appUrl}")
  public String appUrl = "https://escalate.opulencesix.com";

  @Value("${o6.payment-gateway-key}")
  public String gatewayKey;
  
  @Value("${o6.payment-gateway-key-sec}")
  public String gatewaySecret;
  
  @Value("${o6.contentMetadataSchemaOverride:}")
  public String contentMetadataSchemaOverride;
  
  @Value("${o6.defaultUserProfileOverride:}")
  public String defaultUserProfileOverride;
  
}
