package com.o6.contentmetadataimporttest;

import java.util.Arrays;
import java.util.List;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.o6.contentmetadataimport.ContentMetaDataRepository;
import com.o6.dao.ContentMetaData;


@RunWith(SpringRunner.class)
@ContextConfiguration(classes = ContentMetaDataImportConfigTest.class)
public class ContentMetadataImportTest {

  @Autowired
  ContentMetaDataRepository mdRepo;

  @Autowired
  MongoTemplate mongoTemplate;

  // Not a real test, but demonstrates the use of criteria queries
  // Will be useful to test only if some records are present in the DB
  @Ignore
  @Test
  public void testLoadedContentMetadata() {

    Query query;
    List<ContentMetaData> mds;
    Criteria crit;

    query = new Query();
    query.addCriteria(Criteria.where("junkCol").is("junkVal"));
    mds = mongoTemplate.find(query, ContentMetaData.class);
    printRecs(query, mds);

    query = new Query();
    crit =
        Criteria.where("contentRenderInfo.language").in(
            Arrays.asList(new String[] {"english", "hindi"}));
    crit =
        crit.and("contentRenderInfo.authors").in(Arrays.asList(new String[] {"Charles Dickens"}));
    crit = crit.and("profileTemplate.profileContentType").in(Arrays.asList(new String[] {"book", "novela"}));
    query.addCriteria(crit);
    mds = mongoTemplate.find(query, ContentMetaData.class);
    printRecs(query, mds);

    /*
     * Full text search query. For it to work, need to create the below index:
     * 
     * db.contentMetaData.createIndex( { "$**": "text" }, { language_override: "junkColName" } ); 
     * 
     * and then fire the query: db.contentMetaData.find({$text: { $search: "dickens"}});
     */
    
    // For 'AND' query of terms, the individual terms need to be specified in "", 
    // else, it is treated as an 'OR' query
    TextCriteria criteria = TextCriteria.forDefaultLanguage().matching("\"favorite\" \"dickens\"");
        //.matchingAny("jacob", "सिंडरेला");

    query = TextQuery.queryText(criteria)
        .sortByScore()
        .with(new PageRequest(0, 20));

    mds = mongoTemplate.find(query, ContentMetaData.class);
    printRecs(query, mds);

    System.out.println("Bye!");

  }

  private static void printRecs(Query qry, List<ContentMetaData> mds) {
    System.out.println("*************************");
    System.out.println("Query: " + qry);
    if (mds.size() == 0) {
      System.out.println("No records found");
    }
    for (ContentMetaData cmd : mds) {
      System.out.println(cmd);
    }
    System.out.println("==========================");
  }
}
