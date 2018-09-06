package com.o6.controller;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.o6.dao.DynamicConfig;
import com.o6.dao.DynamicConfigRepository;

@RestController
@RequestMapping(value = "/api/dynamicConfig")
@ControllerAdvice
public class DynamicConfigController {

  private static final Logger logger = LoggerFactory.getLogger(DynamicConfigController.class);

  /*
   * TODO, as config gets more complex, move functionality to its own service
   */
  @Autowired
  DynamicConfigRepository configRepos;

  /*
   * Return notifications
   */
  @SuppressWarnings("unchecked")
  @RequestMapping(value = "/notifications", method = RequestMethod.GET)
  public ResponseEntity<List<String>> getNotifications() {
    DynamicConfig notiConf = configRepos.findOne(DynamicConfig.NOTIFICATION_CONFIG_KEY);
    
    List<String> notifications;

    if (notiConf == null) {
      notifications = Collections.emptyList();
    } else {
      notifications = (List<String>) notiConf.getConfigVal();
    }

    return new ResponseEntity<List<String>>(notifications, HttpStatus.OK);
  }

  /*
   * General exception handling
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<String> handleException(Exception e) {
    logger.error("Exception caught:", e);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
  }

}
