package com.herokuapp.projectideas.api.exception;

import com.herokuapp.projectideas.database.exception.DatabaseException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
@ResponseBody
public class ControllerExceptionHandler {

    @ExceptionHandler(DatabaseException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public final void handleDatabaseException(DatabaseException e) {}
}
