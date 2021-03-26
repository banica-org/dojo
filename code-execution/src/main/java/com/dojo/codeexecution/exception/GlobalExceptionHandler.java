package com.dojo.codeexecution.exception;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler({NoSuchImageFoundException.class})
    public String handleException(NoSuchImageFoundException exception){
        return exception.getMessage();
    }

    @ExceptionHandler({NoLogsForContainerException.class})
    public String handleException(NoLogsForContainerException exception){
        return exception.getMessage();
    }

}
