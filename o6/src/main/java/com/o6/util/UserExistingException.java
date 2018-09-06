package com.o6.util;

import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;

@SuppressWarnings("serial")
@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR, reason = "User already existing")
public class UserExistingException extends Exception {
  public UserExistingException(String msg) {
    super(msg);
  }
}
