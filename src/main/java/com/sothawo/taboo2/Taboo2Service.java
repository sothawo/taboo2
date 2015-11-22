/*
 Copyright 2015 Peter-Josef Meisch (pj.meisch@sothawo.com)

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
*/
package com.sothawo.taboo2;

import com.sothawo.taboo2.repository.BookmarkRepository;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;

/**
 * Spring-Boot Service implementation for the taboo backend service.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@RestController // contains @ResponseBody
@RequestMapping(value = Taboo2Service.MAPPING_TABOO2, produces = "application/json")
public class Taboo2Service {
// ------------------------------ FIELDS ------------------------------

    /** Logger for the class. */
    private final static Logger LOG = LoggerFactory.getLogger(Taboo2Service.class);

    /** Mapping for the class, package scope for test class. */
    static final String MAPPING_TABOO2 = "/taboo2";

    /** Mapping for the tags call, package scope for test class. */
    static final String MAPPING_TAGS = "/tags";

    /** Mapping for the bookmarks call, package scope for test class. */
    static final String MAPPING_BOOKMARKS = "/bookmarks";

    /** Mapping for title call, package scope for test class. */
    static final String MAPPING_TITLE = "/title";

    /** Mapping for check call, package scope for test class. */
    static final String MAPPING_CHECK = "/check";

    /** dumping all bookmarks without ids. */
    public static final String MAPPING_DUMP_BOOKMARKS = "/dump";

    /** needed for tests. */
    static final String MAGIC_TEST_URL = "magicTestStringThatsNotAnUrl";

    /** OR operation. */
    private static final String OP_OR = "or";
    /** AND operation. */
    private static final String OP_AND = "and";

    /**
     * user agent that jsoup sends when fetching the page title. Some sites send 403, when no known user agent is
     * sent).
     */
    private static final String JSOUP_USER_AGENT =
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    /** configuration object. */
    @Autowired
    private Taboo2Configuration taboo2Config;

    /** backend repository for the bookmarks. */
    @Autowired
    private BookmarkRepository repository;

// -------------------------- OTHER METHODS --------------------------

    /**
     * simple method that returns Status OK.
     */
    @RequestMapping(value = MAPPING_CHECK, method = RequestMethod.GET)
    public final ResponseEntity check() {
        LOG.info("check called");
        return new ResponseEntity(HttpStatus.OK);
    }

    @PreDestroy
    public void close() {
        try {
            repository.close();
        } catch (Exception e) {
            LOG.warn("closing repository", e);
        }
    }

    /**
     * creates a new bookmark in the repository.
     *
     * @param bookmark
     *         new bookmark to be created
     * @param ucb
     *         uri component builder to build the created uri
     * @return the created bookmark
     * @throws IllegalArgumentException
     *         when bookmark is null or has it's id set or one of the tags is an empty string
     */
    @RequestMapping(value = MAPPING_BOOKMARKS, method = RequestMethod.POST)
    public final ResponseEntity<Bookmark> createBookmark(@RequestBody final Bookmark bookmark,
                                                         final UriComponentsBuilder ucb) {
        if (null == bookmark) {
            throw new IllegalArgumentException("bookmark must not be null");
        }
        if (null != bookmark.getId()) {
            throw new IllegalArgumentException("id must not be set");
        }
        if (bookmark.getTags().stream().filter(String::isEmpty).findFirst().isPresent()) {
            throw new IllegalArgumentException("tags must not be empty");
        }

        Bookmark createdBookmark = repository.createBookmark(bookmark);
        HttpHeaders headers = new HttpHeaders();
        URI locationUri = ucb
                .path(MAPPING_TABOO2 + MAPPING_BOOKMARKS + '/')
                .path(String.valueOf(createdBookmark.getId()))
                .build().toUri();
        headers.setLocation(locationUri);
        LOG.info("created bookmark {}", createdBookmark);
        return new ResponseEntity<>(createdBookmark, headers, HttpStatus.CREATED);
    }

    /**
     * deletes a bookmark from the repository.
     *
     * @param id
     *         id of the bookmark to delete
     * @throws NotFoundException
     *         when no Bookmarks is found for the id
     */
    @RequestMapping(value = MAPPING_BOOKMARKS + "/{id}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public final void deleteBookmarkById(@PathVariable(value = "id") final String id) {
        repository.deleteBookmark(id);
        LOG.info("deleted bookmark with id {}", id);
    }

    /**
     * dumps all the bookmarks without having their id set.
     *
     * @return bookmarks
     */
    @RequestMapping(value = MAPPING_DUMP_BOOKMARKS, method = RequestMethod.GET)
    public final Collection<Bookmark> dumpBookmarks() {
        return repository.dumpBookmarks();
    }

    /**
     * ExceptionHandler for AlreadyExistsException. returns the exception's error message in the body with the 409
     * status code.
     *
     * @param e
     *         the exception to handle
     * @return HTTP CONFLICT Response Status and error message
     */
    @ExceptionHandler(AlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public final String exceptionHandlerAlreadyExistsException(final AlreadyExistsException e) {
        return '"' + e.getMessage() + '"';
    }

    /**
     * ExceptionHandler for IllegalArgumentException. returns the exception's error message in the body with the 412
     * status code.
     *
     * @param e
     *         the exception to handle
     * @return HTTP PRECONDITION_FAILED Response Status and error message
     */
    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    public final String exceptionHandlerIllegalArgumentException(final IllegalArgumentException e) {
        return '"' + e.getMessage() + '"';
    }

    /**
     * ExceptionHandler for NotFoundException. returns the exception's error message in the body with the 404 status
     * code.
     *
     * @param e
     *         the exception to handle
     * @return HTTP NOT_FOUND Response Status and error message
     */
    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public final String exceptionHandlerNotFoundException(final NotFoundException e) {
        return '"' + e.getMessage() + '"';
    }

    /**
     * gets the bookmarks from the repository. There can be additional selection citeria, either tags or a search search
     * string.
     *
     * @param tags
     *         optional list of tags
     * @param op
     *         if "or", tags are combined with OR, otherwise with AND
     * @param search
     *         optional search string to be searched
     * @return all bookmarks
     */
    @RequestMapping(value = MAPPING_BOOKMARKS, method = RequestMethod.GET)
    public final Collection<Bookmark> findAllBookmarks(@RequestParam(value = "tag", required = false)
                                                       final List<String> tags,
                                                       @RequestParam(value = "op", defaultValue = OP_AND)
                                                       final String op,
                                                       @RequestParam(value = "search", required = false)
                                                       final String search) {
        boolean opAnd = !OP_OR.equals(op.toLowerCase());
        if (null == tags && null == search) {
            return repository.getAllBookmarks();
        } else if (null == search) {
            // only tags
            return repository.getBookmarksWithTags(tags, opAnd);
        } else if (null == tags) {
            // only search
            return repository.getBookmarksWithSearch(search);
        } else {
            return repository.getBookmarksWithTagsAndSearch(tags, opAnd, search);
        }
    }

    /**
     * return all tags from the repository.
     *
     * @return collection of tags
     */
    @RequestMapping(value = MAPPING_TAGS, method = RequestMethod.GET)
    public final Collection<String> findAlltags() {
        return repository.getAllTags().stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * returns the bookmark with the given id.
     *
     * @param id
     *         id of the bookmark
     * @return Bookmark
     * @throws NotFoundException
     *         when no Bookmarks is found for the id
     */
    @RequestMapping(value = MAPPING_BOOKMARKS + "/{id}", method = RequestMethod.GET)
    public final Bookmark findBookmarkById(@PathVariable(value = "id") final String id) {
        return repository.getBookmarkById(id);
    }

    /**
     * tries to load the title for a web page.
     *
     * @param url
     *         url for which the title shall be loaded
     * @return ResponseEntity with the title
     */
    @RequestMapping(value = MAPPING_TITLE, method = RequestMethod.GET)
    @ResponseBody
    public final ResponseEntity<Bookmark> loadTitle(@RequestParam(value = "url", required = true) final String url) {
        if (MAGIC_TEST_URL.equals(url)) {
            return new ResponseEntity<>(aBookmark().withUrl(url).build(), HttpStatus.OK);
        }

        String urlString = url;
        if (null != urlString && !urlString.isEmpty()) {
            if (!urlString.startsWith("http")) {
                urlString = "http://" + urlString;
            }
            final String finalUrl = urlString;
            LOG.info("loading title for url {}", finalUrl);
            try {
                String htmlTitle = Jsoup
                        .connect(finalUrl)
                        .timeout(5000)
                        .userAgent(JSOUP_USER_AGENT)
                        .get()
                        .title();
                LOG.info("got title: {}", htmlTitle);
                return new ResponseEntity<>(aBookmark().withUrl(finalUrl).withTitle(htmlTitle).build(), HttpStatus.OK);
            } catch (HttpStatusException e) {
                LOG.info("loading url http error", e);
                return new ResponseEntity<>(HttpStatus.valueOf(e.getStatusCode()));
            } catch (IOException e) {
                LOG.info("loading url error", e);
            }
        }
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostConstruct
    public void logInfoToDebug() {
        LOG.debug("taboo2.info={}", taboo2Config.getInfo());
        LOG.debug("bookmark repository implementation: {}", (null == repository) ? "null" : repository.getClass()
                .getCanonicalName());
    }

    /**
     * updates a bookmark in the repository.
     *
     * @param bookmark
     *         bookmark to be updated
     * @throws IllegalArgumentException
     *         when bookmarkis null or doesnt have it's id set
     */
    @RequestMapping(value = MAPPING_BOOKMARKS, method = RequestMethod.PUT)
    public final void updateBookmark(@RequestBody final Bookmark bookmark) {
        if (null == bookmark) {
            throw new IllegalArgumentException("bookmark must not be null");
        }
        if (null == bookmark.getId()) {
            throw new IllegalArgumentException("id must be set");
        }
        repository.updateBookmark(bookmark);
        LOG.info("updated bookmark {}", bookmark);
    }
}
