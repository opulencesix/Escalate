package com.o6.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface DynamicConfigRepository extends MongoRepository<DynamicConfig, String> {
}