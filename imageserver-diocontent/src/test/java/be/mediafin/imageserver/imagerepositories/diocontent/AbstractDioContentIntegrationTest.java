package be.mediafin.imageserver.imagerepositories.diocontent;

import be.mediafin.imageserver.imagerepositories.diocontent.config.DioContentIntegrationTestConfig;
import com.foreach.imageserver.core.AbstractIntegrationTest;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(classes = {DioContentIntegrationTestConfig.class})
public class AbstractDioContentIntegrationTest extends AbstractIntegrationTest {
}
