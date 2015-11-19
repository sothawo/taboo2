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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sothawo.taboo2.repository.BookmarkRepository;
import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Verifications;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;
import static org.hamcrest.CoreMatchers.endsWith;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Tests for the Taboo2Service class.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@SuppressWarnings("SpringJavaAutowiredMembersInspection")
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Taboo2Application.class)
@WebAppConfiguration
@ActiveProfiles({"test", "repo-mocked"})
public class Taboo2ServiceTests {
// ------------------------------ FIELDS ------------------------------

    // couple of convenience constants
    private final static String TABOO2_BOOKMARKS = Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_BOOKMARKS;
    private final static String TABOO2_DUMP_BOOKMARKS = Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_DUMP_BOOKMARKS;
    private final static String TABOO2_TAGS = Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_TAGS;
    private final static String TABOO2_TITLE = Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_TITLE;

    /**
     * The service to test. Cannot be created with @Tested because we need internal spring DI resolution.
     */
    @Autowired
    private Taboo2Service taboo2Service;

    /** a mocked bookmark repository. */
    @Injectable
    private BookmarkRepository repository;

// -------------------------- OTHER METHODS --------------------------

    private byte[] convertObjectToJsonBytes(Object o) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper.writeValueAsBytes(o);
    }

    @Test
    public void createBookmark() throws Exception {
        Bookmark bookmarkIn = aBookmark().withUrl("url").withTitle("title").addTag("tag").build();
        Bookmark bookmarkOut = aBookmark().withId("11").withUrl("url").withTitle("title").addTag("tag").build();

        new Expectations() {{
            repository.createBookmark(bookmarkIn);
            result = bookmarkOut;
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(post(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmarkIn)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", endsWith(TABOO2_BOOKMARKS + "/11")))
                .andExpect(jsonPath("$.id", is(bookmarkOut.getId())))
                .andExpect(jsonPath("$.url", is(bookmarkOut.getUrl())))
                .andExpect(jsonPath("$.title", is(bookmarkOut.getTitle())))
        ;
        new Verifications() {{
            repository.createBookmark(bookmarkIn);
            times = 1;
        }};
    }

    @Test

    public void createBookmarksWithEmptyTagYieldsPreconditionFailed() throws Exception {
        Bookmark bookmarkIn = aBookmark().withUrl("url").withTitle("title").addTag("tag").addTag("").
                build();

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(post(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmarkIn)))
                .andExpect(status().isPreconditionFailed())
        ;

        new Verifications() {{
            repository.createBookmark((Bookmark) any);
            times = 0;
        }};
    }

    @Test
    public void createBookmarksWithExistingUrlYieldsConflict() throws Exception {
        Bookmark bookmarkIn = aBookmark().withUrl("url").withTitle("title").addTag("tag").build();

        new Expectations() {{
            repository.createBookmark((Bookmark) any);
            result = new AlreadyExistsException("bookmark url");
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(post(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmarkIn)))
                .andExpect(status().isConflict())
        ;

        new Verifications() {{
            repository.createBookmark((Bookmark) any);
            times = 1;
        }};
    }

    @Test
    public void createBookmarksWithIdYieldsPreconditionFailed() throws Exception {
        Bookmark bookmarkIn = aBookmark().withId("11").withUrl("url").withTitle("title").addTag("tag").build();

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(post(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmarkIn)))
                .andExpect(status().isPreconditionFailed())
        ;

        new Verifications() {{
            repository.createBookmark((Bookmark) any);
            times = 0;
        }};
    }

    @Test
    public void createBookmarksWithoutData() throws Exception {
        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(post(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;

        new Verifications() {{
            repository.createBookmark((Bookmark) any);
            times = 0;
        }};
    }

    @Test
    public void deleteExistingBookmark() throws Exception {
        final String id = "42";
        new Expectations() {{
            repository.deleteBookmark(id);
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(delete(TABOO2_BOOKMARKS + "/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent())
        ;

        new Verifications() {{
            repository.deleteBookmark(id);
            times = 1;
        }};
    }

    @Test
    public void deleteNotExistingBookmark() throws Exception {
        final String id = "42";
        new Expectations() {{
            repository.deleteBookmark(id);
            result = new NotFoundException("");
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(delete(TABOO2_BOOKMARKS + "/{id}", id).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
        ;

        new Verifications() {{
            repository.deleteBookmark(id);
            times = 1;
        }};
    }

    @Test
    public void getAllBookmarks() throws Exception {
        List<Bookmark> bookmarks = createBookmarks("23", "42");

        new Expectations() {{
            repository.getAllBookmarks();
            result = bookmarks;
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookmarks.get(0).getId())))
                .andExpect(jsonPath("$[0].url", is(bookmarks.get(0).getUrl())))
                .andExpect(jsonPath("$[0].title", is(bookmarks.get(0).getTitle())))
                .andExpect(jsonPath("$[0].tags[0]", is(bookmarks.get(0).getTags().iterator().next())))
                .andExpect(jsonPath("$[1].id", is(bookmarks.get(1).getId())))
                .andExpect(jsonPath("$[1].url", is(bookmarks.get(1).getUrl())))
                .andExpect(jsonPath("$[1].title", is(bookmarks.get(1).getTitle())))
                .andExpect(jsonPath("$[1].tags[0]", is(bookmarks.get(1).getTags().iterator().next())))
        ;

        new Verifications() {{
            repository.getAllBookmarks();
            times = 1;
        }};
    }

    @Test
    public void getAllTags() throws Exception {
        String[] tags = new String[]{"tag1", "tag3", "tag2", "tag42"};

        new Expectations() {{
            repository.getAllTags();
            result = Arrays.asList(tags);
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_TAGS).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(4)))
                .andExpect(jsonPath("$[0]", is(tags[0])))
                .andExpect(jsonPath("$[1]", is(tags[1])))
                .andExpect(jsonPath("$[2]", is(tags[2])))
                .andExpect(jsonPath("$[3]", is(tags[3])))
        ;

        new Verifications() {{
            repository.getAllTags();
            times = 1;
        }};
    }

    @Test
    public void getBookmarksWithAllTags() throws Exception {
        Bookmark bookmark = createBookmarks("2").get(0);
        bookmark.addTag("abc");

        new Expectations() {{
            repository.getBookmarksWithTags(Arrays.asList("tag2", "abc"), true);
            result = Collections.singletonList(bookmark);
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS)
                .param("tag", "tag2", "abc")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookmark.getId()))) // id check is enough here
        ;

        new Verifications() {{
            repository.getBookmarksWithTags(Arrays.asList("tag2", "abc"), true);
            times = 1;
        }};
    }

    @Test
    public void getBookmarksWithAnyTag() throws Exception {
        List<Bookmark> bookmarks = createBookmarks("2", "3");
        bookmarks.get(0).addTag("abc");
        bookmarks.get(1).addTag("abc");

        new Expectations() {{
            repository.getBookmarksWithTags(Arrays.asList("tag2", "abc"), false);
            result = bookmarks;
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS)
                .param("tag", "tag2", "abc")
                .param("op", "or")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(bookmarks.get(0).getId()))) // id check is enough here
                .andExpect(jsonPath("$[1].id", is(bookmarks.get(1).getId())))
        ;

        new Verifications() {{
            repository.getBookmarksWithTags(Arrays.asList("tag2", "abc"), false);
            times = 1;
        }};
    }

    /**
     * helper method to create a list of bookmarks.
     *
     * @param ids
     *         id values for the Bookmark objects to create
     * @return a list of bookmarks
     */
    private List<Bookmark> createBookmarks(String... ids) {
        List<Bookmark> bookmarks = new ArrayList<>();
        for (String id : ids) {
            bookmarks
                    .add(aBookmark().withId(id).withUrl("url" + id).withTitle("title" + id).addTag("tag" + id).build());
        }
        return bookmarks;
    }

    @Test
    public void getBookmarksWithSearch() throws Exception {
        Bookmark bookmark = createBookmarks("1").get(0);

        new Expectations() {{
            repository.getBookmarksWithSearch("search1");
            result = Collections.singletonList(bookmark);
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS)
                .param("search", "search1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookmark.getId()))) // id check is enough here
        ;

        new Verifications() {{
            repository.getBookmarksWithSearch("search1");
            times = 1;
        }};
    }

    @Test
    public void getBookmarksWithTagAndSearch() throws Exception {
        Bookmark bookmark = createBookmarks("1").get(0);
        Collection<String> tags = Collections.singletonList("tag1");
        String search = "search1";

        new Expectations() {{
            repository.getBookmarksWithTagsAndSearch(tags, true, search);
            result = Collections.singletonList(bookmark);
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS)
                .param("search", "search1")
                .param("tag", "tag1")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(bookmark.getId()))) // id check is enough here
        ;

        new Verifications() {{
            repository.getBookmarksWithTagsAndSearch(tags, true, search);
            times = 1;
        }};
    }

    @Test
    public void getExistingBookmark() throws Exception {
        final Bookmark bookmark = createBookmarks("11").get(0);
        new Expectations() {{
            repository.getBookmarkById("11");
            result = bookmark;
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS + "/{id}", bookmark.getId()).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(bookmark.getId())))
                .andExpect(jsonPath("$.url", is(bookmark.getUrl())))
                .andExpect(jsonPath("$.tags[0]", is(bookmark.getTags().iterator().next())))
        ;

        new Verifications() {{
            repository.getBookmarkById("11");
            times = 1;
        }};
    }

    @Test
    public void getNotExistingBookmarkYieldsNotFound() throws Exception {
        new Expectations() {{
            repository.getBookmarkById("11");
            result = new NotFoundException("bookmark 11");
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_BOOKMARKS + "/{id}", 11).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
        ;

        new Verifications() {{
            repository.getBookmarkById("11");
            times = 1;
        }};
    }

    /**
     * set up the Service to be tested from the WebApplication context and replace the contained BookmarkRepository with
     * a mock. Must be called before each test, as JMockit creates the repository for each test call.
     */
    @Before
    public void setupTest() {
        Deencapsulation.setField(taboo2Service, repository);
        taboo2Service.logInfoToDebug();
    }

    @Test
    public void titleForBookmark() throws Exception {
        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_TITLE).param("url", Taboo2Service.MAGIC_TEST_URL))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url", is(Taboo2Service.MAGIC_TEST_URL)));
    }

    @Test
    public void updateBookmark() throws Exception {
        Bookmark bookmark = createBookmarks("1").get(0);
        bookmark.setId("id1");

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(put(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmark)))
                .andExpect(status().isOk())
        ;

        new Verifications() {{
            repository.updateBookmark(bookmark);
            times = 1;
        }};
    }

    @Test
    public void updateBookmarkToExistingUrl() throws Exception {
        Bookmark bookmark = createBookmarks("1").get(0);
        bookmark.setId("id1");
        new Expectations() {{
            repository.updateBookmark(bookmark);
            result = new AlreadyExistsException("bookmark exists " + bookmark.getUrl());
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(put(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmark)))
                .andExpect(status().isConflict())
        ;

        new Verifications() {{
            repository.updateBookmark(bookmark);
            times = 1;
        }};
    }

    @Test
    public void updateBookmarkWithNotExistingId() throws Exception {
        Bookmark bookmark = createBookmarks("1").get(0);
        bookmark.setId("id1");
        new Expectations() {{
            repository.updateBookmark(bookmark);
            result = new NotFoundException("bookmark not found " + bookmark.getUrl());
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(put(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmark)))
                .andExpect(status().isNotFound())
        ;

        new Verifications() {{
            repository.updateBookmark(bookmark);
            times = 1;
        }};
    }

    @Test
    public void updateBookmarkWithoutData() throws Exception {
        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(put(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
        ;

        new Verifications() {{
            repository.updateBookmark((Bookmark) any);
            times = 0;
        }};
    }

    @Test
    public void updateBookmarkWithoutId() throws Exception {
        Bookmark bookmarkIn = aBookmark().withUrl("url").withTitle("title").addTag("tag").build();

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(put(TABOO2_BOOKMARKS)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(convertObjectToJsonBytes(bookmarkIn)))
                .andExpect(status().isPreconditionFailed())
        ;

        new Verifications() {{
            repository.createBookmark((Bookmark) any);
            times = 0;
        }};
    }

    @Test
    public void dumpBookmarks() throws Exception {
        List<Bookmark> bookmarks = createBookmarks("0", "1", "2");
        // remove ids from bookmarks
        for (Bookmark bookmark : bookmarks) {
            bookmark.setId(null);
        }

        new Expectations() {{
            repository.dumpBookmarks();
            result = bookmarks;
        }};

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(TABOO2_DUMP_BOOKMARKS).accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.*", hasSize(3)))
                .andExpect(jsonPath("$[0].id", is(bookmarks.get(0).getId())))
                .andExpect(jsonPath("$[0].url", is(bookmarks.get(0).getUrl())))
                .andExpect(jsonPath("$[0].title", is(bookmarks.get(0).getTitle())))
                .andExpect(jsonPath("$[0].tags[0]", is(bookmarks.get(0).getTags().iterator().next())))
                .andExpect(jsonPath("$[1].id", is(bookmarks.get(1).getId())))
                .andExpect(jsonPath("$[1].url", is(bookmarks.get(1).getUrl())))
                .andExpect(jsonPath("$[1].title", is(bookmarks.get(1).getTitle())))
                .andExpect(jsonPath("$[1].tags[0]", is(bookmarks.get(1).getTags().iterator().next())))
                .andExpect(jsonPath("$[2].id", is(bookmarks.get(2).getId())))
                .andExpect(jsonPath("$[2].url", is(bookmarks.get(2).getUrl())))
                .andExpect(jsonPath("$[2].title", is(bookmarks.get(2).getTitle())))
                .andExpect(jsonPath("$[2].tags[0]", is(bookmarks.get(2).getTags().iterator().next())))
        ;

        new Verifications() {{
            repository.dumpBookmarks();
            times = 1;
        }};

    }
}
