package com.media.socialmedia.util;

public class RefreshTokenExpireException extends RuntimeException {
    public RefreshTokenExpireException(String msg) {
        super(msg);
    }
}
