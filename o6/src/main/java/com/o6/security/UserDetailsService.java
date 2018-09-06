package com.o6.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsService implements org.springframework.security.core.userdetails.UserDetailsService  {

  @Autowired
  private UserRepository userRepo;

  @Override
  public final User loadUserByUsername(String email) throws UsernameNotFoundException {
    final User user = userRepo.findByEmail(email);
    if (user == null) {
      throw new UsernameNotFoundException("user not found");
    }
    return user;
  }
}
