/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

/**
 * Exception thrown if something already exists.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class AlreadyExistsException extends TabooException {
// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates an AlreadyExistsException with a message.
     *
     * @param message
     *         the message
     */
    public AlreadyExistsException(final String message) {
        super(message);
    }
}
