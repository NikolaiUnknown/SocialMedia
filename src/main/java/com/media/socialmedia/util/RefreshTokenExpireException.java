package com.media.socialmedia.util;

public class RefreshTokenExpireException extends RuntimeException {
  public RefreshTokenExpireException(String message) {
    super(message);
  }
}
