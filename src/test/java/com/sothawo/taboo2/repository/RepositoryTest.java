package com.sothawo.taboo2.repository;

import com.sothawo.taboo2.AlreadyExistsException;
import com.sothawo.taboo2.Bookmark;
import com.sothawo.taboo2.NotFoundException;
import com.sothawo.taboo2.repository.jpa.DBManager;
import com.sothawo.taboo2.repository.jpa.H2Repository;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;
import static org.hamcrest.CoreMatchers.hasItem;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.collection.IsCollectionWithSize.hasSize;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

/**
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@RunWith(Parameterized.class)
public class RepositoryTest {
// ------------------------------ FIELDS ------------------------------

    private static final String H2_JDBC_URL_TEST = "jdbc:h2:./target/bookmark-db-test";
    private static final String DB_CHANGELOG = "db/db-changelog.xml";

    @Parameter(0)
    public Class<BookmarkRepositoryFactory> repositoryFactoryClass;

    @Parameter(1)
    public String[] repositoryFactoryArguments;

    /** created in #setupRepository() method */
    private BookmarkRepository repository;

    /**
     * run the database changelog file before running the tests.
     */
    @BeforeClass
    public static void beforeClass() {
        DBManager.updateDB(H2_JDBC_URL_TEST, DB_CHANGELOG);
    }

// -------------------------- STATIC METHODS --------------------------

    /**
     * get the parameters for the tests. For every test class execution this is an array of a
     * BookmarkRepositoryFactory.class and an array of creatin arguments.
     *
     * @return test parameters
     */
    @Parameters(name="{index}: factory: {0}, args: {1}")
    public static List<Object[]> parameters() {
        return Arrays.asList(new Object[][]
                {
                        {InMemoryRepository.Factory.class, null},
                        {H2Repository.Factory.class, new String[]{H2_JDBC_URL_TEST}}
                });
    }

// -------------------------- OTHER METHODS --------------------------

    @After
    public void closeRepository() throws Exception {
        repository.close();
    }

    @Test
    public void initiallyEmpty() throws Exception {
        assertThat(repository.getAllBookmarks(), hasSize(0));
        assertThat(repository.getAllTags(), hasSize(0));
    }

    @Test
    public void createBookmark() throws Exception {
        Bookmark bookmarkIn = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build();

        Bookmark bookmarkOut = repository.createBookmark(bookmarkIn);

        assertThat(bookmarkOut, is(notNullValue()));
        assertThat(bookmarkOut.getId(), is(notNullValue()));
        assertThat(bookmarkOut.getUrl(), is(bookmarkIn.getUrl()));
        assertThat(bookmarkOut.getTitle(), is(bookmarkIn.getTitle()));
        assertThat(bookmarkOut.getTags().size(), is(1));
        assertThat(bookmarkOut.getTags().iterator().next(), is(bookmarkIn.getTags().iterator().next()));

        assertThat(repository.getAllBookmarks(), hasSize(1));
        assertThat(repository.getAllTags(), hasSize(1));
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBookmarkWithId() throws Exception {
        Bookmark bookmarkIn = aBookmark().withId("11").withUrl("url1").withTitle("title1").addTag("tag1").build();

        repository.createBookmark(bookmarkIn);
        fail("expected IllegalArgumentException");
    }

    @Test(expected = IllegalArgumentException.class)
    public void createBookmarkWithoutUrl() throws Exception {
        repository.createBookmark(aBookmark().withTitle("title").build());
        fail("excpected IllegalArgumentException");
    }

    @Test(expected = AlreadyExistsException.class)
    public void createBookmarksWithExistingUrl() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build();
        Bookmark bookmark2 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag2").build();

        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        fail("expected AlreadyExistsException");
    }

    @Test
    public void deleteExistingBookmark() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").build();

        bookmark1 = repository.createBookmark(bookmark1);
        bookmark2 = repository.createBookmark(bookmark2);

        repository.deleteBookmark(bookmark2.getId());

        Collection<Bookmark> allBookmarks = repository.getAllBookmarks();
        assertThat(allBookmarks, hasSize(1));
        assertThat(allBookmarks, hasItems(bookmark1));

        assertThat(repository.getAllTags(), hasSize(1));
    }

    @Test(expected = NotFoundException.class)
    public void deleteNotExistingBookmark() throws Exception {
        repository.deleteBookmark("42");
        fail("expected NotFoundException");
    }

    @Test
    public void findAllBookmarks() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").build();

        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);

        Collection<Bookmark> allBookmarks = repository.getAllBookmarks();
        assertThat(allBookmarks, hasSize(2));
        assertThat(allBookmarks, hasItems(bookmark1, bookmark2));
    }

    @Test
    public void dumpBookmarks() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").build();

        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);

        Collection<Bookmark> allBookmarks = repository.dumpBookmarks();
        assertThat(allBookmarks, hasSize(2));
        assertThat(allBookmarks, hasItems(bookmark1, bookmark2));
        for (Bookmark bookmark : allBookmarks) {
            assertThat(bookmark.getId(), is(nullValue()));
        }
    }

    @Test
    public void findAllTags() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").addTag("common").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").addTag("common").build();
        Bookmark bookmark3 = aBookmark().withUrl("url3").withTitle("title3").addTag("tag3").build();
        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        repository.createBookmark(bookmark3);

        Collection<String> tags = repository.getAllTags();

        assertThat(tags, hasSize(4));
        assertThat(tags, hasItems("tag1", "tag2", "tag3", "common"));
    }

    @Test
    public void findBookmarkById() throws Exception {
        Bookmark bookmark =
                repository.createBookmark(aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").build());

        Bookmark foundBookmark = repository.getBookmarkById(bookmark.getId());

        assertThat(foundBookmark, is(bookmark));
    }

    @Test(expected = NotFoundException.class)
    public void findBookmarkByIdNotExisting() throws Exception {
        repository.getBookmarkById("42");
        fail("NotFoundException expected");
    }

    @Test
    public void findBookmarkBySearchInTitle() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("Hello world").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("world wide web").build();
        Bookmark bookmark3 = aBookmark().withUrl("url3").withTitle("say hello").build();
        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        repository.createBookmark(bookmark3);

        Collection<Bookmark> bookmarks =
                repository.getBookmarksWithSearch("hello");

        assertThat(bookmarks, hasSize(2));
        assertThat(bookmarks, hasItems(bookmark1, bookmark3));
    }

    @Test
    public void findBookmarksBySearchInTitleAndTags() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("Hello world").addTag("tag1").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("world wide web").addTag("tag1").addTag("tag2")
                .build();
        Bookmark bookmark3 = aBookmark().withUrl("url3").withTitle("say hello").addTag("tag3").build();
        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        repository.createBookmark(bookmark3);

        Collection<Bookmark> bookmarks =
                repository.getBookmarksWithTagsAndSearch(Collections.singletonList("tag1"), true, "hello");

        assertThat(bookmarks, hasSize(1));
        assertThat(bookmarks, hasItems(bookmark1));
    }

    @Test
    public void findBookmarksWithTagsAnd() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").addTag("common").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").addTag("common").build();
        Bookmark bookmark3 = aBookmark().withUrl("url3").withTitle("title3").addTag("tag3").build();
        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        repository.createBookmark(bookmark3);

        Collection<Bookmark> bookmarks =
                repository.getBookmarksWithTags(Arrays.asList("tag2", "common"), true);

        assertThat(bookmarks, hasSize(1));
        assertThat(bookmarks, hasItem(bookmark2));
    }

    @Test
    public void findBookmarksWithTagsOr() throws Exception {
        Bookmark bookmark1 = aBookmark().withUrl("url1").withTitle("title1").addTag("tag1").addTag("common").build();
        Bookmark bookmark2 = aBookmark().withUrl("url2").withTitle("title2").addTag("tag2").addTag("common").build();
        Bookmark bookmark3 = aBookmark().withUrl("url3").withTitle("title3").addTag("tag3").build();
        repository.createBookmark(bookmark1);
        repository.createBookmark(bookmark2);
        repository.createBookmark(bookmark3);

        Collection<Bookmark> bookmarks =
                repository.getBookmarksWithTags(Arrays.asList("tag2", "common"), false);

        assertThat(bookmarks, hasSize(2));
        assertThat(bookmarks, hasItems(bookmark1, bookmark2));
    }

    @Test
    public void findTagsOnNewRepositoryYieldsEmptyList() throws Exception {
        Collection<String> tags = repository.getAllTags();

        assertThat(tags, hasSize(0));
    }

    @Test
    public void findTagsWhenNoTagsExistYieldsEmptyList() throws Exception {
        Bookmark bookmark = aBookmark().withUrl("url1").withTitle("title1").build();
        repository.createBookmark(bookmark);

        Collection<String> tags = repository.getAllTags();

        assertThat(tags, hasSize(0));
    }

    @Test
    public void repositoryHasNoBookmarksOnCreation() throws Exception {
        Collection<Bookmark> bookmarks = repository.getAllBookmarks();
        assertThat(bookmarks, hasSize(0));
    }

    @Before
    public void setupRepository() throws Exception {
        repository = repositoryFactoryClass.newInstance().create(repositoryFactoryArguments);
        repository.purge();
    }

    @Test
    public void updateBookmark() throws Exception {
        Bookmark bookmark = aBookmark().withUrl("url0").withTitle("title0").addTag("tag0").build();

        bookmark = repository.createBookmark(bookmark);
        bookmark.setUrl("url1");
        bookmark.setTitle("title1");
        bookmark.clearTags();
        bookmark.addTag("tag1");
        String id = bookmark.getId();

        repository.updateBookmark(bookmark);
        bookmark = repository.getBookmarkById(id);

        assertThat(bookmark.getId(), is(id));
        assertThat(bookmark.getUrl(), is("url1"));
        assertThat(bookmark.getTitle(), is("title1"));
        assertThat(bookmark.getTags(), hasSize(1));
        assertThat(bookmark.getTags(), hasItem("tag1"));

        assertThat(repository.getAllBookmarks(), hasSize(1));
        assertThat(repository.getAllTags(), hasSize(1));
    }

    @Test(expected = AlreadyExistsException.class)
    public void updateBookmarkToExistingUrl() throws Exception {
        Bookmark bookmark = repository.createBookmark(aBookmark().withUrl("url0").withTitle("title0").addTag("tag0")
                .build());
        repository.createBookmark(aBookmark().withUrl("url1").withTitle("title1").addTag("tag1")
                .build());
        bookmark.setUrl("url1");

        repository.updateBookmark(bookmark);

        fail("AlreadyExistsException expected");
    }

    @Test(expected = NotFoundException.class)
    public void updateBookmarkWithNotExistingId() throws Exception {
        Bookmark bookmark = aBookmark().withUrl("url0").withTitle("title0").addTag("tag0").build();
        bookmark.setId("NotExistingId");

        repository.updateBookmark(bookmark);

        fail("NotFoundException expected");
    }

    @Test(expected = NullPointerException.class)
    public void updateBookmarkWithoutData() throws Exception {
        repository.updateBookmark(null);

        fail("NullPointerException expected");
    }

    @Test(expected = IllegalArgumentException.class)
    public void updateBookmarkWithoutId() throws Exception {
        Bookmark bookmark = aBookmark().withUrl("url0").withTitle("title0").addTag("tag0").build();

        repository.updateBookmark(bookmark);

        fail("IllegalArgumentException expected");
    }
}
