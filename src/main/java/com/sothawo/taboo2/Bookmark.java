/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Objects;

/**
 * The bookmark POJO. Tags when added are converted to lowercase and duplicate tags are removed. A Tag has a unique id,
 * which is assigned by the repository, a title, which normally is taken from the website's title, a URL, which is
 * unique and a collection of tags.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public final class Bookmark {
// ------------------------------ FIELDS ------------------------------

    /** the id of the bookmark. */
    private String id;
    /** the title of a bookmark. */
    private String title = "";
    /** the URL the bookmark points to as String. */
    private String url = "";
    /** the tags of the bookmark. */
    private final Collection<String> tags = new HashSet<>();

    @Override
    public String toString() {
        return "Bookmark{" +
                "id='" + id + '\'' +
                ", title='" + title + '\'' +
                ", url='" + url + '\'' +
                ", tags=" + tags +
                '}';
    }
// --------------------- GETTER / SETTER METHODS ---------------------

    /**
     * gets the id.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * sets the id.
     *
     * @param id
     *         new id
     */
    public void setId(final String id) {
        this.id = id;
    }

    /**
     * gets the title.
     *
     * @return the title
     */
    public String getTitle() {
        return title;
    }

    /**
     * setes the title.
     *
     * @param title
     *         new title
     */
    public void setTitle(final String title) {
        this.title = title;
    }

    /**
     * gets the url.
     *
     * @return the url
     */
    public String getUrl() {
        return url;
    }

// ------------------------ CANONICAL METHODS ------------------------

    /**
     * creates a clone. for implementations that need to return Bookmark objects that might be changed by the caller.
     *
     * @return a clone of this Bookmark.
     */
    public Bookmark clone() {
        Bookmark clone = new Bookmark();
        clone.setId(id);
        clone.setTitle(title);
        clone.setUrl(url);
        clone.tags.addAll(tags);
        return clone;
    }

    /**
     * sets the url.
     *
     * @param url
     *         new url
     * @throws NullPointerException
     *         when url is null
     */
    public void setUrl(final String url) {
        this.url = Objects.requireNonNull(url);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Bookmark bookmark = (Bookmark) o;

        return !(url != null ? !url.equals(bookmark.url) : bookmark.url != null);
    }

    @Override
    public int hashCode() {
        return url != null ? url.hashCode() : 0;
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * adds the given tag in lowercase to the internal collection, if it is not already present.
     *
     * @param tag
     *         new tag
     * @throws NullPointerException
     *         when tag is null
     */
    public void addTag(final String tag) {
        tags.add(Objects.requireNonNull(tag).toLowerCase());
    }

    /**
     * clears all the tags.
     */
    public void clearTags() {
        tags.clear();
    }

    /**
     * returns an unmodifiable view of the tags collection.
     *
     * @return unmodifiable collection
     */
    public Collection<String> getTags() {
        return Collections.unmodifiableCollection(tags);
    }
}
