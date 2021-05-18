package com.herokuapp.projectideas.database.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmptyPointReadException extends DocumentNotFoundException {

    private static final long serialVersionUID = 5003988241317271418L;

    protected String documentId;
    protected String documentPartitionKey;

    public EmptyPointReadException(
        String documentType,
        String documentId,
        String documentPartitionKey
    ) {
        super(documentType);
        this.documentId = documentId;
        this.documentPartitionKey = documentPartitionKey;
    }

    @Override
    public ResponseStatusException toResponseStatusException() {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            documentType +
            " with id '" +
            documentId +
            "' and partition key '" +
            documentPartitionKey +
            "' could not be found."
        );
    }
}
