package com.herokuapp.projectideas.database.exception;

/**
 * Represents an error when a user attempts to update a document based
 * on an outdated version.
 *
 * For example, if user A gets an idea, then user B gets the same idea,
 * then user A updates the idea, user B would receive an
 * OutdatedDocumentWriteException if they tried to update the idea.
 * This would require user B to obtain an up-to-date copy of the idea
 * before successfully updating it.
 */
public class OutdatedDocumentWriteException extends Exception {

    private static final long serialVersionUID = -988931595864752375L;
}
