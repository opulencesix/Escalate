package com.o6.contentmetadataimporttest;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.AbstractMongoConfiguration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.mongodb.MongoClient;
import com.o6.contentmetadataimport.ContentMetaDataRepository;

@Configuration
@EnableMongoRepositories(basePackageClasses = ContentMetaDataRepository.class)
public class ContentMetaDataImportConfigTest extends AbstractMongoConfiguration {


  @Override
  protected String getDatabaseName() {
    return "test";
  }

  @Bean
  public MongoClient mongo() throws Exception {

    MongoClient client = new MongoClient("localhost");
    return client;
  }

  @Bean
  public MongoTemplate mongoTemplate() throws Exception {
    return new MongoTemplate(mongo(), getDatabaseName());
  }

}
