package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.exception.EmptySingleDocumentQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EmailController {

    @Autowired
    Database database;

    @PostMapping("/api/email/unsubscribe/{unsubscribeId}")
    public void unsubscribeFromEmailNotifications(
        @PathVariable String unsubscribeId
    ) {
        try {
            database.unsubscribeFromEmailNotifications(unsubscribeId);
        } catch (EmptySingleDocumentQueryException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Unsubscribe id " + unsubscribeId + " does not exist."
            );
        }
    }
}
