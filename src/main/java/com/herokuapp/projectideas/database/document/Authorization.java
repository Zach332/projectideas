package com.herokuapp.projectideas.database.document;

public interface Authorization {
    boolean userIsAuthorizedToView(String userId);
    boolean userIsAuthorizedToEdit(String userId);
}
