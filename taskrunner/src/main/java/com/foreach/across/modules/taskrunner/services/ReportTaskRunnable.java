package com.foreach.across.modules.taskrunner.services;

import com.foreach.across.modules.hibernate.util.BasicServiceHelper;
import com.foreach.across.modules.taskrunner.business.ReportRequest;
import com.foreach.across.modules.taskrunner.business.ReportResult;
import com.foreach.across.modules.taskrunner.business.ReportStatus;
import com.foreach.across.modules.taskrunner.business.ReportTask;
import com.foreach.across.modules.taskrunner.dto.ReportTaskDto;
import com.foreach.across.modules.taskrunner.repositories.TaskRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.util.Date;

public class ReportTaskRunnable<T> extends Thread
{
	private static final Logger LOG = LoggerFactory.getLogger( ReportTaskRunnable.class );
	private final ReportHandler<T> reportHandler;
	private ReportTask reportTask;
	private final ReportRequest<T> reportRequest;

	private final TaskRepository taskRepository;
	private final ReportServiceImpl.ReportTaskResult reportTaskResult;
	private final ReportParameterSerializer reportParameterSerializer;

	public ReportTaskRunnable( ReportHandler<T> reportHandler,
	                           ReportTask reportTask,
	                           ReportRequest<T> reportRequest,
	                           TaskRepository taskRepository,
	                           ReportServiceImpl.ReportTaskResult reportTaskResult,
	                           ReportParameterSerializer reportParameterSerializer ) {
		super(String.format( "%s-%s", reportRequest.getParameters().getClass(), reportTask.getUuid() ));
		this.reportHandler = reportHandler;
		this.reportTask = reportTask;
		this.reportRequest = reportRequest;
		this.taskRepository = taskRepository;
		this.reportTaskResult = reportTaskResult;
		this.reportParameterSerializer = reportParameterSerializer;
	}

	@Override
	public void run() {
		performStateChange( ReportStatus.BUSY );

		ReportResult result;
		try {
			result = reportHandler.generate( reportTask, reportRequest );
		}
		catch ( Exception e ) {
			LOG.error( "Failed to generate report", e );
			performStateChange( ReportStatus.FAILED );
			return;
		}

		finishTask( result );
	}

	private void finishTask( ReportResult result ) {
		ReportTaskDto dto = new ReportTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( ReportStatus.FINISHED );
		dto.setResult( reportParameterSerializer.serialize( result ) );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, ReportTask.class, taskRepository );

		reportTaskResult.setReportTask( reportTask );
	}

	private void performStateChange( ReportStatus reportStatus ) {
		ReportTaskDto dto = new ReportTaskDto();
		BeanUtils.copyProperties( reportTask, dto );
		dto.setStatus( reportStatus );
		dto.setUpdated( new Date() );
		reportTask = BasicServiceHelper.save( dto, ReportTask.class, taskRepository );
	}
}
