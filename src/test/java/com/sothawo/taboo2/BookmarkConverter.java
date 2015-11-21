/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableSet;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

import static com.sothawo.taboo2.BookmarkBuilder.aBookmark;

/**
 * program to read a mozilla exported json file. Bookmark uris, titles and tags are extracted. The bookmarks are
 * inserted in the bookmark service.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class BookmarkConverter {
// ------------------------------ FIELDS ------------------------------

    private int count = 0;
    private String tabooUrlBookmarks = "https://taboo2-sothawo.rhcloud.com/taboo2/bookmarks";

    private HttpHeaders headers;

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        try {
            new BookmarkConverter().run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void run() {
        headers = new HttpHeaders();
        headers.add("Authorization", "Basic cGV0ZXI6aE16cFRlOU5Qclh1Qm5IQlZYOHJ0M0VFcm4vcXt2");

        Scanner scanner = null;
        try {
            String text = new Scanner(new File("/Users/peter/Desktop/bookmarks-2015-11-17.json"), "UTF-8")
                    .useDelimiter
                            ("\\A").next();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(text);

            String title = node.path("title").asText();
            final String importedTag = "_imported";
            ImmutableSet<String> tags =
                    title.isEmpty() ? ImmutableSet.of(importedTag) : ImmutableSet.of(title, importedTag);
            loadBookmarks(node, tags);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        System.out.printf("count: " + count);
    }

    private void loadBookmarks(JsonNode node, ImmutableSet<String> tags) {
        final String type = node.path("type").asText();
        final String title = node.path("title").asText();
        if ("text/x-moz-place".equals(type)) {
            final List<String> nodeTags =
                    Arrays.asList(node.path("tags").asText().split(","))
                            .stream()
                            .filter(s -> !s.isEmpty())
                            .map(s -> s.replaceAll("[ \t]", "_"))
                            .map(String::toLowerCase)
                            .collect(Collectors.toList());
            final ImmutableSet<String> allTags = nodeTags.size() > 0 ?
                    ImmutableSet.<String>builder().addAll(tags.iterator()).addAll(nodeTags.iterator()).build() : tags;
            final String uri = node.path("uri").asText();

            if (uri.startsWith("http")) {
                final BookmarkBuilder bookmarkBuilder = aBookmark().withUrl(uri).withTitle(title);
                allTags.stream().forEach(bookmarkBuilder::addTag);
                final Bookmark bookmark = bookmarkBuilder.build();
                storeBookmark(bookmark);
                count++;
            }
        } else if ("text/x-moz-place-container".equals(type)) {
            final ImmutableSet<String> allTags =
                    title.isEmpty()
                            ? tags
                            : ImmutableSet.<String>builder()
                            .addAll(tags.iterator())
                            .add(title.replaceAll("[ \t]", "_").toLowerCase())
                            .build();
            node.path("children").forEach(jsonNode -> loadBookmarks(jsonNode, allTags));
        }
    }

    private void storeBookmark(Bookmark bookmark) {
        if (count < 400) {
            return;
        }
        System.out.println("store " + bookmark);
        RestTemplate rest = new RestTemplate();
        rest.setErrorHandler(new DefaultResponseErrorHandler() {
                                 @Override
                                 public void handleError(ClientHttpResponse response) throws IOException {
                                     System.out.println("result: " + response.getStatusCode());
                                 }
                             }
        );

        HttpEntity<Bookmark> request = new HttpEntity<>(bookmark, headers);
        final URI uri = rest.postForLocation(tabooUrlBookmarks, request);
        System.out.println("count: " + count);
        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
