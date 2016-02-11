package com.epam.search.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

/**
 * Created by Dmytro_Kovalskyi on 11.02.2016.
 */
@ControllerAdvice
class GlobalDefaultExceptionHandler {

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(value = HttpStatus.BAD_REQUEST)
    public
    @ResponseBody
    String defaultErrorHandler(HttpServletRequest req, Exception e) throws Exception {
        return "Error " + e.getMessage();
    }
}
