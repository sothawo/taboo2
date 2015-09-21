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

import mockit.Tested;
import org.junit.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

/**
 * Tests for the Taboo2Service class.
 *
 * @author P.J. Meisch (pj.meisch@sothawo.com).
 */
public class Taboo2ServiceTest {
    @Tested
    Taboo2Service taboo2Service;

    @Test
    public void checkRunning() throws Exception {

        MockMvc mockMvc = standaloneSetup(taboo2Service).build();
        mockMvc.perform(get(Taboo2Service.MAPPING_TABOO2 + Taboo2Service.MAPPING_CHECK).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string(Taboo2Service.IS_RUNNING))
        ;


    }
}
