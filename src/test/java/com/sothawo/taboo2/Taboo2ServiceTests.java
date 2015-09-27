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

import mockit.Deencapsulation;
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
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for the Taboo2Service class.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Taboo2Application.class)
@WebAppConfiguration
@ActiveProfiles({"test", "repo-mocked"})
public class Taboo2ServiceTests {
// ------------------------------ FIELDS ------------------------------

    /** The service to test. Cannot be created with @Tested
     * because we need internal spring DI resolution. */
    @Autowired
    private Taboo2Service taboo2Service;

    /** a mocked bookmark repository. */
    @Injectable
    private BookmarkRepository repository;

// -------------------------- OTHER METHODS --------------------------

    @Test
    public void checkRunning() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(taboo2Service).build();

        mockMvc.perform(get(Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_CHECK).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(Taboo2Service.IS_RUNNING))
        ;
    }

    /**
     * set up the Service to be tested from the WebApplication context and replace the contained BookmarkRepository
     * with a mock. Must be called before each test, as JMockit creates the repository for each test call.
     */
    @Before
    public void setupTest() {
        Deencapsulation.setField(taboo2Service, repository);
        taboo2Service.logInfoToDebug();
    }
}
