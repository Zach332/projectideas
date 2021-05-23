package com.herokuapp.projectideas.database.document.user;

/**
 * User preferences for email notifications
 */
public enum NotificationPreference {
    /**
     * Send emails when user receives a message only if
     * the user was not recently sent an email
     */
    Default,
    /**
     * Send emails for all new messages
     */
    AllNewMessages,
    /**
     * Never send message-related emails
     */
    Unsubscribed,
}
