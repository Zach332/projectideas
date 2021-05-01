package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.user.NotificationPreference;
import com.herokuapp.projectideas.database.exception.EmptySingleDocumentQueryException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
public class EmailController {

    @Autowired
    Database database;

    @PostMapping("/api/email/notificationPreference/{emailSubscriptionId}")
    public void updateEmailNotificationPreference(
        @PathVariable String emailSubscriptionId,
        @RequestBody NotificationPreference notificationPreference
    ) {
        try {
            database.updateEmailNotificationPreference(
                emailSubscriptionId,
                notificationPreference
            );
        } catch (EmptySingleDocumentQueryException e) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Subscription id " + emailSubscriptionId + " does not exist."
            );
        }
    }
}
