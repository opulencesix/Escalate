package com.o6.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.o6.dao.ContentModuleMetadata;
import com.o6.dao.ContentModuleMetadataRepository;
import com.o6.dao.UserProfiles;
import com.o6.dao.UserProfilesRepository;
import com.o6.dto.ContentMetadataSchema;
import com.o6.dto.ProfileTemplate;
import com.o6.dto.UserProfileDTO;
import com.o6.service.UserProfilesService;

@Service
public class UserProfilesServiceImpl implements UserProfilesService {

  Logger logger = LoggerFactory.getLogger(UserProfilesServiceImpl.class);

  @Autowired
  private UserProfilesRepository repository;

  @Autowired
  ContentModuleMetadataRepository contentModuleMDRepos;


  @Override
  public UserProfileDTO getUserProfiles(String userId, Set<String> unlockedModuleIds) {
    UserProfileDTO retVal = new UserProfileDTO();
    retVal.getAllProfiles().addAll(ContentMetadataSchema.getDefaultUserProfiles());

    // Add contentModule-only profiles
    retVal.getAllProfiles().addAll(profileTemplatesForModuleIds(unlockedModuleIds));

    UserProfiles profiles = repository.findOne(userId);
    if (profiles != null) {
      retVal.getAllProfiles().addAll(profiles.getProfiles().values());
      retVal.setCurrentProfileName(profiles.getCurrentProfileName());
    }
    if (StringUtils.isEmpty(retVal.getCurrentProfileName()) && retVal.getAllProfiles().size() > 0) {
      retVal.setCurrentProfileName(retVal.getAllProfiles().get(0).getName());
    }

    return retVal;
  }

  /*
   * Profiles that have only one filter criteria, which is, containing module.
   */
  private List<ProfileTemplate> profileTemplatesForModuleIds(Set<String> moduleIds) {
    List<ProfileTemplate> moduleOnlyProfiles = new ArrayList<ProfileTemplate>();
    if(moduleIds == null) {
      return moduleOnlyProfiles;
    }
    
    for (String moduleId : moduleIds) {
      ContentModuleMetadata cmmd = contentModuleMDRepos.findOne(moduleId);    
      if(!moduleId.equals(ContentModuleMetadata.DEFAULT_MODULE) &&
          cmmd.getStatus().equals(ContentModuleMetadata.ModuleStatus.ACTIVE)) {
        ProfileTemplate tmpl = new ProfileTemplate();
        tmpl.setName(ContentMetadataSchema.O6_MOD_PFX + WordUtils.capitalize(cmmd.getId()));
        tmpl.setDescription("Content comprising module: " + cmmd.getTitle());
        tmpl.getProfileContainingModules().add(moduleId);
        moduleOnlyProfiles.add(tmpl);
      }
    }
    return moduleOnlyProfiles;

  }

  @Override
  public void setUserProfile(String userId, ProfileTemplate profile) {

    UserProfiles dbUserProfiles = repository.findOne(userId);

    if (dbUserProfiles == null) {
      dbUserProfiles = new UserProfiles(userId);
    }

    Map<String, ProfileTemplate> profiles = dbUserProfiles.getProfiles();
    profiles.put((String) profile.getName(), profile);

    repository.save(dbUserProfiles);

  }

  @Override
  public void setCurrentProfile(String userId, String profileName) {
    UserProfiles userProfile = repository.findOne(userId);
    if (userProfile != null) {
      userProfile.setCurrentProfileName(profileName);
      repository.save(userProfile);
    } else {
      logger.error("Trying to set profile without entry for user " + userId);
    }

  }

  @Override
  public void deleteAndSetCurrent(String userId, String profileNameToDelete, String newCurrentName) {
    UserProfiles userProfile = repository.findOne(userId);
    if (userProfile != null) {
      userProfile.getProfiles().remove(profileNameToDelete);
      userProfile.setCurrentProfileName(newCurrentName);
      repository.save(userProfile);
    } else {
      logger.error("Trying to delete profile without entry for user " + userId);
    }

  }


}
