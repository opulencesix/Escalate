package com.o6.contentmetadataimport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.util.StringUtils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import com.o6.dao.ContentMetaData;
import com.o6.dao.ContentModuleMetadata;
import com.o6.dto.ContentRenderInfo;
import com.o6.dto.ContentMetadataSchema;

/*
 * Top level application for content metadata upload. Takes an excel spreadsheet, and updates the
 * ContentMetaData table
 */
@SpringBootApplication
public class ContentMetadataUploaderApplication implements CommandLineRunner {

  // Field separator regex
  private static final String FLD_SEP = ",";
  private static final String FLD_SEP_RE = " *" + FLD_SEP + " *";

  private static final String FIELD_NAME_TITLE_KEY = "titleKey";
  private static final String FIELD_NAME_TITLE = "title";
  private static final String FIELD_NAME_LANGUAGE = "language";
  private static final String FIELD_NAME_AUTHORS = "authors";
  private static final String FIELD_NAME_MEDIA_TYPE = "mediaType";
  private static final String FIELD_NAME_EXTERNAL_ID = "externalId";
  private static final String FIELD_NAME_TEXT_URL = "textUrl";
  private static final String FIELD_NAME_DESCRIPTION = "description";
  private static final String FIELD_NAME_TAGS = "tags";

  private static final Logger logger = LoggerFactory
      .getLogger(ContentMetadataUploaderApplication.class);

  boolean errorFoundWhileImporting = false;

  @Autowired
  ContentMetadataUploaderConfig config;

  @Autowired
  private ContentMetaDataRepository repositoryMD;

  @Autowired
  private ContentModuleInfoRepository repositoryModuleInfo;

  private Set<String> validModuleIds = null;

  public static void main(String[] args) {
    SpringApplication.run(ContentMetadataUploaderApplication.class, args).close();
  }

  @Override
  public void run(String... args) throws Exception {

    validModuleIds = initValidContentModules();

    // Read the Excel file
    FileInputStream fis = null;
    try {
      fis = new FileInputStream(config.inputFile);

      Workbook workbook = new XSSFWorkbook(fis);
      Sheet sheet = workbook.getSheetAt(0);
      Iterator<Row> rowIterator = sheet.iterator();

      // Get the first row for fields
      Row headerRow = rowIterator.next();
      Map<String, Integer> fldNameToColIdx = mapColumnNamesToIndexes(headerRow);

      Map<String, ContentMetaData> titleKeyDeltaContentMDMap =
          new HashMap<String, ContentMetaData>();

      // iterating over each row to build a delta content metadata map per title key.
      while (rowIterator.hasNext()) {

        Row row = rowIterator.next();

        validateFileAndAddMetadataToMap(fldNameToColIdx, row, titleKeyDeltaContentMDMap);
      }

      // Now merge the new content metadata with the old.
      for (Map.Entry<String, ContentMetaData> cmdEntry : titleKeyDeltaContentMDMap.entrySet()) {
        ContentMetaData mergedCMD = getUniqueMetadataByTitle(cmdEntry.getKey());
        if (mergedCMD == null) {
          mergedCMD = cmdEntry.getValue();
          prepareNewRecForDBStorage(mergedCMD);
        } else if (config.updateMode) {
          mergeNewCMDToRecInDB(mergedCMD, cmdEntry.getValue());          
        } else {
          logger.error("Update mode not enabled, and record already existing for: " + cmdEntry.getKey());
          this.errorFoundWhileImporting = true;
        }
        cmdEntry.setValue(mergedCMD);
      }

      // Abort the import if errors were encountered.
      if (this.errorFoundWhileImporting) {
        logger.error("Aborting import due to multiple errors");
        throw new RuntimeException("Cannot continue import process, aborting");
      }

      System.out.println("--------------------------------");
      System.out.println("Printing Content MetaData");
      for (ContentMetaData contentMetaData : titleKeyDeltaContentMDMap.values()) {
        System.out.println(contentMetaData);
      }

      saveContentMetadata(titleKeyDeltaContentMDMap.values());
      // workbook.close();
      fis.close();

    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /*
   * Read content module ids from the DB
   */
  private Set<String> initValidContentModules() {
    return repositoryModuleInfo.findAll().stream().map((moduleInfo) -> {
      return moduleInfo.getId();
    }).collect(Collectors.toSet());
  }

  /*
   * If profile attribute is empty, then, overwrite it with the default (all)
   */
  private static void prepareNewRecForDBStorage(ContentMetaData cmd) {
    setProfileLanguage(cmd);
    cmd.getProfileTemplate().setProfileDefaultsIfEmpty();
  }

  /*
   * Set the list of all languages in the content metadata into the language profile.
   */
  private static void setProfileLanguage(ContentMetaData cmd) {
    // Refill languages first
    List<String> profileLanguage = cmd.getProfileTemplate().getProfileLanguage();
    profileLanguage.clear();
    for (ContentRenderInfo info : cmd.getContentRenderInfo()) {
      profileLanguage.add(info.getLanguage());
    }
  }

  /*
   * Save rows to database
   */
  private void saveContentMetadata(Collection<ContentMetaData> recs) {
    for (ContentMetaData cmd : recs) {
      boolean needToCopyOverIds = (cmd.getId() == null);

      // Saving updates the id in the row. The save also modifies the original object.
      ContentMetaData savedRow = repositoryMD.save(cmd);
      if (needToCopyOverIds) {
        // For new rows, the contentId within the content render info objects is empty
        // a row ID is available only after saving to DB.
        for (ContentRenderInfo renderInfo : savedRow.getContentRenderInfo()) {
          renderInfo.setContentId(savedRow.getId());
        }
        repositoryMD.save(savedRow);
      }
    }

  }

  /*
   * Overwrite new content metadata over old
   */
  private static void mergeNewCMDToRecInDB(ContentMetaData mergedMD, ContentMetaData newMD) {
    if (newMD.getProfileTemplate().getProfileContainingModules().size() > 0) {
      mergedMD.getProfileTemplate().getProfileContainingModules().clear();
      mergedMD.getProfileTemplate().getProfileContainingModules()
          .addAll(newMD.getProfileTemplate().getProfileContainingModules());
    }
    if (StringUtils.hasText(newMD.getTags())) {
      mergedMD.setTags(newMD.getTags());
    }

    // Overwrite the content render info for matching languages.
    int oldRenderInfoLen = mergedMD.getContentRenderInfo().size();
    for (int i = 0; i < newMD.getContentRenderInfo().size(); i++) {
      boolean foundMatching = false;
      ContentRenderInfo newRenderInfo = newMD.getContentRenderInfo().get(i);
      // New info had an empty contentId, since we did not know at that time
      // about that metadata being present in the DB.
      newRenderInfo.setContentId(mergedMD.getId());
      for (int j = 0; j < oldRenderInfoLen; j++) {
        ContentRenderInfo mergedRenderInfo = mergedMD.getContentRenderInfo().get(j);
        if (mergedRenderInfo.getLanguage().equals(newRenderInfo.getLanguage())) {
          mergedMD.getContentRenderInfo().set(j, newRenderInfo);
          foundMatching = true;
        }
      }
      if (!foundMatching) {
        mergedMD.getContentRenderInfo().add(newMD.getContentRenderInfo().get(i));
      }
    }

    // Set the language array
    setProfileLanguage(mergedMD);

    // Now, overwrite profile attributes if not empty
    overwriteProfileAttrIfEmpty(mergedMD.getProfileTemplate().getProfileAgeGroup(), newMD
        .getProfileTemplate().getProfileAgeGroup());
    overwriteProfileAttrIfEmpty(mergedMD.getProfileTemplate().getProfileContentType(), newMD
        .getProfileTemplate().getProfileContentType());
    overwriteProfileAttrIfEmpty(mergedMD.getProfileTemplate().getProfileCulture(), newMD
        .getProfileTemplate().getProfileCulture());
    overwriteProfileAttrIfEmpty(mergedMD.getProfileTemplate().getProfileDifficultyLevel(), newMD
        .getProfileTemplate().getProfileDifficultyLevel());
    overwriteProfileAttrIfEmpty(mergedMD.getProfileTemplate().getProfileTopics(), newMD
        .getProfileTemplate().getProfileTopics());

  }

  /*
   * Overwrite profile array if not previously empty
   */
  private static void overwriteProfileAttrIfEmpty(List<String> mergedProfileAttrs,
      List<String> newProfileAttrs) {
    if (newProfileAttrs.size() > 0) {
      mergedProfileAttrs.clear();
      mergedProfileAttrs.addAll(newProfileAttrs);
    }
  }

  /*
   * Top level row level validation function for new rows to be imported
   */
  private void validateFileAndAddMetadataToMap(Map<String, Integer> fldIdxs, Row row,
      Map<String, ContentMetaData> titleKeyContentMDMap) {

    if (isRowEmpty(row)) {
      return;
    }

    if (!rowSanityChecksPass(fldIdxs, row)) {
      recordError("Sanity check fail", row);
      return;
    }

    String titleKey = getColStrVal(row, fldIdxs.get(FIELD_NAME_TITLE_KEY));
    ContentMetaData oContent = titleKeyContentMDMap.get(titleKey);

    // Not already in map. Create new one.
    if (oContent == null) {
      oContent = new ContentMetaData();
      oContent.setTitleKey(titleKey);
      titleKeyContentMDMap.put(titleKey, oContent);
    }

    if (!rowIntegrityChecksPass(fldIdxs, row, oContent)) {
      recordError("Integrity check fail", row);
      return;
    }

    String newTags = getColStrValLC(row, fldIdxs.get(FIELD_NAME_TAGS));
    if (StringUtils.hasText(newTags)) {
      oContent.setTags(newTags);
    }

    try {
      String containingModulesStr =
          getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_CONTAINING_MODULES));
      if (StringUtils.isEmpty(containingModulesStr)) {
        oContent.getContainingModuleIds().add(ContentModuleMetadata.DEFAULT_MODULE);
      } else {
        oContent.getContainingModuleIds().addAll(Arrays.asList(splitCSV(containingModulesStr)));
        if (!validModuleIds.containsAll(oContent.getContainingModuleIds())) {
          recordError("Containing module should be an id existing in contentModuleInfo table", row);
          return;
        }
      }
    } catch (NumberFormatException ex) {
      recordError("Containing module should be an id existing in contentModuleInfo table", row);
      return;
    }

    oContent.getContentRenderInfo().add(createRenderInfo(row, fldIdxs));
    fillProfileFieldInfo(oContent, row, fldIdxs);

  }

  /*
   * Is row empty?
   */
  private static boolean isRowEmpty(Row row) {
    Iterator<Cell> iter = row.cellIterator();
    while (iter.hasNext()) {
      if (StringUtils.hasText(iter.next().getStringCellValue())) {
        return false;
      }
    }

    return true;
  }

  /*
   * Fill the profile related fields from the row.
   */
  private static void fillProfileFieldInfo(ContentMetaData oContent, Row row,
      Map<String, Integer> fldIdxs) {
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileAgeGroup(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_AGE_GRP)));
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileAudioVideoType(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_AUDVID)));
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileContentType(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_CONTENT_TYPE)));
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileCulture(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_CULTURE)));
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileDifficultyLevel(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_DIFFICULTY)));
    writeListFieldIfEmpty(oContent.getProfileTemplate().getProfileTopics(),
        getColStrVal(row, fldIdxs.get(ContentMetadataSchema.PROFILE_ATTRNAME_TOPICS)));
  }


  /*
   * Overwrite list with new csv if previously empty
   */
  private static void writeListFieldIfEmpty(List<String> oldVal, String newVal) {
    if (oldVal.size() == 0) {
      oldVal.addAll(Arrays.asList(splitCSVLC(newVal)));
    }
  }

  /*
   * Though titleKey is unique, there is no unique index, so, we expect a list with one element
   * only.
   */
  private ContentMetaData getUniqueMetadataByTitle(String titleKey) {
    List<ContentMetaData> recs = repositoryMD.findByTitleKey(titleKey);

    if (recs.size() > 1) {
      String errorMsg =
          "Metadata repository inconsistent, multiple records for titleKey: " + titleKey;
      logger.error(errorMsg);
      throw new RuntimeException(errorMsg);
    }

    return recs.size() == 0 ? null : recs.get(0);
  }

  /*
   * To make the row value fetching more robust, we use column names, and indexes based on those
   * column names.
   */
  private Map<String, Integer> mapColumnNamesToIndexes(Row headerRow) {
    Map<String, Integer> fieldNameToIndexMap = new HashMap<String, Integer>();
    Iterator<Cell> cellIterator = headerRow.cellIterator();

    int colInd = 0;
    while (cellIterator.hasNext()) {
      String valCell = cellIterator.next().getStringCellValue();
      if (StringUtils.isEmpty(valCell)) {
        continue;
      }
      fieldNameToIndexMap.put(valCell.trim(), colInd);
      colInd++;
    }

    return fieldNameToIndexMap;
  }


  /*
   * Record an error object level variable.
   */
  private void recordError(String error, Row row) {
    logger.error("Row number: " + row.getRowNum() + ", " + error);
    errorFoundWhileImporting = true;
  }

  /*
   * Check for correctness for values across multiple rows for the same content.
   */
  private static boolean rowIntegrityChecksPass(Map<String, Integer> fldIdxs, Row row,
      ContentMetaData cmd) {
    ArrayList<Boolean> integrityChecksPassRef = new ArrayList<Boolean>(1);
    integrityChecksPassRef.add(true);

    String newTags = getColStrValLC(row, fldIdxs.get(FIELD_NAME_TAGS));
    if (StringUtils.hasText(cmd.getTags()) && StringUtils.hasText(newTags)
        && !Objects.equals(newTags, cmd.getTags())) {
      integrityChecksPassRef.set(0, false);
    }

    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_AUDVID,
        cmd.getProfileTemplate().getProfileAudioVideoType(), integrityChecksPassRef);
    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_CONTENT_TYPE, cmd.getProfileTemplate()
            .getProfileContentType(), integrityChecksPassRef);
    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_CULTURE, cmd.getProfileTemplate()
            .getProfileCulture(), integrityChecksPassRef);
    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_AGE_GRP, cmd.getProfileTemplate()
            .getProfileAgeGroup(), integrityChecksPassRef);
    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_DIFFICULTY, cmd.getProfileTemplate()
            .getProfileDifficultyLevel(), integrityChecksPassRef);
    checkNonEmptyMismatchingAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_TOPICS,
        cmd.getProfileTemplate().getProfileTopics(), integrityChecksPassRef);

    return integrityChecksPassRef.get(0);
  }

  /*
   * Two rows in the spreadsheet should not define different values for the same profile attribute.
   */
  private static void checkNonEmptyMismatchingAndSetFlag(Row row, Map<String, Integer> fldIdxs,
      String fldName, List<String> oldValues, ArrayList<Boolean> integrityChecksPass) {
    String newCellVal = getColStrVal(row, fldIdxs.get(fldName));

    if (StringUtils.isEmpty(newCellVal)) {
      return;
    }

    // New and old identical, or old is empty and new has value are valid conditions.
    List<String> newValues = Arrays.asList(splitCSV(newCellVal));
    if (oldValues.size() == 0
        || (newValues.size() == oldValues.size() && newValues.containsAll(oldValues))) {
      return;
    }

    // If we reach here, then, the values are different, and there is some error.
    logger.error("Mismatching non-empty values for " + fldName + ". Clash with previous row");
    integrityChecksPass.set(0, false);


  }


  /*
   * Sanity checking of individual cell values.
   * 
   * TODO for now, these are hard-coded. However, we should move these out to some common place such
   * that the UI also displays only the valid options in the user profile
   */
  private static boolean rowSanityChecksPass(Map<String, Integer> fldIdxs, Row row) {

    // Since boolean object is immutable, we embed it in a single element array list.
    List<Boolean> sanityChecksPassRef = new ArrayList<Boolean>(1);
    sanityChecksPassRef.add(true);

    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_TITLE_KEY, sanityChecksPassRef);
    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_TITLE, sanityChecksPassRef);
    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_LANGUAGE, sanityChecksPassRef);

    checkValidValuesAndSetFlag(row, fldIdxs, FIELD_NAME_LANGUAGE,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_LANGUAGE), true,
        sanityChecksPassRef);
    checkValidValuesAndSetFlag(row, fldIdxs, FIELD_NAME_MEDIA_TYPE,
        ContentMetadataSchema.SUPPORTED_MEDIA_TYPES_STR, true, sanityChecksPassRef);
    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_EXTERNAL_ID, sanityChecksPassRef);
    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_TEXT_URL, sanityChecksPassRef);
    checkNonEmptyAndSetFlag(row, fldIdxs, FIELD_NAME_DESCRIPTION, sanityChecksPassRef);

    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_AUDVID,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_AUDVID), sanityChecksPassRef);
    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_CONTENT_TYPE,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_CONTENT_TYPE),
        sanityChecksPassRef);
    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_CULTURE,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_CULTURE), sanityChecksPassRef);
    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_AGE_GRP,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_AGE_GRP), sanityChecksPassRef);
    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs,
        ContentMetadataSchema.PROFILE_ATTRNAME_DIFFICULTY,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_DIFFICULTY), sanityChecksPassRef);
    checkValidValuesOrEmptyAndSetFlag(row, fldIdxs, ContentMetadataSchema.PROFILE_ATTRNAME_TOPICS,
        attrValsFromSchema(ContentMetadataSchema.PROFILE_ATTRNAME_TOPICS), sanityChecksPassRef);

    return sanityChecksPassRef.get(0);
  }


  // Fill in the renderInfo from input row
  private static ContentRenderInfo createRenderInfo(Row row, Map<String, Integer> fieldIndexes) {

    ContentRenderInfo renderInfo = new ContentRenderInfo();
    renderInfo.setTitle(getColStrVal(row, fieldIndexes.get(FIELD_NAME_TITLE)));
    renderInfo.setAuthors(getColStrVal(row, fieldIndexes.get(FIELD_NAME_AUTHORS)));
    renderInfo.setDescription(getColStrVal(row, fieldIndexes.get(FIELD_NAME_DESCRIPTION)));
    renderInfo.setExternalId(getColStrVal(row, fieldIndexes.get(FIELD_NAME_EXTERNAL_ID)));
    renderInfo.setLanguage(getColStrValLC(row, fieldIndexes.get(FIELD_NAME_LANGUAGE)));
    renderInfo.setMediaType(getColStrValLC(row, fieldIndexes.get(FIELD_NAME_MEDIA_TYPE)));
    renderInfo.setTextUrl(getColStrVal(row, fieldIndexes.get(FIELD_NAME_TEXT_URL)));

    return renderInfo;
  }

  private static String attrValsFromSchema(String attrName) {
    return String.join(FLD_SEP, ContentMetadataSchema.getSupportedAttrVals(attrName));
  }

  // Shorthand for checking cell emptiness and setting sanityPass flag
  private static void checkNonEmptyAndSetFlag(Row row, Map<String, Integer> fldIdxs,
      String fieldName, List<Boolean> sanityPassFlag) {
    if (StringUtils.isEmpty(getColStrVal(row, fldIdxs.get(fieldName)))) {
      logger.error(fieldName + " is empty");
      sanityPassFlag.set(0, false);
    }
  }

  // Shorthand for checking cell emptiness and setting sanityPass flag
  private static void checkValidValuesOrEmptyAndSetFlag(Row row, Map<String, Integer> fldIdxs,
      String fieldName, String validValuesCSV, List<Boolean> sanityPassFlagRef) {
    String val = getColStrVal(row, fldIdxs.get(fieldName));

    // Empty is valid
    if (StringUtils.isEmpty(val)) {
      return;
    }

    checkValidValuesAndSetFlag(row, fldIdxs, fieldName, validValuesCSV, false, sanityPassFlagRef);
  }

  // Shorthand for checking cell emptiness and setting sanityPass flag
  private static void checkValidValuesAndSetFlag(Row row, Map<String, Integer> fldIdxs,
      String fieldName, String validValuesCSV, boolean singleValueOnly,
      List<Boolean> sanityPassFlagRef) {
    String val = getColStrVal(row, fldIdxs.get(fieldName));

    // Ignore case
    val = (val == null) ? val : val.toLowerCase();
    validValuesCSV = validValuesCSV.toLowerCase();

    String msgIfError =
        fieldName + " should be one of: " + validValuesCSV
            + (singleValueOnly ? ", and single only" : "") + ", but is: " + val;

    // Empty is invalid. Also, check if multiple values present in single value field.
    if (StringUtils.isEmpty(val) || (singleValueOnly && val.contains(","))) {
      logger.error(msgIfError);
      sanityPassFlagRef.set(0, false);
      return;
    }

    if (!Arrays.asList(splitCSV(validValuesCSV)).containsAll(Arrays.asList(splitCSV(val)))) {
      logger.error(msgIfError);
      sanityPassFlagRef.set(0, false);
    }

  }

  // Split CSV string on commas
  private static String[] splitCSVLC(String val) {
    return (val == null || val.trim().equals("")) ? new String[] {} : val.toLowerCase().split(
        FLD_SEP_RE);
  }

  private static String[] splitCSV(String val) {
    return (val == null || val.trim().equals("")) ? new String[] {} : val.split(FLD_SEP_RE);
  }

  // Shorthand for long phrase to get cell value.
  private static String getColStrValLC(Row row, int index) {
    String val = getColStrVal(row, index);
    return val == null ? null : val.toLowerCase().trim();
  }

  private static String getColStrVal(Row row, int index) {
    String val = row.getCell(index).getStringCellValue();
    return val == null ? null : val.trim();
  }

}
