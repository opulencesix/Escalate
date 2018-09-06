package com.o6.controller;

import java.security.Principal;
import java.util.Base64;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.o6.ApplicationConfig;
import com.o6.dao.ContentModuleMetadata;
import com.o6.dao.ContentModuleMetadataRepository;
import com.o6.dao.UserEvent;
import com.o6.dao.UserEventRepository;
import com.o6.dto.UserContentModuleDTO;
import com.o6.security.TokenAuthenticationService;
import com.o6.security.User;
import com.o6.security.UserAuthentication;
import com.o6.security.UserRepository;
import com.o6.util.CommonUtils;

@RestController
@RequestMapping(value = "/api/contentModule")
@ControllerAdvice
public class ContentModuleController {
  private static final Logger logger = LoggerFactory.getLogger(ContentModuleController.class);

  /*
   * TODO Later have independent service for it.
   */

  @Autowired
  ApplicationConfig appConfig;

  @Autowired
  ContentModuleMetadataRepository contentModuleMDRepos;

  @Autowired
  private UserEventRepository eventRepos;

  @Autowired
  UserRepository userRepository;

  @Autowired
  TokenAuthenticationService tokenAuthenticationService;

  @Autowired
  JavaMailSender mailSender;

  /*
   * Return subscribed and unscribed content modules for user
   */
  @RequestMapping(value = "", method = RequestMethod.GET)
  public ResponseEntity<UserContentModuleDTO> getUserContentModules(Principal principal) {
    Set<String> userUnlockedModules =
        ((UserAuthentication) principal).getDetails().getUnlockedContentModules();

    UserContentModuleDTO retVal = new UserContentModuleDTO();
    for (ContentModuleMetadata cmmd : contentModuleMDRepos.findAll()) {
      if (cmmd.getId().equals(ContentModuleMetadata.DEFAULT_MODULE)
          || (cmmd.getStatus() != null && cmmd.getStatus().equals(
              ContentModuleMetadata.ModuleStatus.INACTIVE))) {
        continue;
      }
      if (userUnlockedModules.contains(cmmd.getId())) {
        retVal.getSubscribedModules().add(cmmd);
      } else {
        retVal.getUnSubscribedModules().add(cmmd);
      }
    }

    return new ResponseEntity<UserContentModuleDTO>(retVal, HttpStatus.OK);
  }


  /*
   * Unlock content module Payment APIs from: https://docs.razorpay.com/docs/payments
   */
  @RequestMapping(value = "/unlockForMe", method = RequestMethod.POST)
  public ResponseEntity<String> unlockContentModule(Principal principal, @RequestParam(
      value = "contentModuleId", required = true) String conModId, @RequestParam(
      value = "paymentGatewayResponseId", required = true) String paymentGtwyRespId) {

    User user = ((UserAuthentication) principal).getDetails();
    final User dbUser = userRepository.findOne(user.getId());

    // Find amount of item.
    ContentModuleMetadata cmmd = contentModuleMDRepos.findOne(conModId);
    String amountStr = "0";
    if (cmmd != null) {
      amountStr = String.valueOf(Math.round(cmmd.getInrPriceRupees() * 100));
    } else {
      logger.error("No content module metadata found even if payment is done, for module id: "
          + conModId);
    }

    // Verify payment gateway (razorpay) success status
    RestTemplate restTemplate = new RestTemplate();
    String url = "https://api.razorpay.com/v1/payments/" + paymentGtwyRespId;

    // Create http headers.
    String base64Creds =
        new String(Base64.getEncoder().encode(
            (appConfig.gatewayKey + ":" + appConfig.gatewaySecret).getBytes()));
    HttpHeaders headers = new HttpHeaders();
    headers.add("Authorization", "Basic " + base64Creds);
    HttpEntity<String> request = new HttpEntity<String>(headers);

    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> resp = restTemplate.exchange(url, HttpMethod.GET, request, Map.class);

    // Now, capture the payment if authorized (to trigger money transfer to account)
    if (resp.getStatusCode().equals(HttpStatus.OK)
        && ((String) (resp.getBody().get("status"))).equalsIgnoreCase("authorized")) {
      resp = restTemplate.postForEntity(url + "/capture?amount=" + amountStr, request, Map.class);
    } else {
      if (resp.getStatusCode().equals(HttpStatus.OK)) {
        logger.error("Transaction capture failed, please investigate. Error: "
            + resp.getBody().get("error_description"));
      }
      eventRepos.save(new UserEvent(user.getId(), CommonUtils.UNLOCK_MODULE_FAIL, String
          .valueOf(conModId) + ",TransactionFetchFail"));
      return new ResponseEntity<String>("Could not capture payment transaction",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    String paymentStatus;
    if (resp.getStatusCode().equals(HttpStatus.OK)) {
      paymentStatus = "PaymentStatus:Success,CaptureId:" + (String) resp.getBody().get("id");
    } else {
      paymentStatus = "Transaction capture failed";
      eventRepos.save(new UserEvent(user.getId(), CommonUtils.UNLOCK_MODULE_FAIL, String
          .valueOf(conModId) + ",TransactionCaptureFail"));
      return new ResponseEntity<String>(paymentStatus, HttpStatus.UNPROCESSABLE_ENTITY);
    }

    eventRepos.save(new UserEvent(user.getId(), CommonUtils.UNLOCK_MODULE, String.valueOf(conModId)
        + "," + paymentStatus));

    dbUser.getUnlockedContentModules().add(conModId);
    userRepository.save(dbUser);

    return new ResponseEntity<String>(tokenAuthenticationService.getToken(dbUser), HttpStatus.OK);
  }

  /*
   * Unlock content module
   */
  @RequestMapping(value = "/unlockForUser/{userEmail}/{contentModuleId}",
      method = RequestMethod.POST)
  public ResponseEntity<String> unlockContentModuleForUser(Principal principal,
      @PathVariable("contentModuleId") String conModId, @PathVariable("userEmail") String userEmail) {
    final User dbUser = userRepository.findByEmail(userEmail);
    eventRepos.save(new UserEvent(dbUser.getId(), CommonUtils.UNLOCK_MODULE, String
        .valueOf(conModId) + ",by admin"));

    dbUser.getUnlockedContentModules().add(conModId);
    userRepository.save(dbUser);

    return new ResponseEntity<String>(tokenAuthenticationService.getToken(dbUser), HttpStatus.OK);
  }

  /*
   * Lock content module
   */
  @RequestMapping(value = "/lockForUser/{userEmail}/{contentModuleId}", method = RequestMethod.POST)
  public ResponseEntity<String> lockContentModuleForUser(Principal principal,
      @PathVariable("contentModuleId") int conModId, @PathVariable("userEmail") String userEmail) {
    final User dbUser = userRepository.findByEmail(userEmail);
    eventRepos.save(new UserEvent(dbUser.getId(), CommonUtils.UNLOCK_MODULE, String
        .valueOf(conModId) + ",by admin"));

    dbUser.getUnlockedContentModules().remove(conModId);
    userRepository.save(dbUser);

    return new ResponseEntity<String>(tokenAuthenticationService.getToken(dbUser), HttpStatus.OK);
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
