package com.herokuapp.projectideas.database.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmptySingleDocumentQueryException
    extends DocumentNotFoundException {

    private static final long serialVersionUID = 1247140589529514770L;

    protected String query;

    public EmptySingleDocumentQueryException(
        String documentType,
        String query
    ) {
        super(documentType);
        this.query = query;
    }

    @Override
    public ResponseStatusException toResponseStatusException() {
        return new ResponseStatusException(
            HttpStatus.NOT_FOUND,
            "Single document query '" + query + "' returned no results."
        );
    }
}
