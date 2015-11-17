/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository;

import com.google.common.collect.Sets;
import com.sothawo.taboo2.Bookmark;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Abstract implementation of the BookmarkRepository interface that just implements some basic methods.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public abstract class AbstractBookmarkRepository implements BookmarkRepository {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BookmarkRepository ---------------------


    /**
     * get all bookmarks and remove the ids
     * @return all bookmarks without ids
     */
    @Override
    public Collection<Bookmark> dumpBookmarks() {
        final List<Bookmark> clones = getAllBookmarks()
                .stream()
                .map(Bookmark::clone)
                .collect(Collectors.toList());
        for (Bookmark bookmark : clones) {
            bookmark.setId(null);
        }
        return clones;
    }

    /**
     * basic implementation for the combination logic which relies on #getBookmarksWithTag(String) being implemented.
     *
     * @param tags
     *         the tags to be searched
     * @param opAnd
     *         if true, the tags are to be combined using AND, otherwise OR
     * @return
     */
    @Override
    public Collection<Bookmark> getBookmarksWithTags(Collection<String> tags, boolean opAnd) {
        Map<String, Set<Bookmark>> bookmarksForTag = new HashMap<>();
        for (String tag : tags) {
            bookmarksForTag.put(tag, getBookmarksWithTag(tag));
        }

        Set<Bookmark> foundBookmarks = null;
        for (Set<Bookmark> bookmarkSet : bookmarksForTag.values()) {
            if (null == foundBookmarks) {
                foundBookmarks = bookmarkSet;
            } else {
                if (opAnd) {
                    foundBookmarks = Sets.intersection(foundBookmarks, bookmarkSet);
                } else {
                    foundBookmarks = Sets.union(foundBookmarks, bookmarkSet);
                }
            }
        }
        return foundBookmarks;
    }

    /**
     * basic implementation that calls both methods {@link BookmarkRepository#getBookmarksWithTags(Collection, boolean)}
     * and {@link BookmarkRepository#getBookmarksWithSearch(String)} and builds the intersection of both returned
     * collections. Deriving classes might implement a more performant search.
     *
     * @see BookmarkRepository#getBookmarksWithTagsAndSearch(Collection, boolean, String)
     */
    @Override
    public Collection<Bookmark> getBookmarksWithTagsAndSearch(Collection<String> tags, boolean opAnd, String s) {
        if (null == s) {
            return getBookmarksWithTags(tags, opAnd);
        } else if (null == tags) {
            return getBookmarksWithSearch(s);
        } else {
            Set<Bookmark> bookmarksWithTags = new HashSet<>(getBookmarksWithTags(tags, opAnd));
            Set<Bookmark> bookmarksWithString = new HashSet<>(getBookmarksWithSearch(s));
            return Sets.intersection(bookmarksWithTags, bookmarksWithString);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * get all bookmakrs that have a given tag. basic implementation returning an empty set. Used by
     * #getBookmarksWithTags.
     *
     * @param tag
     *         the tag to search for
     * @return set of bookmarks, may be empty but not null
     */
    protected Set<Bookmark> getBookmarksWithTag(String tag) {
        return Collections.emptySet();
    }
}
