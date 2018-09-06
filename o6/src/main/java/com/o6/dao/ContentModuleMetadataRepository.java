package com.o6.dao;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.o6.dao.ContentModuleMetadata;

@Repository
public interface ContentModuleMetadataRepository extends MongoRepository<ContentModuleMetadata, String> {
}
