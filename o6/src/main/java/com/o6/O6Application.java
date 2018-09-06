package com.o6;

import java.io.IOException;
import java.util.Properties;

import javax.servlet.Filter;

import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.EmbeddedServletContainerFactory;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.CharacterEncodingFilter;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.o6.dao.UserContentRepository;
import com.o6.dto.ContentMetadataSchema;
import com.o6.security.User;
import com.o6.security.UserRepository;
import com.o6.security.UserRole;

@SpringBootApplication
public class O6Application {

  @Autowired
  ApplicationConfig appConf;

  public static void main(String[] args) {
    SpringApplication.run(O6Application.class, args);
  }

  /*
   * Application wide beans. TODO later move out to different files if this gets large.
   */

  @Bean
  public InitializingBean insertDefaultUsers() {
    return new InitializingBean() {

      @Autowired
      private UserRepository userRepository;
      @Autowired
      private UserContentRepository contentRepository;

      @Override
      public void afterPropertiesSet() throws JsonParseException, JsonMappingException, IOException {
        if(!StringUtils.isEmpty(appConf.contentMetadataSchemaOverride)) {
          ContentMetadataSchema.overrideProfileSchemaMap(appConf.contentMetadataSchemaOverride);
        }
        
        if(!StringUtils.isEmpty(appConf.defaultUserProfileOverride)) {
          ContentMetadataSchema.overrideDefaultUserProfiles(appConf.defaultUserProfileOverride);
        }
        /*
         * Hardcoded repository creation for testing purposes. 
         * addUser("admin", "admin", UserRole.ADMIN); 
         * addUser("user", "user", UserRole.USER); 
         * contentRepository.deleteAll();
         * contentRepository.save(new UserContent("junkUserId1", "junkContentId1", "youTubeVideo",
         * "UBckPTwwcmo"));
         */
      }

      private void addUser(String username, String password, UserRole role) {
        User user = new User();
        user.setUsername(username);
        user.grantRole(role);
        user.setPassword(new BCryptPasswordEncoder().encode(password));
        userRepository.save(user);
      }
    };
  }

  @Bean
  public JavaMailSender mailSender() {

    JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();

    javaMailSender.setHost(appConf.smtpHost);
    javaMailSender.setPort(appConf.smtpPort);
    javaMailSender.setUsername(appConf.senderEmail);
    javaMailSender.setPassword(appConf.senderPassword);
    javaMailSender.setProtocol("smtps");

    Properties props = new Properties();
    props.put("mail.smtp.auth", "true");
    props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
    props.put("mail.smtp.starttls.enable", "true");

    javaMailSender.setJavaMailProperties(props);

    return javaMailSender;

  }

  @Bean
  public Filter characterEncodingFilter() {
    CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
    characterEncodingFilter.setEncoding("UTF-8");
    characterEncodingFilter.setForceEncoding(true);
    return characterEncodingFilter;
  }


  /*
   * Redirect http to https
   */
  @Bean
  public EmbeddedServletContainerFactory servletContainer() {
    TomcatEmbeddedServletContainerFactory tomcat = new TomcatEmbeddedServletContainerFactory() {
      @Override
      protected void postProcessContext(Context context) {
        SecurityConstraint securityConstraint = new SecurityConstraint();
        securityConstraint.setUserConstraint("CONFIDENTIAL");
        SecurityCollection collection = new SecurityCollection();
        collection.addPattern("/*");
        securityConstraint.addCollection(collection);
        context.addConstraint(securityConstraint);
      }
    };

    tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
    return tomcat;
  }

  private Connector initiateHttpConnector() {
    Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
    connector.setScheme("http");
    connector.setPort(appConf.serverHttpPort);
    connector.setSecure(false);
    connector.setRedirectPort(appConf.serverHttpsPort);

    return connector;
  }

}
