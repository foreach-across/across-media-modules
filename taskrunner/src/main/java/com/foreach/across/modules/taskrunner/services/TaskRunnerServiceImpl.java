package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.hibernate.util.BasicServiceHelper;
import com.foreach.across.modules.taskrunner.business.*;
import com.foreach.across.modules.taskrunner.dto.PersistedTaskDto;
import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import com.foreach.across.modules.taskrunner.tasks.TaskHandler;
import com.foreach.across.modules.taskrunner.tasks.TaskParametersHashGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Arne Vandamme
 */
@SuppressWarnings("unchecked")
@Service
public class TaskRunnerServiceImpl implements TaskRunnerService
{
	private static final Logger LOG = LoggerFactory.getLogger( TaskRunnerService.class );

	@SuppressWarnings("unused")
	@RefreshableCollection(includeModuleInternals = true)
	private Collection<TaskHandler> taskHandlers;

	private ThreadPoolExecutor threadPool;

	@Autowired
	private TaskObjectsSerializer taskObjectsSerializer;

	@Autowired
	private TaskTransitionManager transitionManager;

	@Autowired
	private TaskRepository taskRepository;

	private TaskParametersHashGenerator<Object> defaultHashGenerator;

	private int threadpoolMin;
	private int threadpoolMax;

	// todo (feedback): min size should probably be 1 or 2 by default, with a max of 5 - move to executor (?)
	@Deprecated
	public TaskRunnerServiceImpl(
			int threadpoolMin, int threadpoolMax ) {

		this.threadpoolMin = threadpoolMin;
		this.threadpoolMax = threadpoolMax;
	}

	public void setDefaultHashGenerator( TaskParametersHashGenerator<Object> defaultHashGenerator ) {
		this.defaultHashGenerator = defaultHashGenerator;
	}

	@PostConstruct
	public void start() {
		if ( defaultHashGenerator == null ) {
			defaultHashGenerator = new DefaultTaskParametersHashGenerator( taskObjectsSerializer );
		}

		CustomizableThreadFactory threadFactory = new CustomizableThreadFactory( "taskrunner-thread-" );
		threadPool = new ThreadPoolExecutor( threadpoolMin, threadpoolMax, 60, TimeUnit.SECONDS,
		                                     new ArrayBlockingQueue<Runnable>( 1000 ), threadFactory );
	}

	// TODO: handle tasks that are not started and are interrupted
	@PreDestroy
	public void shutdown() {
		LOG.info( "shutting down reportservice threadpool" );
		threadPool.shutdown(); // Disable new tasks from being submitted
		try {
			LOG.info( "awaiting thread termination" );
			// Wait a while for existing tasks to terminate
			if ( !threadPool.awaitTermination( 30, TimeUnit.SECONDS ) ) {
				LOG.error( "threadpool did not terminate after 30 seconds, calling shutdownNow()" );
				threadPool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if ( !threadPool.awaitTermination( 30, TimeUnit.SECONDS ) ) {
					LOG.error( "threadpool did not terminate after 30 seconds" );
				}
				else {
					LOG.error( "threadpool terminated after shutdownNow()" );
				}
			}
			else {
				LOG.info( "threadpool terminated normally after 30 seconds" );
			}
		}
		catch ( InterruptedException e ) {
			LOG.error( "InterruptedException while shutting down threadpool", e );
			// (Re-)Cancel if current thread also interrupted
			threadPool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
		LOG.info( "threadpool shut down" );
	}

	/**
	 * Asynchronous submit of a request.
	 */
	@Override
	public Task submit( TaskRequest<?> request ) {
		TaskHandler handler = handler( request );

		return createOrGetTask( handler, request, true );
	}

	/**
	 * Synchronous execution of a request.
	 */
	@Override
	public Task execute( TaskRequest request ) {
		TaskHandler handler = handler( request );

		return createOrGetTask( handler, request, false );
	}

	@Override
	public Task getTaskById( String id ) {
		PersistedTask persisted = taskRepository.getByUuid( id );

		if ( persisted != null ) {
			return readTask( persisted );
		}

		return null;
	}

	private TaskHandler handler( TaskRequest<?> request ) {
		Assert.notNull( request.getParameters() );

		for ( TaskHandler handler : taskHandlers ) {
			if ( handler.accepts( request.getParameters() ) ) {
				return handler;
			}
		}

		throw new TaskExecutionException( "No task handler found for task request." );
	}

	private MutableTask createOrGetTask( TaskHandler handler, TaskRequest request, boolean async ) {
		String hash = generateParameterHash( handler, request.getParameters() );

		MutableTask task = findReusableTask( handler, request, hash );

		if ( task != null && request.isForceExecution() ) {
			reschedule( task, request );
		}
		else {
			task = createTaskFromRequest( request, hash );
		}

		try {
			FutureTask<Void> future = new FutureTask<>( new TaskRunnable( transitionManager, handler, task ), null );

			threadPool.execute( future );

			if ( !async ) {
				future.get();
			}
		}
		catch ( RejectedExecutionException | InterruptedException | ExecutionException e ) {
			// TODO: handle tasks that are interrupted by a threadpool shutdown
			LOG.error( "Failed to add task {} to threadpool. ", task.getId(), e );
		}

		return task;
	}

	private void reschedule( MutableTask task, TaskRequest request ) {
		// todo
	}

	private String generateParameterHash( TaskHandler handler, Object parameters ) {
		TaskParametersHashGenerator hashGenerator = defaultHashGenerator;

		if ( handler instanceof TaskParametersHashGenerator ) {
			hashGenerator = (TaskParametersHashGenerator) handler;
		}

		return hashGenerator.generateParametersHash( parameters );
	}

	private MutableTask findReusableTask( TaskHandler handler, TaskRequest request, String hash ) {
		Collection<PersistedTask> reportTasks = taskRepository.getAllByHash( hash );

		for ( PersistedTask nextTask : reportTasks ) {
			MutableTask existing = readMatchingTask( handler, request, nextTask );

			if ( existing != null ) {
				return existing;
			}
		}

		return null;
	}

	private MutableTask readMatchingTask(
			TaskHandler handler, TaskRequest request, PersistedTask persistedTask ) {

		// Task should not be reused
		if ( !persistedTask.isSaved() ) {
			return null;
		}

		// Task results have expired
		if ( persistedTask.getExpiryDate() != null && !persistedTask.getExpiryDate().before( new Date() ) ) {
			return null;
		}

		// Existing results are older than the oldest allowed
		if ( request.getOldestResultDate() != null && persistedTask.getStatus() == TaskStatus.FINISHED && persistedTask.getUpdated().before(
				request.getOldestResultDate() ) ) {
			return null;
		}

		// Execution is forced but the task is not yet finished
		if ( persistedTask.getStatus() == TaskStatus.FINISHED && request.isForceExecution() ) {
			return null;
		}

		// Read the entire task and see if the handler tells us we can't use it
		MutableTask task = readTask( persistedTask );

		if ( !handler.matches( request, task ) ) {
			return null;
		}

		// No reason why this task should not be reused
		return task;
	}

	private MutableTask createTaskFromRequest( TaskRequest request, String hash ) {
		PersistedTaskDto task = new PersistedTaskDto();
		task.setNewEntity( true );
		task.setParameters( serialize( request.getParameters() ) );
		task.setUuid( UUID.randomUUID().toString() );
		task.setRequestHashCode( hash );
		task.setCreatedBy( request.getCreatedBy() );
		task.setCreated( new Date() );
		task.setStatus( TaskStatus.SCHEDULED );
		task.setSaved( request.isSaveResult() );
		task.setExpiryDate( request.getExpiryDate() );

		PersistedTask persistedTask = BasicServiceHelper.save( task, PersistedTask.class, taskRepository );

		return readTask( persistedTask );
	}

	private MutableTask readTask( PersistedTask persisted ) {
		TaskImpl task = new TaskImpl( persisted.getUuid() );
		BeanUtils.copyProperties( persisted, task, "id", "parameters", "result" );
		task.setParameters( deserialize( persisted.getParameters() ) );
		task.setResult( deserialize( persisted.getResult() ) );

		return task;
	}

	private String serialize( Object data ) {
		return data != null ? taskObjectsSerializer.serialize( data ) : null;
	}

	private Object deserialize( String xml ) {
		return xml != null ? taskObjectsSerializer.deserialize( xml ) : null;
	}
}
