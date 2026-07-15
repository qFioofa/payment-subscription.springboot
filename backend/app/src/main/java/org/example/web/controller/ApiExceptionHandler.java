package org.example.web.controller;

import org.example.domain.exception.InvalidObligationStateException;
import org.example.domain.exception.ObligationNotFoundException;
import org.example.web.model.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(ObligationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiError handleNotFound(ObligationNotFoundException e) {
        return new ApiError(e.getMessage());
    }

    @ExceptionHandler(InvalidObligationStateException.class)
    @ResponseStatus(HttpStatus.UNPROCESSABLE_ENTITY)
    public ApiError handleInvalidState(InvalidObligationStateException e) {
        return new ApiError(e.getMessage());
    }
}
