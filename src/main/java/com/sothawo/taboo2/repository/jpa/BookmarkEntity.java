/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Bookmark entity.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Entity
@Table(name = "BOOKMARK")
@NamedQueries({
        @NamedQuery(name = BookmarkEntity.BOOKMARK_BY_URL,
                query = "select b from BookmarkEntity b where b.url = :url"),
        @NamedQuery(name = BookmarkEntity.ALL_BOOKMARKS,
                query = "select b from BookmarkEntity b"),
        @NamedQuery(name = BookmarkEntity.BOOKMARKS_WITH_TITLE,
                query = "select b from BookmarkEntity b where lower(b.title) like lower(:s)")
})
public class BookmarkEntity implements Serializable {
// ------------------------------ FIELDS ------------------------------

    public final static String BOOKMARK_BY_URL = "BookmarkEntity.bookmarkByUrl";
    public final static String ALL_BOOKMARKS = "BookmarkEntity.allBookmarks";
    public final static String BOOKMARKS_WITH_TITLE = "BookmarkEntity.bookmarksWithTitle";

    /** db id. */
    private Long id;

    /** the bookmark's url. */
    private String url;

    /** the bookmark's title. */
    private String title;

    /** the booksmark's tag. */
    private Set<TagEntity> tags = new HashSet<>();

// --------------------- GETTER / SETTER METHODS ---------------------

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "BOOKMARK_TAG",
            joinColumns = {@JoinColumn(name = "BOOKMARK_ID")},
            inverseJoinColumns = {@JoinColumn(name = "TAG_ID")}
    )
    public Set<TagEntity> getTags() {
        return tags;
    }

    @Column(name = "URL", length = 512)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Column(name = "TITLE", length = 512)
    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookmarkEntity that = (BookmarkEntity) o;
        return Objects.equals(url, that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(url);
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * add a tag and set up the entity connections.
     *
     * @param tagEntity
     *         TagEntity to add
     */
    public void addTag(TagEntity tagEntity) {
        if (null != tagEntity) {
            tagEntity.getBookmarks().add(this);
            getTags().add(tagEntity);
        }
    }

    /**
     * remove a tag and set up the entity connections.
     *
     * @param tagEntity
     *         TagEntity to remove
     */
    public void removeTag(TagEntity tagEntity) {
        if (null != tagEntity) {
            tagEntity.getBookmarks().remove(this);
            getTags().remove(tagEntity);
        }
    }

    public void setTags(Set<TagEntity> tags) {
        this.tags = null != tags ? tags : new HashSet<>();
    }
}
