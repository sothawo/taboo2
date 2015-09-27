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

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * configuraiton for BookmarkRepositories.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Configuration
public class BookmarkRepositoryConfig {
// -------------------------- OTHER METHODS --------------------------

    /**
     * in-memory implementation, used in-memory profile is defined.
     *
     * @return BookmarkRepository in-memory implementation.
     */
    @Bean(name = "defaultBookmarkRepository")
    @Profile("repo-inmemory")
    public BookmarkRepository getDefaultBookmarkRepository() {
        return new InMemoryRepository();
    }

    /**
     * null implementation, returned, when the repo-mocked profile is active
     * @return null
     */
    @Bean(name = "nullBookmarkRepository")
    @Profile("repo-mocked")
    public BookmarkRepository getNullBookmarkRepository() {
        return null;
    }
}
