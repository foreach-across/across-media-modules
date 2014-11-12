package com.foreach.across.modules.spring.batch.config;

import com.foreach.across.core.annotations.AcrossEventHandler;
import com.foreach.across.core.annotations.Event;
import com.foreach.across.core.context.configurer.PostProcessorConfigurer;
import com.foreach.across.core.events.AcrossModuleBeforeBootstrapEvent;
import com.foreach.across.modules.spring.batch.SpringBatchModuleSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.DefaultBatchConfigurer;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.support.JobRegistryBeanPostProcessor;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@Configuration
@AcrossEventHandler
@EnableBatchProcessing
public class SpringBatchConfiguration extends DefaultBatchConfigurer
{
	private static final Logger LOG = LoggerFactory.getLogger( SpringBatchConfiguration.class );

	@Autowired
	private JobRegistry jobRegistry;

	@Autowired
	private SpringBatchModuleSettings settings;

	@Override
	public void initialize() {
		super.initialize();

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
}
