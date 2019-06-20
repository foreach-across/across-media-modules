package test;

import com.foreach.across.core.AcrossContext;
import com.foreach.across.modules.ehcache.EhcacheModule;
import com.foreach.across.modules.web.AcrossWebModule;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.runner.RunWith;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = { AbstractCachedIntegrationTest.Config.class })
@WebAppConfiguration
@EnableTransactionManagement
public abstract class AbstractCachedIntegrationTest
{
	@AcrossTestConfiguration(modules = { AcrossWebModule.NAME })
	@Configuration
	static class Config extends AbstractIntegrationTest.Config
	{
		@Override
		public void configure( AcrossContext context ) {
			super.configure( context );

			context.addModule( ehcacheModule() );
		}

		public EhcacheModule ehcacheModule() {
			return new EhcacheModule();
		}
	}
}
