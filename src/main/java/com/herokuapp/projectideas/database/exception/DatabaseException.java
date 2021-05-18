package com.herokuapp.projectideas.database.exception;

import org.springframework.web.server.ResponseStatusException;

public abstract class DatabaseException extends Exception {

    protected DatabaseException() {}

    public abstract ResponseStatusException toResponseStatusException();
}
