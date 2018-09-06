package com.o6.controller;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.o6.dao.ContentModuleMetadata;
import com.o6.dto.ContentMetadataSchema;
import com.o6.dto.ProfileTemplate;
import com.o6.dto.UserProfileDTO;
import com.o6.security.UserAuthentication;
import com.o6.service.UserProfilesService;

@RestController
@RequestMapping(value = "/api/profile")
@ControllerAdvice
public class UserProfileController {

  private static final Logger logger = LoggerFactory.getLogger(UserProfileController.class);

  @Autowired
  UserProfilesService userProfilesService;

  /*
   * Return the current user's profiles
   */
  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity<UserProfileDTO> getUserProfile(Principal principal) {

    // For non authenticated users, return default profile.
    if (principal == null) {
      UserProfileDTO profiles = new UserProfileDTO();
      profiles.setAllProfiles(ContentMetadataSchema.getDefaultUserProfiles());
      profiles.setCurrentProfileName(profiles.getAllProfiles().get(0).getName());
      return new ResponseEntity<UserProfileDTO>(profiles, HttpStatus.OK);
    }

    UserAuthentication ua = (UserAuthentication) principal;
    String userId = ua.getDetails().getId();

    return new ResponseEntity<UserProfileDTO>(userProfilesService.getUserProfiles(userId, ua
        .getDetails().getUnlockedContentModules()), HttpStatus.OK);
  }

  /*
   * Add/Update a profile in the current user's profile
   */
  @RequestMapping(value = "", method = RequestMethod.POST)
  public ResponseEntity<String> setUserProfile(Principal principal,
      @RequestBody final ProfileTemplate inProfile) {

    UserAuthentication ua = (UserAuthentication) principal;
    String userId = ua.getDetails().getId();

    // Default profile is fixed.
    if (ContentMetadataSchema.getDefaultUserProfileNames().contains(inProfile.getName())) {
      logger.error("Cannot save reserved profile: " + inProfile.getName());
      return new ResponseEntity<String>("Cannot save. Profile name is reserved",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    userProfilesService.setUserProfile(userId, inProfile);
    return new ResponseEntity<String>("Saved", HttpStatus.OK);
  }

  /*
   * Delete profile and update current.
   */
  @RequestMapping(value = "", method = RequestMethod.DELETE)
  public ResponseEntity<String> deleteProfileUpdateCurrent(Principal principal, @RequestParam(
      value = "currentProfileName", required = true) String profileNameToDelete, @RequestParam(
      value = "newCurrentName", required = true) String newCurrentName) {

    UserAuthentication ua = (UserAuthentication) principal;

    // Don't save anonymous user profiles.
    if (principal == null) {
      return new ResponseEntity<String>("Anonymous Ignored", HttpStatus.OK);
    }

    String userId = ua.getDetails().getId();

    userProfilesService.deleteAndSetCurrent(userId, profileNameToDelete, newCurrentName);
    return new ResponseEntity<String>("Saved", HttpStatus.OK);
  }


  /*
   * Return the profile schema
   */
  @RequestMapping(value = "/schema", method = RequestMethod.GET)
  public ResponseEntity<Map<String, Object>> getUserProfileSchema(Principal principal) {

    if (principal == null) {
      return new ResponseEntity<Map<String, Object>>(
          ContentMetadataSchema.getProfileSchemaMap(Arrays
              .asList(ContentModuleMetadata.DEFAULT_MODULE)), HttpStatus.OK);
    } else {
      Set<String> userModules =
          ((UserAuthentication) principal).getDetails().getUnlockedContentModules();
      return new ResponseEntity<Map<String, Object>>(
          ContentMetadataSchema.getProfileSchemaMap(new ArrayList<String>(userModules)),
          HttpStatus.OK);
    }

  }

  /*
   * Updating current profile name.
   */
  @RequestMapping(value = "currentProfileName/{name}", method = RequestMethod.POST)
  public ResponseEntity<String> setCurrentProfileName(Principal principal,
      @PathVariable("name") String profileName) {

    UserAuthentication ua = (UserAuthentication) principal;

    // Don't save anonymous user profiles.
    if (principal == null) {
      return new ResponseEntity<String>("Anonymous Ignored", HttpStatus.OK);
    }
    String userId = ua.getDetails().getId();

    userProfilesService.setCurrentProfile(userId, profileName);
    return new ResponseEntity<String>("Saved", HttpStatus.OK);
  }


  /*
   * General exception handling
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    logger.error("Exception caught:", e);
    return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(e.getMessage());
  }

}
