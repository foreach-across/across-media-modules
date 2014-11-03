package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.taskrunner.business.PersistedTask;
import com.foreach.across.modules.taskrunner.business.Task;
import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import com.foreach.common.test.MockedLoader;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.UUID;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * @author Arne Vandamme
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(classes = TestTaskRunnerService.Config.class, loader = MockedLoader.class)
public class TestTaskRunnerService
{
	@Autowired
	private TaskRunnerService taskRunnerService;

	@Autowired
	private TaskObjectsSerializer objects;

	@Autowired
	private TaskRepository taskRepository;

	@Before
	public void resetMocks() {
		reset( objects, taskRepository );
	}

	@Test
	public void unexistingTaskIsNull() {
		String uuid = UUID.randomUUID().toString();

		assertNull( taskRunnerService.getTaskById( uuid ) );
	}

	@Test
	public void noDeserializationIfNoData() {
		String uuid = UUID.randomUUID().toString();

		PersistedTask persisted = new PersistedTask();
		persisted.setUuid( uuid );
		persisted.setCreatedBy( "freddie" );

		when( taskRepository.getByUuid( uuid ) ).thenReturn( persisted );

		Task task = taskRunnerService.getTaskById( uuid );

		assertNotNull( task );
		assertEquals( uuid, task.getId() );
		assertEquals( "freddie", task.getCreatedBy() );

		verify( objects, never() ).deserialize( anyString() );
	}

	@Test
	public void deserializingParametersAndResult() {
		String uuid = UUID.randomUUID().toString();

		Long parameters = 123L;
		Integer result = 999;

		PersistedTask persisted = new PersistedTask();
		persisted.setUuid( uuid );
		persisted.setCreatedBy( "freddie" );
		persisted.setParameters( "parameters" );
		persisted.setResult( "result" );

		when( taskRepository.getByUuid( uuid ) ).thenReturn( persisted );
		when( objects.deserialize( "parameters" ) ).thenReturn( parameters );
		when( objects.deserialize( "result" ) ).thenReturn( result );

		Task task = taskRunnerService.getTaskById( uuid );

		assertNotNull( task );
		assertEquals( uuid, task.getId() );
		assertEquals( "freddie", task.getCreatedBy() );
		assertSame( parameters, task.getParameters() );
		assertSame( result, task.getResult() );
	}

	@Configuration
	protected static class Config
	{
		@Bean
		public TaskRunnerService taskRunnerService() {
			return new TaskRunnerServiceImpl( 1, 1 );
		}
	}
}
