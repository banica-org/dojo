package com.dojo.codeexecution.exception;

public class NoSuchImageFoundException extends RuntimeException{
    public NoSuchImageFoundException(String message){
        super(message);
    }
}
