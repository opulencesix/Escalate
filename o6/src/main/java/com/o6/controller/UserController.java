package com.o6.controller;

import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.o6.ApplicationConfig;
import com.o6.dao.UserContentRepository;
import com.o6.dto.BasicUserAuthInfo;
import com.o6.security.TokenAuthenticationService;
import com.o6.security.User;
import com.o6.security.UserAuthentication;
import com.o6.security.UserRepository;
import com.o6.security.UserRole;
import com.o6.service.UserProfilesService;
import com.o6.util.CommonUtils;
import com.o6.util.UserExistingException;

/*
 * User controller.
 * 
 * TODO: Currently, some service functionality is also in here, needs to be moved to separate
 * service layer.
 */
@RestController
@ControllerAdvice
public class UserController {

  private static final String EMAIL_CONFIRM_URL_BASE = "/api/register/confirmEmail";
  private static final Logger logger = LoggerFactory.getLogger(UserController.class);

  @Autowired
  ApplicationConfig appConfig;

  @Autowired
  JavaMailSender mailSender;

  @Autowired
  UserRepository userRepository;

  @Autowired
  private UserContentRepository userContentRepository;

  @Autowired
  TokenAuthenticationService tokenAuthenticationService;

  @Autowired
  UserProfilesService userProfilesService;

  @RequestMapping(value = "/api/users/current", method = RequestMethod.GET)
  public User getCurrent() {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication instanceof UserAuthentication) {
      return ((UserAuthentication) authentication).getDetails();
    }
    return new User(authentication.getName()); // anonymous user support
  }

  @RequestMapping(value = "/api/register", method = RequestMethod.POST)
  public ResponseEntity<String> register(@RequestBody BasicUserAuthInfo user,
      HttpServletRequest request, HttpServletResponse response) throws UserExistingException {

    if (user.getUserName().equalsIgnoreCase("Anonymous")) {
      return new ResponseEntity<String>("User Anonymous not allowed",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    if (userRepository.findByEmail(user.getEmail()) != null) {
      return new ResponseEntity<String>("User already present in the system",
          HttpStatus.UNPROCESSABLE_ENTITY);
    }

    // Verify captcha
    RestTemplate restTemplate = new RestTemplate();
    String url =
        String.format(
            "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s&remoteip=%s",
            appConfig.recaptchaKey, user.getReCaptchaVal(), request.getRemoteAddr());

    @SuppressWarnings("rawtypes")
    ResponseEntity<Map> resp = restTemplate.postForEntity(url, null, Map.class);

    if (!resp.getStatusCode().equals(HttpStatus.OK)) {
      return new ResponseEntity<String>("Captcha check failure", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    final BCryptPasswordEncoder pwEncoder = new BCryptPasswordEncoder();

    User dbUser = new User();
    dbUser.setUsername(user.getUserName());
    dbUser.setEmail(user.getEmail());
    dbUser.setEmailVerifyKey(UUID.randomUUID().toString());
    dbUser.setPassword(pwEncoder.encode(user.getPassword()));
    dbUser.grantRole(UserRole.USER);
    userRepository.save(dbUser);

    sendEmailConfirmationEmail(dbUser.getUsername(), dbUser.getEmail(), dbUser.getEmailVerifyKey());

    final UserAuthentication userAuthentication = new UserAuthentication(dbUser);

    // Add the custom token as HTTP header to the response
    tokenAuthenticationService.addAuthentication(response, userAuthentication);

    // Add the authentication to the Security context
    SecurityContextHolder.getContext().setAuthentication(userAuthentication);

    return new ResponseEntity<String>("User registered", HttpStatus.OK);
  }

  /*
   * Confirm email, update database.
   */
  @RequestMapping(value = EMAIL_CONFIRM_URL_BASE + "/{verifyKey}", method = RequestMethod.GET)
  public ResponseEntity<String> confirmEmail(HttpServletResponse httpServletResponse,
      @PathVariable("verifyKey") String verifyKey) {
    final User currentUser = userRepository.findByEmailVerifyKey(verifyKey);

    if (currentUser != null) {
      currentUser.setEmailVerified(true);
      userRepository.save(currentUser);
    }

    httpServletResponse.setHeader("Location", appConfig.appUrl);
    return new ResponseEntity<String>("redirect:" + appConfig.appUrl, HttpStatus.TEMPORARY_REDIRECT);
  }

  /*
   * Confirm email, update database.
   */
  @RequestMapping(value = "/api/user/refreshToken", method = RequestMethod.GET)
  public ResponseEntity<String> refreshToken(Principal principal) {
    UserAuthentication ua = (UserAuthentication) principal;
    final User currentUser = userRepository.findOne(ua.getDetails().getId());

    return new ResponseEntity<String>(tokenAuthenticationService.getToken(currentUser),
        HttpStatus.OK);
  }

  @RequestMapping(value = "/api/changePassword", method = RequestMethod.POST)
  public ResponseEntity<String> changePassword(@RequestBody final BasicUserAuthInfo user) {
    final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    final User currentUser =
        userRepository.findByEmail(((UserAuthentication) authentication).getDetails().getEmail());

    if (user.getNewPassword() == null || user.getNewPassword().length() < 4) {
      return new ResponseEntity<String>("new password too short", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    final BCryptPasswordEncoder pwEncoder = new BCryptPasswordEncoder();
    if (!pwEncoder.matches(user.getPassword(), currentUser.getPassword())) {
      return new ResponseEntity<String>("old password mismatch", HttpStatus.UNPROCESSABLE_ENTITY);
    }

    currentUser.setPassword(pwEncoder.encode(user.getNewPassword()));
    userRepository.save(currentUser);
    return new ResponseEntity<String>("password changed", HttpStatus.OK);
  }

  @RequestMapping(value = "/api/resetPassword", method = RequestMethod.POST)
  public ResponseEntity<String> resetPassword(@RequestBody final BasicUserAuthInfo user) {
    final User currentUser = userRepository.findByEmail(user.getEmail());
    final BCryptPasswordEncoder pwEncoder = new BCryptPasswordEncoder();

    String randomPassword = RandomStringUtils.randomAlphanumeric(8);
    currentUser.setPassword(pwEncoder.encode(randomPassword));
    userRepository.save(currentUser);

    sendResetPasswordEmail(currentUser.getUsername(), currentUser.getEmail(), randomPassword);

    return new ResponseEntity<String>("password changed", HttpStatus.OK);
  }

  @RequestMapping(value = "/admin/api/users", method = RequestMethod.GET)
  public List<User> list() {
    return userRepository.findAll();
  }

  /*
   * Send password reset mail in a separate thread.
   */
  private void sendResetPasswordEmail(String userName, String toAddress, String newPassword) {
    String subject = "Your password reset request";
    String message =
        "Dear " + userName + ",\n\n"
            + "Your password reset request is processed. Your new password is: " + newPassword
            + "\n\nRegards\n--Support Staff.\n";

    CommonUtils.commonExecService().execute(new Runnable() {
      @Override
      public void run() {
        CommonUtils.sendEmail(mailSender, appConfig.senderName, appConfig.senderEmail, toAddress,
            subject, message, false);
      }
    });

  }

  private void sendEmailConfirmationEmail(String userName, String email, String emailConfirmKey) {
    String subject = "Thanks for registering, please confirm email";
    String confirmUrl = appConfig.appUrl + EMAIL_CONFIRM_URL_BASE + "/" + emailConfirmKey;
    String message =
        "<html><body>"
            + "Dear "
            + userName
            + ",<br><br>"
            + "Thanks for registering to O6Escalate. One last step in this process is to confirm your email address. "
            + "To confirm, please click on " + "<a href='" + confirmUrl
            + "' target='_blank'>this link</a>, " + "or open this URL in a browser: " + confirmUrl
            + "<br> " + "<br><br>Regards<br>--Support Staff.<br>" + "</html></body>";

    CommonUtils.commonExecService().execute(new Runnable() {
      @Override
      public void run() {
        CommonUtils.sendEmail(mailSender, appConfig.senderName, appConfig.senderEmail, email,
            subject, message, true);
      }
    });

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
