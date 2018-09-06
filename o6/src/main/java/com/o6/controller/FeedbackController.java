package com.o6.controller;

import java.security.Principal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.o6.ApplicationConfig;
import com.o6.dao.UserEvent;
import com.o6.dao.UserEventRepository;
import com.o6.security.User;
import com.o6.security.UserAuthentication;
import com.o6.security.UserRepository;
import com.o6.util.CommonUtils;

@RestController
@RequestMapping(value = "/api/feedback")
@ControllerAdvice
public class FeedbackController {
  private static final Logger logger = LoggerFactory.getLogger(FeedbackController.class);

  /*
   * TODO Later have independent service for it.
   */
  
  @Autowired
  ApplicationConfig appConfig;

  @Autowired
  private UserEventRepository eventRepos;
  
  @Autowired
  UserRepository userRepository;

  @Autowired
  JavaMailSender mailSender;

  /*
   * Convey negative feedback.
   */
  @RequestMapping(value = "/negative", method = RequestMethod.POST)
  public ResponseEntity<String> registerNegativeFeedback(Principal principal,
        @RequestBody final String complaintText) {
    return logFeedback(((UserAuthentication) principal).getDetails(), CommonUtils.NEGATIVE_FEEDBACK, complaintText);
  }

  /*
   * Convey positive or general feedback.
   */
  @RequestMapping(value = "/positiveGeneral", method = RequestMethod.POST)
  public ResponseEntity<String> registerNegativeOrGeneralFeedback(Principal principal,
        @RequestBody final String feedbackText) {
    return logFeedback(((UserAuthentication) principal).getDetails(), CommonUtils.POSITIVE_GENERAL_FEEDBACK, feedbackText);
  }
  
  private ResponseEntity<String> logFeedback(User user, String feedbackType, String message) {
    eventRepos.save(new UserEvent(user.getId(), feedbackType, message));
    sendFeedbackEmail(user.getUsername(), user.getEmail(), feedbackType, message);
    return new ResponseEntity<String>("Feedback registered", HttpStatus.OK);    
  }

  private void sendFeedbackEmail(String userName, String userEmail, String feedbackType, String message) {
    String subject = "Received " + feedbackType + " from " + userName + "<" + userEmail + ">";

    CommonUtils.commonExecService().execute(new Runnable() {
      @Override
      public void run() {
        CommonUtils.sendEmail(mailSender, appConfig.senderName, appConfig.senderEmail, appConfig.senderEmail,
            subject, message, false);
      }
    });
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
