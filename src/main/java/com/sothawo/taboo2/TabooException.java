/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

/**
 * base class for exceptions of the taboo service. Extends RuntimeException.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class TabooException extends RuntimeException {
// --------------------------- CONSTRUCTORS ---------------------------

    /**
     * creates a TabooException with the given message.
     *
     * @param message
     *         the message
     */
    public TabooException(final String message) {
        super(message);
    }

    /**
     * creates a TabbooException with message and cause.
     *
     * @param message
     *         the message
     * @param cause
     *         the cause
     */
    public TabooException(String message, Throwable cause) {
        super(message, cause);
    }
}
