package com.herokuapp.projectideas.util;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.Authorization;
import com.herokuapp.projectideas.database.document.Tagged;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import java.util.ArrayList;
import java.util.List;

public class ControllerUtils {

    public static <T extends Authorization> boolean userIsAuthorizedToView(
        T document,
        String userId,
        Database database
    ) {
        try {
            return (
                document.userIsAuthorizedToView(userId) ||
                database.isUserAdmin(userId)
            );
        } catch (EmptyPointReadException e) { // If the user does not exist
            return false;
        }
    }

    public static <T extends Authorization> boolean userIsAuthorizedToEdit(
        T document,
        String userId,
        Database database
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

    /**
     * Gets a list of tags that exist in document1 but not in document2
     */
    public static <T extends Tagged> List<String> getTagsOnlyInFirstDocument(
        T document1,
        T document2
    ) {
        List<String> tags = new ArrayList<>();
        for (String tag : document1.getTags()) {
            if (!document2.getTags().contains(tag)) {
                tags.add(tag);
            }
        }
        return tags;
    }
}
