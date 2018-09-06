package com.o6.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.o6.dto.BasicUserAuthInfo;

class StatelessLoginFilter extends AbstractAuthenticationProcessingFilter {

  private final TokenAuthenticationService tokenAuthenticationService;
  private final UserDetailsService userDetailsService;

  protected StatelessLoginFilter(String urlMapping,
      TokenAuthenticationService tokenAuthenticationService, UserDetailsService userDetailsService,
      AuthenticationManager authManager) {
    super(new AntPathRequestMatcher(urlMapping));
    this.userDetailsService = userDetailsService;
    this.tokenAuthenticationService = tokenAuthenticationService;
    setAuthenticationManager(authManager);
  }

  @Override
  public Authentication attemptAuthentication(HttpServletRequest request,
      HttpServletResponse response) throws AuthenticationException, IOException, ServletException {

    final BasicUserAuthInfo user = new ObjectMapper().readValue(request.getInputStream(), BasicUserAuthInfo.class);
    final UsernamePasswordAuthenticationToken loginToken =
        new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword());
    return getAuthenticationManager().authenticate(loginToken);
  }

  @Override
  protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response,
      FilterChain chain, Authentication authentication) throws IOException, ServletException {

    // Lookup the complete User object from the database and create an Authentication for it. 
    // As of now, we have the entire information in the Principal, but later on, we might need to get remaining
    // information from some other place, say, some other user profile table, etc.
    final User authenticatedUser = (User)userDetailsService.loadUserByUsername(((User)authentication.getPrincipal()).getEmail());
    final UserAuthentication userAuthentication = new UserAuthentication(authenticatedUser);

    // Add the custom token as HTTP header to the response
    tokenAuthenticationService.addAuthentication(response, userAuthentication);

    // Add the authentication to the Security context
    SecurityContextHolder.getContext().setAuthentication(userAuthentication);
  }
}
