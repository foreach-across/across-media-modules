package com.foreach.across.modules.spring.batch.config;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.context.configurer.PostProcessorConfigurer;
import com.foreach.across.core.context.registry.AcrossContextBeanRegistry;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.modules.spring.batch.SpringBatchModuleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.target.AbstractLazyCreationTargetSource;
import org.springframework.batch.core.configuration.BatchConfigurationException;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.BatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.explore.support.JobExplorerFactoryBean;
import org.springframework.batch.core.explore.support.MapJobExplorerFactoryBean;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.batch.core.repository.support.MapJobRepositoryFactoryBean;
import org.springframework.batch.support.transaction.ResourcelessTransactionManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Configuration
@AcrossEventHandler
@EnableBatchProcessing
public class SpringBatchConfiguration implements BatchConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( SpringBatchConfiguration.class );

	@Autowired
	private AcrossContextBeanRegistry beanRegistry;

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private SpringBatchModuleSettings settings;

	@Autowired
	private DataSource dataSource;

	private PlatformTransactionManager transactionManager;
	private JobRepository jobRepository;
	private JobLauncher jobLauncher;
	private JobExplorer jobExplorer;

	protected SpringBatchConfiguration() {
	}

	@Override
	public JobRepository getJobRepository() {
		return jobRepository;
	}

	@Override
	public PlatformTransactionManager getTransactionManager() {
		if ( transactionManager == null ) {
			PlatformTransactionManagerTargetSource targetSource = new PlatformTransactionManagerTargetSource();
			targetSource.setContextBeanRegistry( beanRegistry );
			targetSource.setDataSource( dataSource );

			transactionManager = ProxyFactory.getProxy( PlatformTransactionManager.class, targetSource );
		}

		return transactionManager;
	}

	@Override
	public JobLauncher getJobLauncher() {
		return jobLauncher;
	}

	@Override
	public JobExplorer getJobExplorer() {
		return jobExplorer;
	}

	@PostConstruct
	public void initialize() {
		try {
			if ( dataSource == null ) {
				MapJobRepositoryFactoryBean jobRepositoryFactory = new MapJobRepositoryFactoryBean(
						getTransactionManager() );
				jobRepositoryFactory.afterPropertiesSet();
				this.jobRepository = jobRepositoryFactory.getObject();

				MapJobExplorerFactoryBean jobExplorerFactory = new MapJobExplorerFactoryBean( jobRepositoryFactory );
				jobExplorerFactory.afterPropertiesSet();
				this.jobExplorer = jobExplorerFactory.getObject();
			}
			else {
				this.jobRepository = createJobRepository();

				JobExplorerFactoryBean jobExplorerFactoryBean = new JobExplorerFactoryBean();
				jobExplorerFactoryBean.setDataSource( this.dataSource );
				jobExplorerFactoryBean.afterPropertiesSet();
				this.jobExplorer = jobExplorerFactoryBean.getObject();
			}

			this.jobLauncher = createJobLauncher();
		}
		catch ( Exception e ) {
			throw new BatchConfigurationException( e );
		}

		SimpleJobLauncher jobLauncher = (SimpleJobLauncher) getJobLauncher();

		TaskExecutor taskExecutor = settings.getTaskExecutor();

		if ( taskExecutor == null ) {
			LOG.info( "Creating a default asynchronous task executor with a concurrency limit of 10" );

			SimpleAsyncTaskExecutor asyncTaskExecutor = new SimpleAsyncTaskExecutor( "springBatchModule-" );
			asyncTaskExecutor.setConcurrencyLimit( 5 );

			taskExecutor = asyncTaskExecutor;
		}

		jobLauncher.setTaskExecutor( taskExecutor );
	}

	private JobLauncher createJobLauncher() throws Exception {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository( jobRepository );
		jobLauncher.afterPropertiesSet();
		return jobLauncher;
	}

	protected JobRepository createJobRepository() throws Exception {
		JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
		factory.setDataSource( dataSource );
		factory.setTransactionManager( getTransactionManager() );
		factory.afterPropertiesSet();
		return factory.getObject();
	}

	@Bean
	public JobLauncher synchronousJobLauncher() {
		SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
		jobLauncher.setJobRepository( getJobRepository() );

		return jobLauncher;
	}

	@Event
	protected void registerBeanPostProcessor( final AcrossModuleBeforeBootstrapEvent moduleBeforeBootstrapEvent ) {
		moduleBeforeBootstrapEvent.getBootstrapConfig().addApplicationContextConfigurer(
				new PostProcessorConfigurer(
						new BeanFactoryPostProcessor()
						{
							@Override
							public void postProcessBeanFactory( ConfigurableListableBeanFactory beanFactory ) throws BeansException {
								JobRegistryBeanPostProcessor jobRegistrar = new JobRegistryBeanPostProcessor();
								jobRegistrar.setGroupName( moduleBeforeBootstrapEvent.getModule().getName() );
								jobRegistrar.setJobRegistry( jobRegistry );

								beanFactory.addBeanPostProcessor( jobRegistrar );
							}
						}
				)
		);
	}

	/**
	 * Fetches TransactionManager from the AcrossContext.
	 */
	private static class PlatformTransactionManagerTargetSource extends AbstractLazyCreationTargetSource
	{
		private AcrossContextBeanRegistry contextBeanRegistry;
		private DataSource dataSource;

		public void setDataSource( DataSource dataSource ) {
			this.dataSource = dataSource;
		}

		public void setContextBeanRegistry( AcrossContextBeanRegistry contextBeanRegistry ) {
			this.contextBeanRegistry = contextBeanRegistry;
		}

		@Override
		protected Object createObject() throws Exception {
			Assert.notNull( contextBeanRegistry );

			try {
				return contextBeanRegistry.getBeanOfType( PlatformTransactionManager.class );
			}
			catch ( BeansException be ) {
				LOG.warn(
						"No PlatformTransactionManager bean found in the AcrossContext, falling back to defaults." );
			}

			if ( dataSource != null ) {
				LOG.debug( "Creating default PlatformTransactionManager for the datasource" );
				return new DataSourceTransactionManager( dataSource );
			}

			LOG.warn( "No datasource was provided...using a Map based JobRepository" );

			return new ResourcelessTransactionManager();
		}
	}
}
