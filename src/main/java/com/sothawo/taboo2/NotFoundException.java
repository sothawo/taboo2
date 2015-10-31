/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

/**
 * Exception that is thrown when something could not be found.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class NotFoundException extends TabooException {
// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates a NotFoundException with the given message.
     *
     * @param message
     *         the message
     */
    public NotFoundException(final String message) {
        super(message);
    }

    /**
     * creates a NotFoundException with the given message and cause.
     *
     * @param message
     *         the message
     * @param cause
     *         the cause
     */
    public NotFoundException(String message, Exception cause) {
        super(message, cause);
    }
}
