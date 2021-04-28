package com.herokuapp.projectideas.database.document;

// TODO: move interfaces to their own directory
public interface Authorization {
    boolean userIsAuthorizedToView(String userId);
    boolean userIsAuthorizedToEdit(String userId);
}
