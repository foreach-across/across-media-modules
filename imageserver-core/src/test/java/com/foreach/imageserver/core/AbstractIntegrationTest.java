package com.foreach.imageserver.core;

import com.foreach.imageserver.core.config.IntegrationTestConfig;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = {IntegrationTestConfig.class})
@EnableTransactionManagement
public abstract class AbstractIntegrationTest {
}
