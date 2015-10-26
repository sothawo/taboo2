/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2.repository.h2;

import com.sothawo.taboo2.Bookmark;
import com.sothawo.taboo2.repository.BookmarkRepository;
import com.sothawo.taboo2.repository.BookmarkRepositoryFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.spi.PersistenceProvider;
import javax.persistence.spi.PersistenceProviderResolver;
import javax.persistence.spi.PersistenceProviderResolverHolder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public void deleteBookmark(String id) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<Bookmark> getAllBookmarks() {
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public Collection<String> getAllTags() {
        throw new UnsupportedOperationException("not yet implemented.");
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
        throw new UnsupportedOperationException("not yet implemented.");
    }

    @Override
    public void updateBookmark(Bookmark bookmark) {
        throw new UnsupportedOperationException("not yet implemented.");
    }

// -------------------------- INNER CLASSES --------------------------

    /**
     * Factory implementation.
     */
    public static class Factory implements BookmarkRepositoryFactory {
// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface BookmarkRepositoryFactory ---------------------

        @Override
        public BookmarkRepository create(String[] args) {
            if (null == args || args.length < 1) {
                throw new IllegalArgumentException();
            }
            return new H2Repository(args[0]);
        }
    }
}
