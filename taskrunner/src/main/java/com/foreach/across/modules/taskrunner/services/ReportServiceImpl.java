package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.core.annotations.RefreshableCollection;
import com.foreach.across.modules.hibernate.util.BasicServiceHelper;
import com.foreach.across.modules.taskrunner.business.ReportRequest;
import com.foreach.across.modules.taskrunner.business.ReportStatus;
import com.foreach.across.modules.taskrunner.business.ReportTask;
import com.foreach.across.modules.taskrunner.dto.ReportTaskDto;
import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.*;

/**
 * @author Arne Vandamme
 */
@Service
public class ReportServiceImpl implements ReportService
{
	private static final Logger LOG = LoggerFactory.getLogger( ReportService.class );

	@SuppressWarnings("unused")
	@RefreshableCollection(includeModuleInternals = true)
	private Collection<ReportHandler> reportHandlers;

	private ThreadPoolExecutor threadPool;
	private ReportParameterSerializer reportParameterSerializer = new ReportParameterSerializer();

	@Autowired
	private TaskRepository taskRepository;

	private int threadpoolMin;
	private int threadpoolMax;

	// todo (feedback): min size should probably be 1 or 2 by default, with a max of 5
	public ReportServiceImpl( int threadpoolMin,
	                          int threadpoolMax ) {

		this.threadpoolMin = threadpoolMin;
		this.threadpoolMax = threadpoolMax;
	}

	@PostConstruct
	public void init() {
		CustomizableThreadFactory threadFactory = new CustomizableThreadFactory( "report-pool-" );
		threadPool = new ThreadPoolExecutor( threadpoolMin, threadpoolMax, 60, TimeUnit.SECONDS,
		                                     new ArrayBlockingQueue<Runnable>( 100 ),
		                                     threadFactory );
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
	public ReportTask submit( ReportRequest<?> request ) {
		ReportHandler handler = handler( request );

		return createOrGetTask( handler, request, true );
	}

	private ReportHandler handler( ReportRequest<?> request ) {
		for ( ReportHandler handler : reportHandlers ) {
			if ( handler.accepts( request ) ) {
				return handler;
			}
		}

		throw new RuntimeException( "unable to handle report" );
	}

	private <T> ReportTask createOrGetTask( ReportHandler<T> handler, ReportRequest<T> request, boolean async ) {

		// create hash of request parameters
		// if allowed: get all tasks from database that match the hash

		String requestXml = reportParameterSerializer.serialize( request );
		String hash = handler.generateHash( request, reportParameterSerializer );

		ReportTask foundReportTask = findExistingReport( handler, request, hash );
		ReportTaskDto task = new ReportTaskDto();

		if ( !request.isForceGeneration() && foundReportTask != null ) {
			return foundReportTask;
		}
		if ( foundReportTask == null ) {
			task = new ReportTaskDto();
			task.setNewEntity( true );
			task.setParameters( requestXml );
			task.setUuid( UUID.randomUUID().toString() );
			task.setRequestHashCode( hash );
			task.setCreatedBy( request.getCreatedBy() );
			task.setCreated( new Date() );
		}

		if ( request.isForceGeneration() && foundReportTask != null ) {
			task = ReportTaskDto.fromReportTask( foundReportTask );
		}

		task.setStatus( ReportStatus.SCHEDULED );
		task.setSaved( request.isSaveResult() );
		task.setExpiryDate( request.getExpiryDate() );
		ReportTask reportTask = BasicServiceHelper.save( task, ReportTask.class, taskRepository );

		try {
			ReportTaskResult reportTaskResult = new ReportTaskResult();
			FutureTask<ReportTaskResult> futureTask = new FutureTask<>(
					new ReportTaskRunnable<>( handler, reportTask, request, taskRepository, reportTaskResult,
					                          reportParameterSerializer ), reportTaskResult );

			threadPool.execute( futureTask );
			if ( !async ) {
				ReportTaskResult callback = futureTask.get();
				reportTask = callback.getReportTask();
			}
		}
		catch ( RejectedExecutionException | InterruptedException | ExecutionException e ) {
			// TODO: handle tasks that are interrupted by a threadpool shutdown
			LOG.error( "Failed to add task {} to threadpool. ", task.getUuid(), e );
		}

		// iterate all tasks:
		// * if task is finished, not expired and created after the oldest result data: it is a candidate
		// * throw the candidate against handler.matches() and use it if the result is true
		// if task is forced and parameters determine it: replace existing
		// if no task: create a new task
		// if allowed: store the task in database

		return reportTask;
	}

	private <T> ReportTask findExistingReport( ReportHandler<T> handler, ReportRequest<T> request, String hash ) {
		Collection<ReportTask> reportTasks = taskRepository.getAllByHash( hash );

		for ( ReportTask nextTask : reportTasks ) {
			if ( isReportTaskValid( handler, request, nextTask ) ) {
				return nextTask;
			}
		}

		return null;
	}

	private <T> boolean isReportTaskValid( ReportHandler<T> handler, ReportRequest<T> request, ReportTask task ) {
		if ( task.getStatus() == ReportStatus.FINISHED ) {
			if ( task.getExpiryDate() == null || task.getExpiryDate().before( new Date() ) ) {
				if ( request.getOldestResultDate() == null ||
						task.getCreated().after( request.getOldestResultDate() ) ) {
					if ( handler.matches( request, task ) ) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/**
	 * Synchronous execution of a request.
	 */
	@Override
	public ReportTask execute( ReportRequest request ) {
		ReportHandler handler = handler( request );

		return createOrGetTask( handler, request, false );
	}

	// todo (feedback): fetching of a ReportTask should check if result is available and if it is, should deserialize it already
	@Override
	public ReportTask getReportTaskById( long id ) {
		return taskRepository.getById( id );
	}

	@Override
	public ReportTask getReportTaskByUuid( String uuid ) {
		return taskRepository.getByUuid( uuid );
	}

	@Override
	public <T> T getReportResult( String xml, Class<T> clazz ) {
		return reportParameterSerializer.deserialize( xml, clazz );
	}

	public static class ReportTaskResult
	{
		private ReportTask reportTask;

		public void setReportTask( ReportTask reportTask ) {
			this.reportTask = reportTask;
		}

		public ReportTask getReportTask() {
			return reportTask;
		}
	}
}
