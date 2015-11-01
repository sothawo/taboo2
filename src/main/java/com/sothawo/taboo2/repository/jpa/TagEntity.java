/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Tag entity.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Entity
@Table(name = "TAG")
@NamedQueries({
        @NamedQuery(name = TagEntity.FIND_BY_TAG, query = "select t from TagEntity t where t.tag = :tag"),
        @NamedQuery(name = TagEntity.ALL_TAGS, query = "select t from TagEntity t")
})
public class TagEntity implements Serializable {
// ------------------------------ FIELDS ------------------------------

    public static final String FIND_BY_TAG = "TagEntity.findByTag";
    public static final String ALL_TAGS = "TagEntity.allTags";

    /** id of the entity. */
    private Long id;

    /** the actual tag value. */
    private String tag;

    /** the bookmarks for the tag */
    private Set<BookmarkEntity> bookmarks = new HashSet<>();

// --------------------- GETTER / SETTER METHODS ---------------------

    @ManyToMany(mappedBy = "tags")
    public Set<BookmarkEntity> getBookmarks() {
        return bookmarks;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", unique = true)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String name) {
        this.tag = name;
    }

// ------------------------ CANONICAL METHODS ------------------------

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TagEntity tagEntity = (TagEntity) o;
        return Objects.equals(tag, tagEntity.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tag);
    }

// -------------------------- OTHER METHODS --------------------------

    public void setBookmarks(Set<BookmarkEntity> bookmarks) {
        this.bookmarks = null != bookmarks ? bookmarks : new HashSet<>();
    }
}
