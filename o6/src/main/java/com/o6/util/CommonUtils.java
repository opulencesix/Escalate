package com.o6.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

public class CommonUtils {
  public static String TEMPLATE_USER_ID = "templateUserId";
  public static String CONTENT_SEEN = "consumed";
  public static String UNLOCK_MODULE = "unlockModule";
  public static String UNLOCK_MODULE_FAIL = "unlockModule";
  public static String NEGATIVE_FEEDBACK = "complaint";
  public static String POSITIVE_GENERAL_FEEDBACK = "feedback";
  public static String CONTENT_INPROGRESS = "inProgress";
  public static String CONTENT_TOGGLE_FAVORITE = "toggleFavorite";
  public static String DEFAULT_PROFILE_NAME = "Default";

  private static ExecutorService executor = Executors.newFixedThreadPool(1);
  
  public static ExecutorService commonExecService() {
    return executor;
  }
  
  public static void tryToSleep(int millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ex) {
      // Do nothing
    }
  }

  /*
   * Send email
   */
  public static void sendEmail(JavaMailSender mailSender, String senderName, String senderEmail,
      String toAddress, String subject, String messageContents, boolean isHtml) {

    MimeMessagePreparator preparator = new MimeMessagePreparator() {

      public void prepare(MimeMessage mimeMessage) throws Exception {
        
        MimeMessageHelper message = new MimeMessageHelper(mimeMessage);

        message.setTo(new InternetAddress(toAddress));
        message.setSubject(subject);
        message.setFrom(new InternetAddress(senderName + "<" + senderEmail + ">"));
        message.setText(messageContents, isHtml);
      }
    };

    mailSender.send(preparator);

  }



}
