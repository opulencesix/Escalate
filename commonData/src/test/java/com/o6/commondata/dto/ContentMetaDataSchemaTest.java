package com.o6.commondata.dto;

import java.util.Arrays;

import org.junit.Test;

import com.o6.dto.ContentMetadataSchema;

public class ContentMetaDataSchemaTest {
  
  @Test
  public void testStatic() {
    ContentMetadataSchema.getProfileSchemaMap(Arrays.asList("open"));
    ContentMetadataSchema.getDefaultUserProfiles();
  }

}
