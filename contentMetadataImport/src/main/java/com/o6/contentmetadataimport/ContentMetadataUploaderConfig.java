package com.o6.contentmetadataimport;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ContentMetadataUploaderConfig {
  
  @Value("${inputFile}")
  public String inputFile;
  
  @Value("${updateMode:false}")
  public Boolean updateMode;
  
}
