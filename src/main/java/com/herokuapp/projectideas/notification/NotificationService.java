package com.herokuapp.projectideas.notification;

import com.herokuapp.projectideas.database.Database;
import com.herokuapp.projectideas.database.document.user.User;
import com.herokuapp.projectideas.database.exception.EmptyPointReadException;
import com.herokuapp.projectideas.email.EmailInterface;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for sending notification emails. Waits before sending an email.
 */
@Service
public class NotificationService {

    @Autowired
    @Lazy
    Database database;

    @Autowired
    EmailInterface emailInterface;

    private static final int MILLISECONDS_BEFORE_SENDING_EMAIL = 1000 * 60 * 5;

    @Async
    public void notifyUserOfUnreadMessages(User user)
        throws EmptyPointReadException {
        int numUnreadMessages = user.getUnreadMessages();
        if (numUnreadMessages > 0) {
            boolean sendEmail =
                switch (user.getNotificationPreference()) {
                    // Only send an email if the user is up-to-date with reading messages
                    case Default -> numUnreadMessages == 1;
                    case AllNewMessages -> true;
                    case Unsubscribed -> false;
                };

            if (sendEmail) {
                // Wait before sending email in case the user has checked their messages
                // since the start of this method
                try {
                    Thread.sleep(MILLISECONDS_BEFORE_SENDING_EMAIL);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                user = database.getUser(user.getUserId());
                numUnreadMessages = user.getUnreadMessages();
                if (numUnreadMessages > 0) {
                    emailInterface.sendUnreadMessagesEmail(
                        user,
                        numUnreadMessages
                    );
                }
            }
        }
    }
}
