package com.media.socialmedia.util;

public class UserNotCreatedException extends  RuntimeException{
    public UserNotCreatedException(String msg){
        super(msg);
    }
}