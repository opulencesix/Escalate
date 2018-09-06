package com.o6.contentmetadataimport;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.o6.dao.ContentModuleMetadata;

@Repository
public interface ContentModuleInfoRepository extends MongoRepository<ContentModuleMetadata, String> {
}
