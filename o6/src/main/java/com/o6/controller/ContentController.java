package com.o6.controller;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
import org.springframework.web.bind.annotation.RestController;

import com.o6.dto.ContentReference;
import com.o6.dto.ContentRenderInfo;
import com.o6.dto.ProfileTemplate;
import com.o6.security.UserAuthentication;
import com.o6.service.ContentService;
import com.o6.util.CommonUtils;

@RestController
@RequestMapping(value = "/api/content")
@ControllerAdvice
public class ContentController {

  private static final Logger logger = LoggerFactory.getLogger(ContentController.class);

  private final ContentService contentService;

  @Autowired
  ContentController(ContentService service) {
    this.contentService = service;
  }

  @RequestMapping(value = "/newRelevant", method = RequestMethod.POST)
  public List<ContentRenderInfo> getUserContent(Principal principal,
      @RequestBody final ProfileTemplate forProfile) {
    UserAuthentication ua = (UserAuthentication) principal;
    return contentService.getNewRelevantContent((ua == null ? null : ua.getDetails()), forProfile);
  }

  @RequestMapping(value = "/consumed", method = RequestMethod.GET)
  public List<ContentRenderInfo> getConsumedContent(Principal principal) {
    UserAuthentication ua = (UserAuthentication) principal;
    if(ua == null) {
      return Collections.emptyList();
    }
    return contentService.getConsumedContent(ua.getDetails());
  }

  @RequestMapping(value = "/inProgress", method = RequestMethod.GET)
  public List<ContentRenderInfo> getInProgressContent(Principal principal) {
    UserAuthentication ua = (UserAuthentication) principal;
    if(ua == null) {
      return Collections.emptyList();
    }
    return contentService.getInProgressContent(ua.getDetails());
  }

  @RequestMapping(value = "/favorite", method = RequestMethod.GET)
  public List<ContentRenderInfo> getFavoriteContent(Principal principal) {
    UserAuthentication ua = (UserAuthentication) principal;
    if(ua == null) {
      return Collections.emptyList();
    }
    return contentService.getFavoriteContent(ua.getDetails());
  }

  @RequestMapping(value = "/special", method = RequestMethod.GET)
  public List<ContentRenderInfo> getPremiumContent(Principal principal) {
    UserAuthentication ua = (UserAuthentication) principal;
    if(ua == null) {
      return Collections.emptyList();
    }
    return contentService.getPremiumContent(ua.getDetails());
  }

  @RequestMapping(value = "/search", method = RequestMethod.POST)
  public List<ContentRenderInfo> searchContent(@RequestBody final Map<String, String> searchStrObj) {
    return contentService.searchContent(searchStrObj.get("searchStr"));
  }

  @RequestMapping(value = "/event/{eventType}", method = RequestMethod.POST)
  public void recordContentEvent(Principal principal, @PathVariable("eventType") String eventType,
      @RequestBody final ContentReference ref) {
    UserAuthentication ua = (UserAuthentication) principal;
    
    // For unauthenticated users, don't log the event.
    // TODO, later, check if we want to at least record events
    // from anonymous users for analysis.
    if(ua == null) {
      return;
    }

    if (eventType.equalsIgnoreCase(CommonUtils.CONTENT_INPROGRESS)) {
      contentService.setInProgressContent(ua.getDetails(), ref.getContentId(), ref.getLanguage());
    } else if (eventType.equalsIgnoreCase(CommonUtils.CONTENT_SEEN)) {
      contentService.setConsumedContent(ua.getDetails(), ref.getContentId(), ref.getLanguage());
    } else if (eventType.equalsIgnoreCase(CommonUtils.CONTENT_TOGGLE_FAVORITE)) {
      contentService.toggleFavoriteContent(ua.getDetails(), ref.getContentId(), ref.getLanguage());
    }
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
