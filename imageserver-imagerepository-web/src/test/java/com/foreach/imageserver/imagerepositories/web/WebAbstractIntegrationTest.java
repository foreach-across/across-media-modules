package com.foreach.imageserver.imagerepositories.web;

import com.foreach.imageserver.core.AbstractIntegrationTest;
import com.foreach.imageserver.imagerepositories.web.config.WebIntegrationTestConfig;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {WebIntegrationTestConfig.class})
public class WebAbstractIntegrationTest extends AbstractIntegrationTest {
}
