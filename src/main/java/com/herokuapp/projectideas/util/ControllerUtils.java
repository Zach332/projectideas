package com.herokuapp.projectideas.util;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.Authorization;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import org.springframework.beans.factory.annotation.Autowired;

public class ControllerUtils {

    @Autowired
    private static Database database;

    public static <T extends Authorization> boolean userIsAuthorized(
        T document,
        String userId
    ) {
        try {
            return (
                document.userIsAuthorizedToEdit(userId) ||
                database.isUserAdmin(userId)
            );
        } catch (EmptyPointReadException e) { // If the user does not exist
            return false;
        }
    }
}
