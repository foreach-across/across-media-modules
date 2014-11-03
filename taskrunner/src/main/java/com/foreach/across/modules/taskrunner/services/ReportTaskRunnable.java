package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.hibernate.util.BasicServiceHelper;
import com.foreach.across.modules.taskrunner.business.PersistedTask;
import com.foreach.across.modules.taskrunner.business.TaskRequest;
import com.foreach.across.modules.taskrunner.dto.PersistedTaskDto;
import com.foreach.across.modules.taskrunner.tasks.reports.ReportResult;
import com.foreach.across.modules.taskrunner.business.TaskStatus;
import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import com.foreach.across.modules.taskrunner.tasks.TaskHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Date;

public class ReportTaskRunnable<T> extends Thread
{
	private static final Logger LOG = LoggerFactory.getLogger( ReportTaskRunnable.class );
	private final TaskHandler<T> taskHandler;
	private PersistedTask reportTask;
	private final TaskRequest<T> taskRequest;

	private final TaskRepository taskRepository;
	private final TaskRunnerServiceImpl.ReportTaskResult reportTaskResult;
	private final TaskObjectsSerializer taskObjectsSerializer;

	public ReportTaskRunnable( TaskHandler<T> taskHandler,
	                           PersistedTask reportTask,
	                           TaskRequest<T> taskRequest,
	                           TaskRepository taskRepository,
	                           TaskRunnerServiceImpl.ReportTaskResult reportTaskResult,
	                           TaskObjectsSerializer taskObjectsSerializer ) {
		super(String.format( "%s-%s", taskRequest.getParameters().getClass(), reportTask.getUuid() ));
		this.taskHandler = taskHandler;
		this.reportTask = reportTask;
		this.taskRequest = taskRequest;
		this.taskRepository = taskRepository;
		this.reportTaskResult = reportTaskResult;
		this.taskObjectsSerializer = taskObjectsSerializer;
	}

	@Override
	public void run() {
		performStateChange( TaskStatus.BUSY );

		ReportResult result;
		try {
			result = taskHandler.execute( reportTask, taskRequest );
		}
		catch ( Exception e ) {
			LOG.error( "Failed to execute report", e );
			performStateChange( TaskStatus.FAILED );
			return;
		}

		finishTask( result );
	}

	private void finishTask( ReportResult result ) {
		PersistedTaskDto dto = new PersistedTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( TaskStatus.FINISHED );
		dto.setResult( taskObjectsSerializer.serialize( result ) );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, PersistedTask.class, taskRepository );

		reportTaskResult.setReportTask( reportTask );
	}

	private void performStateChange( TaskStatus taskStatus ) {
		PersistedTaskDto dto = new PersistedTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( taskStatus );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, PersistedTask.class, taskRepository );
	}
}
