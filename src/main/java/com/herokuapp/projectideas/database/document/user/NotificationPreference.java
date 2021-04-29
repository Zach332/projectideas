package com.herokuapp.projectideas.database.document.user;

/**
 * User preferences for email notifications
 */
public enum NotificationPreference {
    /**
     * Send emails when user receives a message only if
     * (a) the user was not recently sent an email and (b)
     * the user was not active recently
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
