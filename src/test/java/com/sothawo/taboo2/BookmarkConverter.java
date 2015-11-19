/**
 * Copyright (c) 2015 sothawo
 *
 * http://www.sothawo.com
 */
package com.sothawo.taboo2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * program to read a mozilla exported json file. Tags are extracted
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class BookmarkConverter {
// ------------------------------ FIELDS ------------------------------

    private int count = 0;

// --------------------------- main() method ---------------------------

    public static void main(String[] args) {
        try {
            new BookmarkConverter().run();
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    private void run() {
        Scanner scanner = null;
        try {
            String text = new Scanner(new File("/Users/peter/Desktop/bookmarks-2015-11-17.json"), "UTF-8")
                    .useDelimiter
                            ("\\A").next();
            ObjectMapper mapper = new ObjectMapper();
            JsonNode node = mapper.readTree(text);

            String title = node.path("title").asText();
            ImmutableList<String> tags = title.isEmpty() ? ImmutableList.of() : ImmutableList.of(title);
            dumpBookmarks(node, tags);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (null != scanner) {
                scanner.close();
            }
        }
        System.out.printf("count: " + count);
    }

    private void dumpBookmarks(JsonNode node, ImmutableList<String> tags) {
        final String type = node.path("type").asText();
        final String title = node.path("title").asText();
        if ("text/x-moz-place".equals(type)) {
            final List<String> nodeTags =
                    Arrays.asList(node.path("tags").asText().split(",")).stream().filter(s -> !s.isEmpty())
                            .collect(Collectors.toList());
            final ImmutableList<String> allTags = nodeTags.size() > 0 ?
                    ImmutableList.<String>builder().addAll(tags.iterator()).addAll(nodeTags.iterator()).build() : tags;
            System.out.println(
                    MessageFormat.format("url: {0}, title: {1}, tags: {2}", node.path("uri").asText(), title, allTags));
            count++;
        } else if ("text/x-moz-place-container".equals(type)) {
            final ImmutableList<String> allTags =
                    title.isEmpty() ? tags : ImmutableList.<String>builder().addAll(tags.iterator()).add(title).build();
            node.path("children").forEach(jsonNode -> dumpBookmarks(jsonNode, allTags));
        }
    }
}
