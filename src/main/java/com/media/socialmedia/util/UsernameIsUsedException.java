package com.media.socialmedia.util;

public class UsernameIsUsedException extends RuntimeException{
    public UsernameIsUsedException(String msg) {
        super(msg);
    }
}
