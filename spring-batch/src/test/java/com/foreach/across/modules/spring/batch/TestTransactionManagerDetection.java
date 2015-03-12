package com.foreach.across.modules.spring.batch;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.context.AcrossContextUtils;
import com.foreach.across.core.context.configurer.AnnotatedClassConfigurer;
import com.foreach.across.core.context.configurer.ConfigurerScope;
import com.foreach.across.modules.spring.batch.config.SpringBatchConfiguration;
import com.foreach.across.test.AcrossTestContext;
import org.junit.Test;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

import static org.junit.Assert.*;

/**
 * @author Arne Vandamme
 */
public class TestTransactionManagerDetection
{
	private final AcrossContextConfigurer defaultConfigurer = new AcrossContextConfigurer()
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new SpringBatchModule() );
		}
	};

	@Test
	public void defaultCreatedIfNoneConfigured() {
		try (AcrossTestContext ctx = new AcrossTestContext( defaultConfigurer )) {
			SpringBatchConfiguration config = ctx.beanRegistry().getBeanOfTypeFromModule( SpringBatchModule.NAME,
			                                                                              SpringBatchConfiguration.class );

			PlatformTransactionManager proxy = config.getTransactionManager();
			PlatformTransactionManager target = (PlatformTransactionManager) AcrossContextUtils.getProxyTarget( proxy );

			assertNotNull( target );
			assertEquals( 1, ctx.beanRegistry().getBeansOfType( PlatformTransactionManager.class, true ).size() );
		}
	}

	@Test
	public void existingUsedIfNoneSpecific() {
		AcrossContextConfigurer single = new AcrossContextConfigurer()
		{
			@Override
			public void configure( AcrossContext context ) {
				context.addApplicationContextConfigurer(
						new AnnotatedClassConfigurer( SingleTransactionManagerConfig.class ),
						ConfigurerScope.CONTEXT_ONLY
				);
			}
		};

		try (AcrossTestContext ctx = new AcrossTestContext( defaultConfigurer, single )) {
			SpringBatchConfiguration config = ctx.beanRegistry().getBeanOfTypeFromModule( SpringBatchModule.NAME,
			                                                                              SpringBatchConfiguration.class );

			PlatformTransactionManager proxy = config.getTransactionManager();
			PlatformTransactionManager target = (PlatformTransactionManager) AcrossContextUtils.getProxyTarget( proxy );

			assertNotNull( target );
			assertSame( ctx.beanRegistry().getBean( "singleTransactionManager" ), target );
			assertEquals( 1, ctx.beanRegistry().getBeansOfType( PlatformTransactionManager.class ).size() );
		}
	}

	@Test
	public void multipleCandidatesButOneSpecified() {
		AcrossContextConfigurer multiple = new AcrossContextConfigurer()
		{
			@Override
			public void configure( AcrossContext context ) {
				context.addApplicationContextConfigurer(
						new AnnotatedClassConfigurer( SingleTransactionManagerConfig.class,
						                              SpecificTransactionManagerConfig.class ),
						ConfigurerScope.CONTEXT_ONLY
				);
			}
		};

		try (AcrossTestContext ctx = new AcrossTestContext( defaultConfigurer, multiple )) {
			SpringBatchConfiguration config = ctx.beanRegistry().getBeanOfTypeFromModule( SpringBatchModule.NAME,
			                                                                              SpringBatchConfiguration.class );

			PlatformTransactionManager proxy = config.getTransactionManager();
			PlatformTransactionManager target = (PlatformTransactionManager) AcrossContextUtils.getProxyTarget( proxy );

			assertNotNull( target );
			assertSame( ctx.beanRegistry().getBean( "otherTransactionManager" ), target );
			assertEquals( 2, ctx.beanRegistry().getBeansOfType( PlatformTransactionManager.class ).size() );
		}
	}

	@Configuration
	public static class SingleTransactionManagerConfig
	{
		@Bean
		public DataSourceTransactionManager singleTransactionManager( DataSource dataSource ) {
			return new DataSourceTransactionManager( dataSource );
		}
	}

	@Configuration
	public static class SpecificTransactionManagerConfig
	{
		@Bean(name = { "otherTransactionManager", SpringBatchConfiguration.TRANSACTION_MANAGER_BEAN })
		public DataSourceTransactionManager otherTransactionManager( DataSource dataSource ) {
			return new DataSourceTransactionManager( dataSource );
		}
	}
}
