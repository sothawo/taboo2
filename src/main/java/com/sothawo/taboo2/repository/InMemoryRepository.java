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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * InMemory BookmarkRepository implementation. Just offers functionality, no performance.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class InMemoryRepository extends AbstractBookmarkRepository {
// ------------------------------ FIELDS ------------------------------

    /** id generator */
    private static final AtomicInteger nextId = new AtomicInteger(1);

    /** map for storing the url -> bookmarks */
    private final Map<String, Bookmark> bookmarks = new HashMap<>();

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AutoCloseable ---------------------

    @Override
    public void close() throws Exception {
    }

// --------------------- Interface BookmarkRepository ---------------------

    @Override
    public Bookmark createBookmark(Bookmark bookmark) {
        if (null == bookmark) {
            throw new IllegalArgumentException("bookmark is null");
        }
        if (null != bookmark.getId()) {
            throw new IllegalArgumentException("is is not null");
        }
        if (null == bookmark.getUrl() || bookmark.getUrl().isEmpty()) {
            throw new IllegalArgumentException("bookmark url is not set");
        }
        if (bookmarks.containsKey(bookmark.getUrl())) {
            throw new AlreadyExistsException("bookmark with url: " + bookmark.getUrl());
        }

        bookmark.setId(String.valueOf(nextId.getAndIncrement()));
        bookmarks.put(bookmark.getUrl(), bookmark.clone());
        return bookmark;
    }

    /**
     * deletes the bookmark with the given id
     *
     * @param id
     *         id of the bookmark to delete
     * @throws NotFoundException
     *         if no bookmark is found for the given id
     */
    @Override
    public void deleteBookmark(String id) {
        Bookmark bookmark = getBookmarkById(id);
        String url = bookmark.getUrl();
        bookmarks.remove(url);
    }

    @Override
    public Collection<Bookmark> getAllBookmarks() {
        return bookmarks.values();
    }

    @Override
    public Collection<String> getAllTags() {
        return bookmarks.values().stream().flatMap(bookmark -> bookmark.getTags().stream())
                .collect(Collectors.toSet());
    }

    @Override
    public Bookmark getBookmarkById(String id) {
        String idToSearch = Objects.requireNonNull(id);
        return bookmarks.values().stream()
                .filter(b -> b.getId().equals(idToSearch))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("no bookmark with id " + id));
    }

    /**
     * returns the bookmarks where the title contains the given string. The search must be case insensitive.
     *
     * @param s
     *         the substring to search
     * @return the found bookmarks
     */
    @Override
    public Collection<Bookmark> getBookmarksWithSearch(String s) {
        final String titleToSearch = Objects.requireNonNull(s).toLowerCase();
        return bookmarks.values().stream()
                .filter(bookmark -> Objects.nonNull(bookmark.getTitle()))
                .filter(bookmark -> bookmark.getTitle().toLowerCase().contains(titleToSearch))
                .collect(Collectors.toList());
    }

    /**
     * removes all bookmarks from the reository.
     */
    @Override
    public void purge() {
        bookmarks.clear();
    }

    @Override
    public void updateBookmark(Bookmark bookmark) {
        String id = Objects.requireNonNull(bookmark).getId();
        if (null == id || id.isEmpty()) {
            throw new IllegalArgumentException();
        }
        // check if there is no different bookmark with this url
        Bookmark found = bookmarks.get(bookmark.getUrl());
        if (null != found && !id.equals(found.getId())) {
            throw new AlreadyExistsException("bookmark with url " + bookmark.getUrl());
        }
        deleteBookmark(id);
        bookmarks.put(bookmark.getUrl(), bookmark);
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * get all the bookmarks that have a given tag.
     *
     * @param tag
     *         the tag
     * @return a Set of Bookmarks, may be empty not null
     */
    protected Set<Bookmark> getBookmarksWithTag(String tag) {
        final Set<Bookmark> foundBookmarks = new HashSet<>();
        bookmarks.values()
                .stream()
                .filter(bookmark -> bookmark.getTags().contains(tag))
                .forEach(foundBookmarks::add);
        return foundBookmarks;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Factory class implementation.
     */
    public static class Factory implements BookmarkRepositoryFactory {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BookmarkRepositoryFactory ---------------------

        @Override
        public BookmarkRepository create(String[] args) {
            return new InMemoryRepository();
        }
    }
}
