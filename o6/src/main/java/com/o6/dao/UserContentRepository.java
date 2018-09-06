package com.o6.dao;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserContentRepository extends MongoRepository<UserContent, String> {
}