package com.herokuapp.projectideas.database.exception;

import lombok.Getter;

@Getter
public abstract class DocumentNotFoundException extends DatabaseException {

    private static final long serialVersionUID = 5016848024482820294L;

    protected String documentType;

    public DocumentNotFoundException(String documentType) {
        super();
        this.documentType = documentType;
    }
}
