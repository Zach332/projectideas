package com.herokuapp.projectideas.api;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.exception.DatabaseException;
import com.herokuapp.projectideas.dto.email.UpdateNotificationPreferenceDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@CrossOrigin
public class EmailController {

    @Autowired
    Database database;

    @PostMapping("/api/email/notificationPreference/{emailSubscriptionId}")
    public void updateEmailNotificationPreference(
        @PathVariable String emailSubscriptionId,
        @RequestBody UpdateNotificationPreferenceDTO notificationPreferenceDTO
    ) throws DatabaseException {
        database.updateEmailNotificationPreference(
            emailSubscriptionId,
            notificationPreferenceDTO.getNotificationPreference()
        );
    }
}
