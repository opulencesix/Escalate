package com.o6.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;

import com.o6.dao.ContentMetaData;
import com.o6.dao.ContentMetaDataRepository;
import com.o6.dao.UserContent;
import com.o6.dao.UserContentRepository;
import com.o6.dao.UserEvent;
import com.o6.dao.UserEventRepository;
import com.o6.dto.ContentMetadataSchema;
import com.o6.dto.ContentReference;
import com.o6.dto.ContentRenderInfo;
import com.o6.dto.ProfileTemplate;
import com.o6.security.User;
import com.o6.service.ContentService;
import com.o6.util.CommonUtils;

@Service
public class ContentServiceImpl implements ContentService {

  private static final int NUM_METADATA_RECORDS_NEEDED = 10;
  private static final int NUM_METADATA_RECORDS_TO_FETCH = NUM_METADATA_RECORDS_NEEDED * 1;

  private static final String PROF_COL_PREFIX = "profileTemplate.";

  private static Logger logger = LoggerFactory.getLogger(ContentServiceImpl.class);

  private static HashSet<String> OPEN_TO_ALL_CONTENT_MODULES = new HashSet<String>(
      Arrays.asList("open"));

  @Autowired
  private UserContentRepository repository;
  @Autowired
  MongoTemplate mongoTemplate;
  @Autowired
  private UserEventRepository eventRepos;
  @Autowired
  private ContentMetaDataRepository repositoryMD;

  /*
   * Main function to identify the most appropriate content to return to the user.
   * 
   * @see com.o6.service.ContentService#getNewRelevantContent(com.o6.security.User,
   * com.o6.dto.ProfileTemplate)
   */
  @Override
  public List<ContentRenderInfo> getNewRelevantContent(User user, ProfileTemplate forProfile) {

    Query queryMD = new Query();
    Set<ContentReference> seenContRef = new HashSet<ContentReference>();
    Set<ContentReference> inProgressContRef = new HashSet<ContentReference>();
    Set<ContentReference> favoriteContRef = new HashSet<ContentReference>();

    // For non Anonymous User.
    if (user != null) {
      UserContent uc = repository.findOne(user.getId());

      // For first time, user content could be null
      if (uc != null) {
        seenContRef = uc.getSeenContent();
        inProgressContRef = uc.getInProgressContent();
        favoriteContRef = uc.getFavoriteContent();
      }
    }

    Set<ContentReference> seenAndInProgressRefs = new HashSet<ContentReference>(seenContRef);
    // TODO. Revisit this. For now, skipped content is marked as seen,
    // therefore inprogress are candidates to be shown again.
    // seenAndInProgressRefs.addAll(inProgressContRef);

    List<String> userLang = forProfile.getProfileLanguage();

    queryMD.limit(NUM_METADATA_RECORDS_TO_FETCH + seenAndInProgressRefs.size());

    // Incrementally prepare the criteria.
    List<Criteria> crit = new ArrayList<Criteria>();
    addAllProfileCriteriaToList(crit, userLang, forProfile);
    addContentModuleCriteriaToList(crit, user);

    // Either and all criteria if there are any filters, else, empty criteria.
    Criteria combinedCriteria =
        crit.size() == 0 ? new Criteria() : (new Criteria()).andOperator(crit
            .toArray(new Criteria[crit.size()]));
    queryMD.addCriteria(combinedCriteria);
    queryMD.with(new Sort(Sort.Direction.ASC, "id"));
    List<ContentMetaData> metaData = mongoTemplate.find(queryMD, ContentMetaData.class);
    Collections.shuffle(metaData);


    // Filter out SEEN Content and InProgress Content, returns list of RenderInfo objects.
    List<ContentMetaData> filteredMD =
        filterByContentRefs(seenAndInProgressRefs, metaData, NUM_METADATA_RECORDS_NEEDED);
    List<ContentRenderInfo> lstRenderInfo = new ArrayList<ContentRenderInfo>();
    for (ContentMetaData cmd : filteredMD) {
      // FilterSeenContent filters the content based on user language preferences too.
      addRenderInfoAndFavoriteForLang(userLang, cmd, lstRenderInfo, favoriteContRef);
    }

    if (logger.isDebugEnabled()) {
      logger.debug("For filter: " + forProfile.toString() + ", Returning Data: "
          + metaData.toString());
    }

    // Earlier, there was a Collections.shuffle(lstRenderInfo). Now, don't shuffle
    Collections.shuffle(lstRenderInfo);
    return lstRenderInfo.size() > NUM_METADATA_RECORDS_NEEDED ? lstRenderInfo.subList(0,
        NUM_METADATA_RECORDS_NEEDED) : lstRenderInfo;
  }

  private void addAllProfileCriteriaToList(List<Criteria> crit, List<String> languages,
      ProfileTemplate forProfile) {
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_LANGUAGE),
        languages);
    addToCriteriaIfNotNull(crit,
        addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CONTAINING_MODULES),
        forProfile.getProfileContainingModules());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_AGE_GRP),
        forProfile.getProfileAgeGroup());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_AUDVID),
        forProfile.getProfileAudioVideoType());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CONTENT_TYPE),
        forProfile.getProfileContentType());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CULTURE),
        forProfile.getProfileCulture());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_DIFFICULTY),
        forProfile.getProfileDifficultyLevel());
    addToCriteriaIfNotNull(crit, addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_TOPICS),
        forProfile.getProfileTopics());
  }

  private void addContentModuleCriteriaToList(List<Criteria> crit, User user) {
    if (user == null || user.getUnlockedContentModules() == null
        || user.getUnlockedContentModules().size() == 0) {
      crit.add(Criteria.where(addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CONTAINING_MODULES))
          .in(OPEN_TO_ALL_CONTENT_MODULES));
    } else {
      crit.add(Criteria.where(addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CONTAINING_MODULES))
          .in(user.getUnlockedContentModules()));
    }
  }

  private String addColPfx(String col) {
    return PROF_COL_PREFIX + col;
  }

  /*
   * Abhijeet Filling the Criteria object for Query. Input: Criteria object, Attribute Name and List
   * of AttributeValues
   */
  public void addToCriteriaIfNotNull(List<Criteria> crit, String key,
      Collection<String> profileValues) {
    if (profileValues == null || profileValues.size() == 0) {
      return;
    }
    crit.add(Criteria.where(key).in(profileValues));

  }

  /*
   * Abhijeet Filters out the ContentReference out of MetaData (Seen and In progress content)
   * 
   * Input: Set <ContentRefence> List <ContentMetaData>
   * 
   * Returns: ArrayList <ContentMetaData>
   */
  public ArrayList<ContentMetaData> filterByContentRefs(Set<ContentReference> contRef,
      List<ContentMetaData> objMetaData, int numRecsNeeded) {
    ArrayList<ContentMetaData> filteredList = new ArrayList<ContentMetaData>();
    ContentReference testRef = new ContentReference("junkId", "junkLang");
    ArrayList<ContentMetaData> extraIfNeeded = new ArrayList<ContentMetaData>();

    for (ContentMetaData cntMD : objMetaData) {
      // Check if the ContentID exists in ContentList (ContentReference)
      testRef.setContentId(cntMD.getId());
      if (contRef.contains(testRef)) {
        extraIfNeeded.add(cntMD);
      } else {
        filteredList.add(cntMD);
      }
    }

    // For the degenerate case that something is needed and we have recs in DB,
    // but they are watched already.
    if (filteredList.size() < numRecsNeeded) {
      int moreNeeded = numRecsNeeded - filteredList.size();
      filteredList.addAll(extraIfNeeded.subList(0, moreNeeded <= extraIfNeeded.size() ? moreNeeded
          : extraIfNeeded.size()));
    }

    return filteredList;
  }

  @Override
  public List<ContentRenderInfo> getConsumedContent(User user) {
    UserContent uc = findUserContent(user);
    if (uc != null) {
      return getRenderInfoFromMetadata(uc.getSeenContent(), uc.getFavoriteContent());
    }

    return Collections.emptyList();
  }

  @Override
  public List<ContentRenderInfo> getInProgressContent(User user) {
    UserContent uc = findUserContent(user);
    if (uc != null) {
      return getRenderInfoFromMetadata(uc.getInProgressContent(), uc.getFavoriteContent());
    }

    return Collections.emptyList();
  }

  @Override
  public List<ContentRenderInfo> getFavoriteContent(User user) {
    UserContent uc = findUserContent(user);
    if (uc != null) {
      return getRenderInfoFromMetadata(uc.getFavoriteContent(), uc.getFavoriteContent());
    }

    return Collections.emptyList();
  }

  @Override
  public List<ContentRenderInfo> getPremiumContent(User user) {
    UserContent uc = findUserContent(user);

    // If There are unlocked content modules, return those.
    if (uc != null && user.getUnlockedContentModules().size() > 0) {
      Set<String> premiumModules = new HashSet<String>(user.getUnlockedContentModules());
      premiumModules.remove(0);
      List<ContentRenderInfo> cntRndInfo = new ArrayList<ContentRenderInfo>();

      // Prepare query
      Query queryMD = new Query();
      queryMD.limit(NUM_METADATA_RECORDS_TO_FETCH);
      queryMD.addCriteria(Criteria.where(
          addColPfx(ContentMetadataSchema.PROFILE_ATTRNAME_CONTAINING_MODULES)).in(premiumModules));
      queryMD.with(new Sort(Sort.Direction.ASC, "id"));
      List<ContentMetaData> metaDataList = mongoTemplate.find(queryMD, ContentMetaData.class);

      // Remember the favorite flag setting.
      ContentReference tempCntRef = new ContentReference("Junk", "Junk");
      for (ContentMetaData cmd : metaDataList) {
        tempCntRef.setContentId(cmd.getId());
        cntRndInfo.add(withFavoriteFlag(cmd.getContentRenderInfo().get(0), uc.getFavoriteContent()
            .contains(tempCntRef)));
      }
      return cntRndInfo;
    }

    return Collections.emptyList();
  }

  private UserContent findUserContent(User user) {
    if (user == null) {
      return null;
    }
    return repository.findOne(user.getId());
  }

  @Override
  public List<ContentRenderInfo> searchContent(String searchStr) {
    List<ContentMetaData> mds;
    List<ContentRenderInfo> searchResults = new ArrayList<ContentRenderInfo>();

    TextCriteria criteria = TextCriteria.forDefaultLanguage().matching(searchStr);

    Query query = TextQuery.queryText(criteria).sortByScore().with(new PageRequest(0, 20));

    mds = mongoTemplate.find(query, ContentMetaData.class);

    // Return the content in the language in the search string,
    // or if not found, the first language.
    if (mds != null) {
      for (ContentMetaData md : mds) {
        boolean neededLanguageFound = false;
        for (ContentRenderInfo renderInfo : md.getContentRenderInfo()) {
          if (searchStr.toLowerCase().contains(renderInfo.getLanguage().toLowerCase())) {
            neededLanguageFound = true;
            searchResults.add(renderInfo);
            break;
          }
        }
        if (!neededLanguageFound) {
          searchResults.add(md.getContentRenderInfo().get(0));
        }
      }
    }

    return searchResults;
  }

  /*
   * Abhijeet Method to extract ContentRenderInfo from MetaData records based on user's language
   */
  private void addRenderInfoAndFavoriteForLang(List<String> langs, ContentMetaData cmd,
      List<ContentRenderInfo> criColl, Set<ContentReference> favorites) {
    ContentReference dummyRef = new ContentReference(cmd.getId(), "dummy");

    // If nothing in languages, return the first available
    if (langs == null || langs.isEmpty()) {
      criColl
          .add(withFavoriteFlag(cmd.getContentRenderInfo().get(0), favorites.contains(dummyRef)));
      return;
    }

    // If language present, then some should match.
    for (ContentRenderInfo cri : cmd.getContentRenderInfo()) {
      if (langs.contains(cri.getLanguage())) {
        criColl.add(withFavoriteFlag(cri, favorites.contains(dummyRef)));
        return;
      }
    }

    // Should not reach here.
    logger.error("Some internal consistency error, trying to get fetch languages " + langs + " in "
        + cmd);

  }

  // Add a favorite flag to content render info and return it.
  private ContentRenderInfo withFavoriteFlag(ContentRenderInfo cri, boolean favoriteFlag) {
    cri.setFavorite(favoriteFlag);
    return cri;
  }


  public List<ContentRenderInfo> getRenderInfoFromMetadata(Set<ContentReference> refs,
      Set<ContentReference> favorites) {
    List<ContentRenderInfo> retVal = new ArrayList<ContentRenderInfo>(refs.size());
    for (ContentReference currRef : refs) {
      ContentMetaData md = repositoryMD.findOne(currRef.getContentId());
      if (md == null) {
        logger.error("For " + currRef + ", there is no metadata");
        continue;
      }
      for (ContentRenderInfo renderInfo : md.getContentRenderInfo()) {
        if (renderInfo.getLanguage().equals(currRef.getLanguage())) {
          retVal.add(withFavoriteFlag(renderInfo, favorites.contains(currRef)));
        }
      }
    }

    return retVal;
  }

  /*
   * Log consumed content event and move over content from upcoming to consumed
   */
  @Override
  public void setConsumedContent(User user, String id, String lang) {
    UserContent uc = repository.findOne(user.getId());
    ContentReference tempRef = new ContentReference(id, lang);
    if (uc == null) {
      logger.warn("No existing user Record in UserContent even if content consumed");
      uc = new UserContent(user.getId());
    }
    uc.getSeenContent().add(tempRef);
    uc.getInProgressContent().remove(tempRef);

    repository.save(uc);
    eventRepos.save(new UserEvent(user.getId(), CommonUtils.CONTENT_SEEN, tempRef.toString()));

  }

  /*
   * For in-progress content, just copy over from upcoming, and record event.
   */
  @Override
  public void setInProgressContent(User user, String id, String lang) {
    UserContent uc = repository.findOne(user.getId());
    ContentReference tempRef = new ContentReference(id, lang);
    if (uc == null) {
      uc = new UserContent(user.getId());
    }
    uc.getInProgressContent().add(tempRef);

    repository.save(uc);

    // For now, avoid saving a lot of in progress events in the DB
    // TODO, later, save these when more precise metering of watched
    // content is needed.
    // eventRepos
    // .save(new UserEvent(user.getId(), CommonUtils.CONTENT_INPROGRESS, tempRef.toString()));


  }

  /*
   * For favorite content, just toggle favorite flag by insert/remove in set.
   */
  @Override
  public void toggleFavoriteContent(User user, String id, String lang) {
    UserContent uc = repository.findOne(user.getId());
    ContentReference tempRef = new ContentReference(id, lang);
    if (uc == null) {
      logger.warn("No existing user Record in UserContent even if content favorite flag changed");
      uc = new UserContent(user.getId());
    }
    if (uc.getFavoriteContent().contains(tempRef)) {
      uc.getFavoriteContent().remove(tempRef);
    } else {
      uc.getFavoriteContent().add(tempRef);
    }

    repository.save(uc);
    eventRepos.save(new UserEvent(user.getId(), CommonUtils.CONTENT_TOGGLE_FAVORITE, tempRef
        .toString()));

  }


}
