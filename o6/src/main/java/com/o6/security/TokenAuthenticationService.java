package com.o6.security;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.DatatypeConverter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class TokenAuthenticationService {

  private static final String AUTH_HEADER_NAME = "X-AUTH-TOKEN";
  private static final String AUTH_DISPLAY_NAME = "DISPLAY-USERNAME";
  private static final long TOKEN_EXPIRY_DURATION_SECS = 7 * 24 * 60 * 60;

  private final TokenHandler tokenHandler;

  @Autowired
  public TokenAuthenticationService(@Value("${token.secret}") String secret) {
    tokenHandler = new TokenHandler(DatatypeConverter.parseBase64Binary(secret));
  }

  /*
   * Create a token and add it to response header, with appropriate expiry time.
   */
  public void addAuthentication(HttpServletResponse response, UserAuthentication authentication) {
    final User user = authentication.getDetails();
    user.setExpires(System.currentTimeMillis()/1000 + TOKEN_EXPIRY_DURATION_SECS);

    response.addHeader(AUTH_HEADER_NAME, tokenHandler.createTokenForUser(user));
    response.addHeader(AUTH_DISPLAY_NAME, user.getUsername());
  }

  /*
   * Just create a token for the user, with appropriate expiry time
   */
  public String getToken(User user) {
    user.setExpires(System.currentTimeMillis()/1000 + TOKEN_EXPIRY_DURATION_SECS);
    return tokenHandler.createTokenForUser(user);   
  }

  public Authentication getAuthentication(HttpServletRequest request) {
    final String token = request.getHeader(AUTH_HEADER_NAME);
    if (token != null) {
      final User user = tokenHandler.parseUserFromToken(token);
      if (user != null && user.getExpires() >= System.currentTimeMillis()/1000) {
        return new UserAuthentication(user);
      }
    }
    return null;
  }
}
