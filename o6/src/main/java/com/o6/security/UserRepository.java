package com.o6.security;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<User, String> {
  User findByEmail(String email);
  User findByEmailVerifyKey(String verifyKey);
}