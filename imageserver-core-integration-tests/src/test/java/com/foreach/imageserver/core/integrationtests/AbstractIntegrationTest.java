package com.foreach.imageserver.core.integrationtests;

import com.foreach.imageserver.core.integrationtests.config.IntegrationTestConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

@RunWith(SpringJUnit4ClassRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {IntegrationTestConfig.class})
public abstract class AbstractIntegrationTest {
}
