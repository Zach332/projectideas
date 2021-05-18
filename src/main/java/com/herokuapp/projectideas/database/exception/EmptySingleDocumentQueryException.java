package com.herokuapp.projectideas.database.exception;

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
    public String toString() {
        return "Single document query '" + query + "' returned no results.";
    }
}
