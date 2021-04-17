package com.herokuapp.projectideas.database.document;

public interface Authorization {
    boolean userIsAuthorizedToEdit(String userId);
}
