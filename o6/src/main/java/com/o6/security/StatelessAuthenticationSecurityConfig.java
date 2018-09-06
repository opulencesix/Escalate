package com.o6.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@EnableWebSecurity
@Configuration
@Order(1)
public class StatelessAuthenticationSecurityConfig extends WebSecurityConfigurerAdapter {

  @Autowired
  private UserDetailsService userDetailsService;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TokenAuthenticationService tokenAuthenticationService;

  public StatelessAuthenticationSecurityConfig() {
    super(true);
  }

  @Override
  protected void configure(HttpSecurity http) throws Exception {
    http
        .exceptionHandling()
        .and()
        .anonymous()
        .and()
        .authorizeRequests()

        // allow anonymous resource requests
        .antMatchers("/")
        .permitAll()
        .antMatchers("/favicon.ico")
        .permitAll()
        .antMatchers("/bundle/**")
        .permitAll()
        .antMatchers("/fonts/**")
        .permitAll()
        .antMatchers("/img/**")
        .permitAll()

        // allow anonymous POSTs to login, register, reset password
        .antMatchers(HttpMethod.POST, "/api/login")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/api/register")
        .permitAll()
        .antMatchers(HttpMethod.GET, "/api/register/**")
        .permitAll()
        .antMatchers(HttpMethod.POST, "/api/resetPassword")
        .permitAll()

        // Anything related to paidModules should be accessible only to registered users
        .antMatchers("/api/contentModule")
        .hasAnyRole("USER", "ADMIN")
        .antMatchers("/api/contentModule/unlockForMe")
        .hasAnyRole("USER", "ADMIN")
        .antMatchers("/api/contentModule/unlockForUser")
        .hasAnyRole("ADMIN")
        .antMatchers("/api/contentModule/lockForUser")
        .hasAnyRole("ADMIN")
        .antMatchers(HttpMethod.POST, "/api/contentModule/paymentSuccess")
        .permitAll()

        // Anything related to feedback should be accessible only to registered users
        .antMatchers("/api/feedback/**")
        .hasAnyRole("USER", "ADMIN")

        // allow anonymous GETs and posts to content API
        .antMatchers("/api/content/**")
        .permitAll()

        // allow anonymous GETs to profile API
        .antMatchers(HttpMethod.GET, "/api/profile/**")
        .permitAll()

        .antMatchers(HttpMethod.GET, "/api/dynamicConfig/**")
        .permitAll()

        // Any API access should have USER role
        .antMatchers("/api/**")
        .hasAnyRole("USER", "ADMIN")
        
        // defined Admin only API area
        //.antMatchers("/admin/**")
        //.hasRole("ADMIN")

        // all other request need to be authenticated
        .anyRequest()
        .hasRole("ADMIN")
        .and()

        // custom JSON based authentication by POST of {"email":"<email>","password":"<password>"}
        // which sets the token header upon authentication
        .addFilterBefore(
            new StatelessLoginFilter("/api/login", tokenAuthenticationService, userDetailsService,
                authenticationManager()), UsernamePasswordAuthenticationFilter.class)

        // custom Token based authentication based on the header previously given to the client
        .addFilterBefore(new StatelessAuthenticationFilter(tokenAuthenticationService),
            UsernamePasswordAuthenticationFilter.class)
        .servletApi()
        .and()
        .headers()
        .cacheControl();
  }

  @Bean
  @Override
  public AuthenticationManager authenticationManagerBean() throws Exception {
    return super.authenticationManagerBean();
  }

  @Override
  protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder());
  }

  @Override
  protected UserDetailsService userDetailsService() {
    return userDetailsService;
  }
}
