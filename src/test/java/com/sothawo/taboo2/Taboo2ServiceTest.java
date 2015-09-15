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
