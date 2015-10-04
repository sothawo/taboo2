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
 * configuration for BookmarkRepositories. The class must produce a BookmarkRepository implementation for each of the
 * used spring profiles.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@Configuration
public class BookmarkRepositoryConfig {
// -------------------------- OTHER METHODS --------------------------

    /**
     * in-memory implementation, used when repo-inmemory profile is active.
     *
     * @return BookmarkRepository in-memory implementation.
     */
    @Bean(name = "defaultBookmarkRepository")
    @Profile("repo-inmemory")
    public BookmarkRepository defaultBookmarkRepository() {
        return new InMemoryRepository();
    }

    /**
     * null implementation, used when the repo-mocked profile is active, becaus then in the tests a mocked repo is
     * injected.
     *
     * @return null
     */
    @SuppressWarnings("SameReturnValue")
    @Bean(name = "nullBookmarkRepository")
    @Profile("repo-mocked")
    public BookmarkRepository nullBookmarkRepository() {
        return null;
    }
}
