package com.o6.service;

import java.util.Set;

import com.o6.dto.ProfileTemplate;
import com.o6.dto.UserProfileDTO;

public interface UserProfilesService {

  UserProfileDTO getUserProfiles(String userId, Set<String> unlockedModules);

  void setUserProfile(String userId, ProfileTemplate profile);

  void setCurrentProfile(String userId, String profileName);

  void deleteAndSetCurrent(String userId, String profileNameToDelete, String newCurrentName);

}
