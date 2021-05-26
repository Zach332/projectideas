package com.herokuapp.projectideas.database.document.user;

/**
 * User preferences for email notifications
 */
public enum NotificationPreference {
    /**
     * Send emails when user receives a message after a
     * short minute wait, assuming they have not read the
     * message since
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
