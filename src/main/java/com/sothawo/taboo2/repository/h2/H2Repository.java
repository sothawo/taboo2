/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.h2;

import com.sothawo.taboo2.AlreadyExistsException;
import com.sothawo.taboo2.Bookmark;
import com.sothawo.taboo2.repository.BookmarkRepository;
import com.sothawo.taboo2.repository.BookmarkRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.NoResultException;
import javax.persistence.Persistence;
import javax.persistence.PersistenceException;
import javax.persistence.TypedQuery;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;

/**
 * Repository implementation using a H2 database.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class H2Repository implements BookmarkRepository {
// ------------------------------ FIELDS ------------------------------

    /** name of the persistence unit. */
    private static final String PERSISTENCE_UNIT_NAME = "taboo2_pu";

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(H2Repository.class);

    /** Entity Manager Factory */
    private final EntityManagerFactory emf;

// -------------------------- STATIC METHODS --------------------------

    static {
        fixDeprecatedHibernateWarning();
    }

    /**
     * fixes the warning (Encountered a deprecated javax.persistence.spi.PersistenceProvider [org.hibernate.ejb
     * .HibernatePersistence]; use [org.hibernate.jpa.HibernatePersistenceProvider] instead.)
     *
     * must be called before getting the EntityManagerFactory
     */
    private static void fixDeprecatedHibernateWarning() {
        PersistenceProviderResolverHolder.setPersistenceProviderResolver(new PersistenceProviderResolver() {
            private final List<PersistenceProvider> providers_ =
                    Arrays.asList(new PersistenceProvider[]{new org.hibernate.jpa.HibernatePersistenceProvider()});

            @Override
            public List<PersistenceProvider> getPersistenceProviders() {
                return providers_;
            }

            @Override
            public void clearCachedProviders() {

            }
        });
    }

// --------------------------- CONSTRUCTORS ---------------------------

    public H2Repository(String jdbcUrl) {
        LOG.info("configured jdbc url: {}", jdbcUrl);
        Map<String, String> props = new HashMap<>();
        props.put("hibernate.connection.url", jdbcUrl);
        emf = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, props);
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AutoCloseable ---------------------

    @Override
    public void close() {
        try {
            if (null != emf) {
                emf.close();
            }
        } catch (RuntimeException e) {
            LOG.warn("DB", e);
        }
    }

// --------------------- Interface BookmarkRepository ---------------------

    @Override
    public Bookmark createBookmark(Bookmark bookmark) {
        // check arguments
        if (null != bookmark.getId() || null == bookmark.getUrl() || bookmark.getUrl().isEmpty()) {
            throw new IllegalArgumentException();
        }

        try {
            EntityManager em = emf.createEntityManager();

            // search existing
            List resultList = em.createNamedQuery(BookmarkEntity.BOOKMARK_BY_URL).setParameter("url", bookmark.getUrl())
                    .getResultList();
            if (resultList.size() > 0) {
                throw new AlreadyExistsException(MessageFormat.format("bookmark with url '{0}' already exists.",
                        bookmark.getUrl()));
            }

            // insert new bookmark
            EntityTransaction tx = em.getTransaction();

            BookmarkEntity bookmarkEntity = new BookmarkEntity();
            bookmarkEntity.setUrl(bookmark.getUrl());
            bookmarkEntity.setTitle(bookmark.getTitle());

            tx.begin();

            // build the TagEntities
            TypedQuery<TagEntity> findTagQuery =
                    em.createNamedQuery(TagEntity.FIND_BY_TAG, TagEntity.class);
            for (String tag : bookmark.getTags()) {
                TagEntity tagEntity;
                try {
                    tagEntity = findTagQuery.setParameter("tag", tag).getSingleResult();
                } catch (NoResultException ignored) {
                    tagEntity = new TagEntity();
                    tagEntity.setTag(tag);
                }
                bookmarkEntity.addTag(tagEntity);
                if (null == tagEntity.getId()) {
                    em.persist(tagEntity);
                }
            }

            em.persist(bookmarkEntity);
            em.flush();


            tx.commit();
            em.close();

            return bookmarkFromEntity(bookmarkEntity);
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on creating bookmark", e);
            return null;
        }
    }

    @Override
    public void deleteBookmark(String id) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<Bookmark> getAllBookmarks() {
        try {
            return emf
                    .createEntityManager()
                    .createNamedQuery(BookmarkEntity.ALL_BOOKMARKS, BookmarkEntity.class)
                    .getResultList()
                    .stream()
                    .map(this::bookmarkFromEntity)
                    .collect(Collectors.toSet());
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting all bookmarks", e);
            return Collections.emptySet();
        }
    }

    @Override
    public Collection<String> getAllTags() {
        try {
            return emf
                    .createEntityManager()
                    .createNamedQuery(TagEntity.ALL_TAGS, TagEntity.class)
                    .getResultList()
                    .stream()
                    .map(TagEntity::getTag)
                    .collect(Collectors.toSet());
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting all tags", e);
            return Collections.emptySet();
        }
    }

    @Override
    public Bookmark getBookmarkById(String id) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<Bookmark> getBookmarksWithSearch(String s) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<Bookmark> getBookmarksWithTags(Collection<String> tags, boolean opAnd) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<Bookmark> getBookmarksWithTagsAndSearch(Collection<String> tags, boolean opAnd, String s) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public void purge() {
        try {
            EntityManager em = emf.createEntityManager();
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createQuery("delete from BookmarkEntity b").executeUpdate();
            em.createQuery("delete from TagEntity t").executeUpdate();

            tx.commit();
            em.close();
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on purging data", e);
        }
    }

    @Override
    public void updateBookmark(Bookmark bookmark) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * converts a BokmarkEntity to a Bookmark
     *
     * @param entity
     *         the entity to convert
     * @return the converted Bookmark
     */
    private Bookmark bookmarkFromEntity(BookmarkEntity entity) {
        Bookmark createdBookmark = aBookmark()
                .withId(String.valueOf(entity.getId()))
                .withUrl(entity.getUrl())
                .withTitle(entity.getTitle())
                .build();
        for (TagEntity tagEntity : entity.getTags()) {
            createdBookmark.addTag(tagEntity.getTag());
        }
        return createdBookmark;
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Factory implementation.
     */
    public static class Factory implements BookmarkRepositoryFactory {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BookmarkRepositoryFactory ---------------------

        /**
         * first argument is jdbcUrl
         *
         * @param args
         *         arguments for the BookmarkRepository
         * @return
         */
        @Override
        public BookmarkRepository create(String[] args) {
            if (null == args || args.length < 1) {
                throw new IllegalArgumentException();
            }

            final String jdbcUrl = args[0];
            return new H2Repository(jdbcUrl);
        }
    }
}
