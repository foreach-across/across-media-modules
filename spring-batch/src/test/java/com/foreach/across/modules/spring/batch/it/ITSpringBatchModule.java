package com.foreach.across.modules.spring.batch.it;

import com.foreach.across.config.AcrossContextConfigurer;
import com.foreach.across.core.AcrossContext;
import com.foreach.across.core.EmptyAcrossModule;
import com.foreach.across.modules.spring.batch.SpringBatchModule;
import com.foreach.across.test.AcrossTestConfiguration;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.JobRegistry;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext
@ContextConfiguration(classes = ITSpringBatchModule.Config.class)
public class ITSpringBatchModule
{
	@Autowired(required = false)
	private JobRepository jobRepository;

	@Autowired(required = false)
	private JobLauncher jobLauncher;

	@Autowired(required = false)
	private JobRegistry jobRegistry;

	@Test
	public void exposedSpringBatchBeans() {
		assertNotNull( jobRepository );
		assertNotNull( jobLauncher );
		assertNotNull( jobRegistry );
	}

	@Test
	public void jobShouldHaveBeenPickedUp() throws Exception {
		Map<String, JobParameter> params = new HashMap<>();
		params.put( "map", new JobParameter( 123L ) );

		JobExecution execution = jobLauncher.run( jobRegistry.getJob( "jobModule.testJob" ),
		                                          new JobParameters( params ) );

		Thread.sleep( 500 );

		assertEquals( BatchStatus.COMPLETED, execution.getStatus() );
		assertEquals( new BigDecimal( "12.30" ), execution.getExecutionContext().get( "returnValue" ) );

	}

	@Configuration
	@AcrossTestConfiguration
	protected static class Config implements AcrossContextConfigurer
	{
		@Override
		public void configure( AcrossContext context ) {
			context.addModule( new SpringBatchModule() );

			EmptyAcrossModule module = new EmptyAcrossModule( "jobModule" );
			module.addApplicationContextConfigurer( JobConfig.class );

			context.addModule( module );
		}
	}

	@Configuration
	protected static class JobConfig
	{
		@Autowired
		private JobBuilderFactory jobBuilderFactory;

		@Autowired
		private StepBuilderFactory stepBuilderFactory;

		@Bean
		public Job testJob() {
			return jobBuilderFactory.get( "testJob" ).start( step() ).build();
		}

		@Bean
		protected Step step() {
			return stepBuilderFactory.get( "step" )
			                         .tasklet(
					                         new Tasklet()
					                         {
						                         @Override
						                         public RepeatStatus execute( StepContribution contribution,
						                                                      ChunkContext chunkContext ) throws Exception {
							                         return RepeatStatus.FINISHED;
						                         }
					                         }
			                         )
			                         .listener(
					                         new StepExecutionListener()
					                         {
						                         @Override
						                         public void beforeStep( StepExecution stepExecution ) {

						                         }

						                         @Override
						                         public ExitStatus afterStep( StepExecution stepExecution ) {
							                         stepExecution.getJobExecution().getExecutionContext()
							                                      .put( "returnValue", new BigDecimal( "12.30" ) );

							                         return ExitStatus.COMPLETED;
						                         }
					                         } )
			                         .build();

		}
	}

}
