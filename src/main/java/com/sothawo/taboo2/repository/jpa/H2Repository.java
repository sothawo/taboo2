/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.jpa;

import com.sothawo.taboo2.AlreadyExistsException;
import com.sothawo.taboo2.Bookmark;
import com.sothawo.taboo2.NotFoundException;
import com.sothawo.taboo2.repository.AbstractBookmarkRepository;
import com.sothawo.taboo2.repository.BookmarkRepository;
import com.sothawo.taboo2.repository.BookmarkRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;

/**
 * Repository implementation using a H2 database.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class H2Repository extends AbstractBookmarkRepository {
// ------------------------------ FIELDS ------------------------------

    /** name of the persistence unit. */
    private static final String PERSISTENCE_UNIT_NAME = "taboo2_pu";

    /** Logger. */
    private final static Logger LOG = LoggerFactory.getLogger(H2Repository.class);

    /** Entity Manager Factory, autocloseable variant */
    private EntityManagerFactoryAutoCloseable emf;

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
        emf = EntityManagerFactoryAutoCloseable.createFor(Persistence.createEntityManagerFactory
                (PERSISTENCE_UNIT_NAME, props));
    }

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface AutoCloseable ---------------------

    @Override
    public void close() {
        try {
            if (null != emf) {
                if (emf.isOpen()) {
                    emf.close();
                }
                emf = null;
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

        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            // search existing
            List resultList = em.createNamedQuery(BookmarkEntity.BOOKMARK_BY_URL).setParameter("url", bookmark.getUrl())
                    .getResultList();
            if (resultList.size() > 0) {
                throw new AlreadyExistsException(MessageFormat.format("bookmark with url {0} already exists.",
                        bookmark.getUrl()));
            }

            // insert new bookmark
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            BookmarkEntity bookmarkEntity = new BookmarkEntity();
            bookmarkEntity.setUrl(bookmark.getUrl());
            bookmarkEntity.setTitle(bookmark.getTitle());


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

            return bookmarkFromEntity(bookmarkEntity);
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on creating bookmark", e);
            return null;
        }
    }

    @Override
    public void deleteBookmark(String id) {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            Long bookmarkId = Long.valueOf(id);

            EntityTransaction tx = em.getTransaction();
            tx.begin();

            BookmarkEntity bookmarkEntity = em.find(BookmarkEntity.class, bookmarkId);
            if (null == bookmarkEntity) {
                throw new NotFoundException("no bookmark with id " + id);
            }

            for (TagEntity tagEntity : bookmarkEntity.getTags()) {
                Set<BookmarkEntity> tagBookmarks = tagEntity.getBookmarks();
                tagEntity.getBookmarks().remove(bookmarkEntity);
                if (tagBookmarks.size() == 0) {
                    em.remove(tagEntity);
                }
            }
            em.remove(bookmarkEntity);

            tx.commit();
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("non numeric id");
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on deleting bookmark", e);
        }
    }

    @Override
    public Collection<Bookmark> getAllBookmarks() {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            Set<Bookmark> bookmarks = em
                    .createNamedQuery(BookmarkEntity.ALL_BOOKMARKS, BookmarkEntity.class)
                    .getResultList()
                    .stream()
                    .map(this::bookmarkFromEntity)
                    .collect(Collectors.toSet());

            return bookmarks;
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting all bookmarks", e);
            return Collections.emptySet();
        }
    }

    @Override
    public Collection<String> getAllTags() {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            Set<String> tags = em
                    .createNamedQuery(TagEntity.ALL_TAGS, TagEntity.class)
                    .getResultList()
                    .stream()
                    .map(TagEntity::getTag)
                    .collect(Collectors.toSet());
            return tags;
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting all tags", e);
            return Collections.emptySet();
        }
    }

    @Override
    public Bookmark getBookmarkById(String id) {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            Optional<Bookmark> bookmarkOptional =
                    Optional.ofNullable(em.find(BookmarkEntity.class, Long.valueOf(id)))
                            .map(this::bookmarkFromEntity);
            return bookmarkOptional.orElseThrow(() -> new NotFoundException("no bookmark with id " + id));
        } catch (Exception e) {
            throw new NotFoundException("no bookmark with id " + id, e);
        }
    }

    @Override
    public Collection<Bookmark> getBookmarksWithSearch(String s) {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            if (null == s || s.isEmpty()) {
                throw new IllegalArgumentException("empty search string");
            }
            Set<Bookmark> bookmarks = em
                    .createNamedQuery(BookmarkEntity.BOOKMARKS_WITH_TITLE, BookmarkEntity.class)
                    .setParameter("s", '%' + s + '%')
                    .getResultList()
                    .stream()
                    .map(this::bookmarkFromEntity)
                    .collect(Collectors.toSet());
            return bookmarks;
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting bookmarks with search string", e);
            return Collections.emptySet();
        }
    }

    @Override
    public void purge() {
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            em.createQuery("delete from BookmarkEntity b").executeUpdate();
            em.createQuery("delete from TagEntity t").executeUpdate();

            tx.commit();
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on purging data", e);
        }
    }

    @Override
    public void updateBookmark(Bookmark bookmark) {
        if (null == bookmark.getId() || null == bookmark.getUrl() || bookmark.getUrl().isEmpty()) {
            throw new IllegalArgumentException();
        }

        Long updateBookmarkId;
        try {
            updateBookmarkId = Long.valueOf(bookmark.getId());
        } catch (NumberFormatException e) {
            throw new NotFoundException("no bookmark with id " + bookmark.getId());
        }

        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            EntityTransaction tx = em.getTransaction();
            tx.begin();

            // check if new URL exists on different entity
            try {
                BookmarkEntity existingBookmarkEntity =
                        em.createNamedQuery(BookmarkEntity.BOOKMARK_BY_URL, BookmarkEntity.class)
                                .setParameter("url", bookmark.getUrl())
                                .getSingleResult();
                if (!existingBookmarkEntity.getId().equals(updateBookmarkId)) {
                    tx.rollback();
                    throw new AlreadyExistsException(MessageFormat.format("new url {0} already bookmarked",
                            bookmark.getUrl()));
                }
            } catch (NoResultException ignored) {
                // ignore
            }

            BookmarkEntity bookmarkEntity = em.find(BookmarkEntity.class, updateBookmarkId);
            if (null == bookmarkEntity) {
                throw new NotFoundException("no bookmark with id " + updateBookmarkId);
            }
            bookmarkEntity.setUrl(bookmark.getUrl());
            bookmarkEntity.setTitle(bookmark.getTitle());

            // keep the old tags
            Set<TagEntity> previousTagEntities = new HashSet<>(bookmarkEntity.getTags());

            // build the new TagEntities
            TypedQuery<TagEntity> findTagQuery =
                    em.createNamedQuery(TagEntity.FIND_BY_TAG, TagEntity.class);
            for (String tag : bookmark.getTags()) {
                TagEntity tagEntity;
                try {
                    tagEntity = findTagQuery.setParameter("tag", tag).getSingleResult();
                } catch (NoResultException ignored) {
                    // new tag
                    tagEntity = new TagEntity();
                    tagEntity.setTag(tag);
                }
                if (!bookmarkEntity.getTags().contains(tagEntity)) {
                    bookmarkEntity.addTag(tagEntity);
                }
                if (null == tagEntity.getId()) {
                    em.persist(tagEntity);
                }
            }

            // check the old set for tags that are not contained anymore
            previousTagEntities
                    .stream()
                    .filter(tagEntity -> !bookmark.getTags().contains(tagEntity.getTag()))
                    .forEach(tagEntity -> {
                        bookmarkEntity.removeTag(tagEntity);
                        if (tagEntity.getBookmarks().isEmpty()) {
                            em.remove(tagEntity);
                        }
                    });

            em.merge(bookmarkEntity);

            tx.commit();
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on updating bookmark", e);
        }
    }

// -------------------------- OTHER METHODS --------------------------

    /**
     * converts a BookmarkEntity to a Bookmark
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

    @Override
    protected Set<Bookmark> getBookmarksWithTag(String tag) {
        Set<Bookmark> bookmarks = new HashSet<>();
        try (EntityManagerAutoCloseable em = (EntityManagerAutoCloseable) emf.createEntityManager()) {
            em.createNamedQuery(TagEntity.FIND_BY_TAG, TagEntity.class)
                    .setParameter("tag", tag)
                    .getSingleResult()
                    .getBookmarks()
                    .stream()
                    .map(this::bookmarkFromEntity)
                    .forEach(bookmarks::add);
        } catch (NoResultException ignored) {
            // ignore
        } catch (IllegalStateException | IllegalArgumentException | PersistenceException e) {
            LOG.error("db error on getting tag", e);
        }
        return bookmarks;
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
