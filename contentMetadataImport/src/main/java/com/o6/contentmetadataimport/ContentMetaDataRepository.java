package com.o6.contentmetadataimport;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.o6.dao.ContentMetaData;

@Repository
public interface ContentMetaDataRepository extends MongoRepository<ContentMetaData, String> {

  List<ContentMetaData> findByTitleKey(String titleKey);

}
