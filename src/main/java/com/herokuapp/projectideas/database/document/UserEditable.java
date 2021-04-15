package com.herokuapp.projectideas.database.document;

public interface UserEditable {
    boolean userIsAuthorizedToEdit(String userId);
}
