package com.o6;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@TestPropertySource(locations = "classpath:applicationTest.properties")
public class O6ApplicationTest {

    @Ignore
	@Test
	public void contextLoads() {
	}

}
