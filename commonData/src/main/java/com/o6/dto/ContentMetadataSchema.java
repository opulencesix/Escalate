package com.o6.dto;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.LinkedHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.o6.util.SimpleUtils;

/*
 * This class represents the Content Metadata Schema. Field names, default values, as well as
 * default profiles. Everyone else should use this class for verifying attribute names and values.
 * 
 * Also used by importer while importing new data. Also returns the Profile schema as desired by the
 * UI, for displaying forms.
 */
public class ContentMetadataSchema {
  // Value separator regex.
  public static final String VAL_SEP = " *, *";
  public static final String VAL_SEP_RE = " *" + VAL_SEP + ", *";

  public static final String SUPPORTED_MEDIA_TYPES_STR = "youtubevideo,youtubeplaylist";

  public static final String ALL = "all";
  public static final String O6_MOD_PFX = "O6-Mod-";

  // Profile attribute constants
  public static final String PROFILE_ATTRNAME_LANGUAGE = "profileLanguage";
  public static final String PROFILE_ATTRNAME_CONTAINING_MODULES = "profileContainingModules";

  public static final String PROFILE_ATTRNAME_AUDVID = "profileAudioVideoType";
  public static final String PROFILE_ATTRNAME_AGE_GRP = "profileAgeGroup";
  public static final String PROFILE_ATTRNAME_CONTENT_TYPE = "profileContentType";
  public static final String PROFILE_ATTRNAME_CULTURE = "profileCulture";
  public static final String PROFILE_ATTRNAME_DIFFICULTY = "profileDifficultyLevel";
  public static final String PROFILE_ATTRNAME_TOPICS = "profileTopics";

  private static Logger logger = LoggerFactory.getLogger(ContentMetadataSchema.class);

  // These are statics for now, but later, we could provide them through configuration.
  private static Map<String, Object> profileSchemaMap;
  private static Map<String, Object> containingModulesNode;
  private static List<ProfileTemplate> defaultProfiles;
  private static Set<String> defaultProfileNames = new HashSet<String>();

  static {
    try {
      initSchemaMap();
      initDefaultUserProfiles();
    } catch (IOException | URISyntaxException e) {
      logger.error("Could not initialize class", e);
      throw new RuntimeException(e);
    }
    logger.info("Initialized content metadata schema");
  }

  /*
   * Return the profile schema map, mostly for display of UI form.
   */
  @SuppressWarnings("unchecked")
  public static Map<String, Object> getProfileSchemaMap(List<String> containingModules) {
    Map<String, Object> retVal = new LinkedHashMap<String, Object>(profileSchemaMap);

    // Remove hidden/disabled schema's
    Iterator<Entry<String, Object>> iter = retVal.entrySet().iterator();
    while (iter.hasNext()) {
      Entry<String, Object> entry = iter.next();

      Object isHidden = ((Map<String, Object>) entry.getValue()).get("hiddenField");
      if (isHidden != null && (Boolean) isHidden) {
        iter.remove();
      }
    }


    // Add appropriate content modules
    Map<String, Object> tempContainingMods =
        new LinkedHashMap<String, Object>(containingModulesNode);
    ((Map<String, List<String>>) tempContainingMods.get("modifiers")).put("oneOrMore",
        containingModules);
    retVal.put(PROFILE_ATTRNAME_CONTAINING_MODULES, tempContainingMods);
    return retVal;
  }

  public static List<String> getSupportedAttrVals(String attrName) {
    return getSupportedAttrVals(profileSchemaMap, attrName);
  }

  /*
   * Return the supported values for a particular profile attribute
   */
  private static List<String> getSupportedAttrVals(Map<String, Object> schemaMap, String attrName) {
    return getSupportedAttrValsForProfileAttr(schemaMap.get(attrName));
  }

  @SuppressWarnings("unchecked")
  private static List<String> getSupportedAttrValsForProfileAttr(Object rawSchema) {
    Map<String, Object> profileAttrSchema = (Map<String, Object>) rawSchema;
    Map<String, Object> modifiers = (Map<String, Object>) profileAttrSchema.get("modifiers");
    if (modifiers != null) {
      return (List<String>) modifiers.get("oneOrMore");
    }

    throw new RuntimeException("Illegal call. Profile attribute " + profileAttrSchema.get("label")
        + " has no modifiers");

  }

  /*
   * Default user profiles
   */
  public static List<ProfileTemplate> getDefaultUserProfiles() {
    return Collections.unmodifiableList(defaultProfiles);
  }

  /*
   * Default user profile names
   */
  public static Set<String> getDefaultUserProfileNames() {
    return Collections.unmodifiableSet(defaultProfileNames);
  }

  /*
   * Initialize the default profiles with in-built values.
   */
  private static void initDefaultUserProfiles() throws IOException, URISyntaxException {
    initDefaultUserProfilesFromJson(SimpleUtils.getStringFromResource(ContentMetadataSchema.class,
        "defaultUserProfile.json"));
  }

  /*
   * Overwrite default user profiles
   */
  public static void overrideDefaultUserProfiles(String fileName) throws JsonParseException,
      JsonMappingException, IOException {
    initDefaultUserProfilesFromJson(SimpleUtils.getStringFromFile(fileName));
  }

  /*
   * Initialize the default profiles with provided values.
   */
  private static void initDefaultUserProfilesFromJson(String jsonStr) throws JsonParseException,
      JsonMappingException, IOException {
    defaultProfiles =
        new ObjectMapper().readValue(jsonStr, new TypeReference<List<ProfileTemplate>>() {});
    defaultProfileNames.clear();

    for (ProfileTemplate tmpl : defaultProfiles) {
      defaultProfileNames.add(tmpl.getName());
    }

    if (defaultProfileNames.size() != defaultProfiles.size()) {
      throw new RuntimeException("Duplicates in default user profile values: " + jsonStr);
    }
  }

  /*
   * Map hardcoded json to a nested map.
   */
  private static void initSchemaMap() throws IOException, URISyntaxException {
    setSchemaVars(buildSchemaMapFromJson(SimpleUtils.getStringFromResource(
        ContentMetadataSchema.class, "defaultContentMetadataSchema.json")));
  }

  /*
   * Override new schema. Generally the label name is changed, or defaults are modified
   */
  public static void overrideProfileSchemaMap(String fileName) throws IOException {
    setSchemaVars(buildSchemaMapFromJson(SimpleUtils.getStringFromFile(fileName)));
  }

  @SuppressWarnings("unchecked")
  private static void setSchemaVars(Map<String, Object> schema) {
    containingModulesNode =
        new LinkedHashMap<String, Object>(
            (Map<String, Object>) schema.get(PROFILE_ATTRNAME_CONTAINING_MODULES));
    schema.remove(PROFILE_ATTRNAME_CONTAINING_MODULES);
    profileSchemaMap = schema;
  }

  /*
   * Map a json to a nested map.
   */
  private static Map<String, Object> buildSchemaMapFromJson(String jsonStr) {
    Map<String, Object> tempProfileSchemaMap;
    try {

      ObjectMapper mapper = new ObjectMapper();
      String json = jsonStr.trim().replaceAll("\\s+", " ");

      // convert JSON string to Map
      tempProfileSchemaMap = mapper.readValue(json, new TypeReference<Map<String, Object>>() {});

    } catch (Exception e) {
      logger.error("Json serialization error: ", e);
      throw (new RuntimeException(e));
    }

    Iterator<Entry<String, Object>> iter = tempProfileSchemaMap.entrySet().iterator();

    while (iter.hasNext()) {
      Entry<String, Object> entry = iter.next();

      try {
        // Field has to be in the ProfileTemplate
        ProfileTemplate.class.getDeclaredField(entry.getKey());

        if (attributeNeedsModifiers(entry.getKey())) {
          checkJsonArrayVals(getSupportedAttrValsForProfileAttr(entry.getValue()));
        }

      } catch (Exception e) {
        logger.error("Field " + entry.getKey() + " definition has errors");
        throw (new RuntimeException(e));

      }
    }

    return tempProfileSchemaMap;
  }

  private static Boolean attributeNeedsModifiers(String attribute) {
    final Set<String> attribsWithoutModifiers =
        new HashSet<String>(Arrays.asList("name", "description"));
    return !attribsWithoutModifiers.contains(attribute);
  }

  // Check for duplicates and split on separator
  private static void checkJsonArrayVals(List<String> vals) {
    if (new HashSet<String>(vals).size() != vals.size()) {
      throw new RuntimeException("Internal error, duplicates in values: " + vals.toString());
    }
  }

}
