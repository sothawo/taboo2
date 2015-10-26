/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository;

/**
 * class tha can create BookmarkRepository implementations. Needed as the TestRunner cannot instantiate
 * BookmarkRepositories with different constructor arguments.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public interface BookmarkRepositoryFactory {
// -------------------------- OTHER METHODS --------------------------

    /**
     * create a BookmarkRepository withe the given arguments.
     *
     * @param args
     *         arguments for the BookmarkRepository
     * @return BookmarkRepository
     */
    BookmarkRepository create(String[] args);
}
