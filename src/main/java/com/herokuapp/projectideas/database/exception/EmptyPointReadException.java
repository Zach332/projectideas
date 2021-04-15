package com.herokuapp.projectideas.database.exception;

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

    public String toString() {
        return (
            documentType +
            " with id '" +
            documentId +
            "' and partition key '" +
            documentPartitionKey +
            "' could not be found."
        );
    }
}
