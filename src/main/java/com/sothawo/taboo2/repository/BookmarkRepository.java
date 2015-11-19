/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository;

import com.sothawo.taboo2.AlreadyExistsException;
import com.sothawo.taboo2.Bookmark;
import com.sothawo.taboo2.NotFoundException;

import java.util.Collection;

/**
 * Implementations of this interface store and retrieve Bookmarks. Extends AutoCloseable so it might be used in contexts
 * using this feature.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public interface BookmarkRepository extends AutoCloseable {
// -------------------------- OTHER METHODS --------------------------

    /**
     * creates a bookmark in the repository.
     *
     * @param bookmark
     *         the new bookmark. must not have the id set
     * @return the created bookmark with it's id
     * @throws IllegalArgumentException
     *         if the id is set in bookmark
     * @throws AlreadyExistsException
     *         if a bookmark with the given url already exists
     */
    Bookmark createBookmark(Bookmark bookmark);

    /**
     * deletes the bookmark with the given id.
     *
     * @param id
     *         id of the bookmark to delete
     * @throws NotFoundException
     *         if no bookmark is found for the given id
     */
    void deleteBookmark(String id);

    /**
     * returns all bookmarks without their ids.
     *
     * @return the bookmarks
     */
    Collection<Bookmark> dumpBookmarks();

    /**
     * returns all bookmarks in the repository.
     *
     * @return the bookmarks
     */
    Collection<Bookmark> getAllBookmarks();

    /**
     * returns all tags that are stored in the repository.
     *
     * @return Collection of tags, may be emoty, not null
     */
    Collection<String> getAllTags();

    /**
     * returns the bookmark for the given id.
     *
     * @param id
     *         id of the bookmark
     * @return the bookmark
     * @throws NotFoundException
     *         if no bookmark is found for the given id
     */
    Bookmark getBookmarkById(String id);

    /**
     * returns the bookmarks that contain the given string. The search must be case insensitive.
     *
     * @param s
     *         the substring to search
     * @return the found bookmarks
     */
    Collection<Bookmark> getBookmarksWithSearch(String s);

    /**
     * returns all bookmarks that have all of the given tags.
     *
     * @param tags
     *         the tags to be searched
     * @param opAnd
     *         if true, the tags are to be combined using AND, otherwise OR
     * @return the found bookmarks
     */
    Collection<Bookmark> getBookmarksWithTags(Collection<String> tags, boolean opAnd);

    /**
     * returns all bookmarks that have all of the given tags and that contain the given search string.
     *
     * @param tags
     *         the tags to be searched
     * @param opAnd
     *         if true, the tags are to be combined using AND, otherwise OR
     * @param s
     *         the string to search for
     * @return the found bookmarks
     */
    Collection<Bookmark> getBookmarksWithTagsAndSearch(Collection<String> tags, boolean opAnd, String s);

    /**
     * removes all bookmarks and tags from the repository.
     */
    void purge();

    /**
     * updates a bookmark.
     *
     * @param bookmark
     *         the bookmark to update
     * @throws NullPointerException
     *         when bookmark is null
     * @throws IllegalArgumentException
     *         when the bookmark has no id
     * @throws NotFoundException
     *         when no bookmark with the given id is found
     */
    void updateBookmark(Bookmark bookmark);
}
