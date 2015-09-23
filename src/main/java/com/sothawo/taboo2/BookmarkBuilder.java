/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import java.util.ArrayList;
import java.util.Collection;

/**
 * class for building Bookmark objects.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class BookmarkBuilder {
// ------------------------------ FIELDS ------------------------------

    /** the id of the bookmark. */
    private String id;
    /** the url for the bookmark. */
    private String url = "";
    /** the title for the bookmark. */
    private String title = "";
    /** the tags for the bookmark. */
    private final Collection<String> tags = new ArrayList<>();

// -------------------------- STATIC METHODS --------------------------

    /**
     * returns a BookmarkBuilder instance.
     *
     * @return a BookmarkBuilder
     */
    public static BookmarkBuilder aBookmark() {
        return new BookmarkBuilder();
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * adds a tag to the bookmark.
     *
     * @param tag
     *         the tag to add
     * @return this object
     */
    public BookmarkBuilder addTag(final String tag) {
        tags.add(tag);
        return this;
    }

    /**
     * builds the Bookmark object.
     *
     * @return the bookmark object
     */
    public Bookmark build() {
        Bookmark bookmark = new Bookmark();
        bookmark.setId(id);
        bookmark.setUrl(url);
        bookmark.setTitle(title);
        tags.stream().forEach(bookmark::addTag);
        return bookmark;
    }

    /**
     * sets the id of the bookmark.
     *
     * @param id
     *         new id
     * @return this object
     */
    public BookmarkBuilder withId(final String id) {
        this.id = id;
        return this;
    }

    /**
     * sets the title for the bookmark.
     *
     * @param title
     *         the title
     * @return this object
     */
    public BookmarkBuilder withTitle(final String title) {
        this.title = title;
        return this;
    }

    /**
     * sets the url for the Bookmark.
     *
     * @param url
     *         the url
     * @return this object
     */
    public BookmarkBuilder withUrl(final String url) {
        this.url = url;
        return this;
    }
}
