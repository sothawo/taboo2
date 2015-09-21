package com.sothawo.taboo2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Taboo2Application.class)
@WebAppConfiguration
@ActiveProfiles("test")
public class Taboo2ApplicationTests {

	@Test
	public void contextLoads() {
	}

}
